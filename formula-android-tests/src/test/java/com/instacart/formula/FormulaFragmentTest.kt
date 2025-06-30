package com.instacart.formula

import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.FragmentState
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.BackCallback
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentStore
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.test.TestBackCallbackRenderModel
import com.instacart.testutils.android.TestKey
import com.instacart.formula.test.TestKeyWithId
import com.instacart.testutils.android.HeadlessFragment
import com.instacart.testutils.android.TestFormulaActivity
import com.instacart.testutils.android.activity
import com.instacart.testutils.android.showFragment
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FormulaFragmentTest {

    private var lastState: FragmentState? = null
    private val stateChangeRelay = PublishRelay.create<Pair<FragmentKey, Any>>()
    private var updateThreads = linkedSetOf<Thread>()
    private val errors = mutableListOf<Throwable>()
    private val fragmentLifecycleEvents = mutableListOf<FragmentLifecycleEvent>()
    private val renderCalls = mutableListOf<Pair<FragmentKey, *>>()

    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            val environment = FragmentEnvironment(
                onScreenError = { _, error ->
                    errors.add(error)
                }
            )
            FormulaAndroid.init(app) {
                activity<TestFormulaActivity> {
                    ActivityStore(
                        fragmentStore = FragmentStore.Builder()
                            .setFragmentEnvironment(environment)
                            .setOnPreRenderFragmentState {
                                lastState = it
                                updateThreads.add(Thread.currentThread())
                            }
                            .setOnFragmentLifecycleEvent {
                                fragmentLifecycleEvents.add(it)
                            }
                            .build {
                                bind(
                                    featureFactory = TestFeatureFactory<TestKey>(
                                        render = { key, value ->
                                            renderCalls.add(key to value)
                                        },
                                        state = { stateChanges(it) }
                                    )
                                )
                                bind(
                                    TestFeatureFactory<TestKeyWithId>(
                                    render = { key, output ->
                                        renderCalls.add(key to output)
                                        if (output == "crash") {
                                            throw IllegalStateException("crashing")
                                        }
                                    },
                                    state = {
                                        stateChanges(it)
                                    }
                                ))
                            },
                    )
                }

            }
        },
        cleanUp = {
            lastState = null
            updateThreads = linkedSetOf()
            fragmentLifecycleEvents.clear()
        }
    )

    private val activityRule = ActivityScenarioRule(TestFormulaActivity::class.java)

    @get:Rule val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestFormulaActivity>

    @Before fun setup() {
        scenario = activityRule.scenario
        scenario.showFragment(TestKey())
    }

    @Test fun `add fragment lifecycle event`() {
        assertThat(activeContracts()).containsExactly(TestKey()).inOrder()
    }

    @Test fun `pop backstack lifecycle event`() {
        navigateToTaskDetail()
        navigateBack()

        assertThat(activeContracts()).containsExactly(TestKey()).inOrder()

        assertThat(fragmentLifecycleEvents).hasSize(3)
        assertThat(fragmentLifecycleEvents[0]).isInstanceOf(FragmentLifecycleEvent.Added::class.java)
        assertThat(fragmentLifecycleEvents[1]).isInstanceOf(FragmentLifecycleEvent.Added::class.java)
        assertThat(fragmentLifecycleEvents[2]).isInstanceOf(FragmentLifecycleEvent.Removed::class.java)
    }

    @Test fun `navigating forward should have both keys in backstack`() {
        navigateToTaskDetail()

        assertThat(activeContracts()).containsExactly(
            TestKey(),
            TestKeyWithId(1)
        ).inOrder()
    }

    @Test fun `ignore headless fragments`() {
        // add headless fragment
        scenario.onActivity {
            it.supportFragmentManager
                .beginTransaction()
                .add(HeadlessFragment(), "headless")
                .commitNow()
        }

        assertVisibleContract(TestKey())
        assertThat(activeContracts()).containsExactly(TestKey()).inOrder()
    }

    @Test fun `render model is passed to visible fragment`() {
        sendStateUpdate(TestKey(), "update")
        assertThat(renderCalls).containsExactly(TestKey() to "update").inOrder()
    }

    @Test fun `render model is not passed to not visible fragment`() {
        navigateToTaskDetail()

        sendStateUpdate(TestKey(), "update")
        assertThat(renderCalls).isEqualTo(emptyList<Any>())
    }

    @Test fun `visible fragments are updated when navigating`() {
        navigateToTaskDetail()

        val contract = TestKeyWithId(1)

        sendStateUpdate(contract, "update")
        assertThat(renderCalls).containsExactly(contract to "update").inOrder()

        navigateBack()

        sendStateUpdate(contract, "update-two")
        assertThat(renderCalls).containsExactly(contract to "update").inOrder()
    }

    @Test fun `delegates back press to current render model`() {
        navigateToTaskDetail()

        var backPressed = 0

        val contract = TestKeyWithId(1)
        sendStateUpdate(contract, BackCallback {
            backPressed += 1
            true
        })

        navigateBack()
        navigateBack()

        assertThat(backPressed).isEqualTo(2)
    }

    @Test fun `activity restart`() {
        navigateToTaskDetail()

        val previous = activity()

        scenario.recreate()

        // Verify that activity has changed
        val new = activity()
        assertThat(previous).isNotEqualTo(new)

        assertVisibleContract(TestKeyWithId(1))
        // Both contracts should be active.
        assertThat(activeContracts()).containsExactly(TestKey(), TestKeyWithId(1)).inOrder()
    }

    @Test fun `process death imitation`() {
        navigateToTaskDetail()

        val previous = activity()
        formulaRule.fakeProcessDeath()

        scenario.recreate()

        // Verify that activity has changed
        val new = activity()
        assertThat(previous).isNotEqualTo(new)

        // When activity is recreated, it first triggers current fragment and
        // then loads ones from backstack
        assertVisibleContract(TestKeyWithId(1))
        assertThat(activeContracts()).containsExactly(TestKeyWithId(1), TestKey()).inOrder()

        navigateBack()

        assertVisibleContract(TestKey())
        assertThat(activeContracts()).containsExactly(TestKey()).inOrder()
    }

    @Test fun `add multiple fragments with same fragment key`() {
        scenario.moveToState(Lifecycle.State.RESUMED)

        navigateToTaskDetail(id = 1)
        navigateToTaskDetail(id = 2)
        navigateToTaskDetail(id = 1)

        assertFragmentViewIsCreated(TestKeyWithId(1))
        assertThat(activeContracts()).containsExactly(TestKey(), TestKeyWithId(1), TestKeyWithId(2), TestKeyWithId(1)).inOrder()

        navigateBack()
        navigateBack()

        assertFragmentViewIsCreated(TestKeyWithId(1))
        assertThat(activeContracts()).containsExactly(TestKey(), TestKeyWithId(1)).inOrder()
    }

    @Test fun `background feature events are moved to the main thread`() {

        val executor = Executors.newSingleThreadExecutor()
        val latch = CountDownLatch(1)

        val initial = TestKey()
        val keyWithId = TestKeyWithId(1)

        navigateToTaskDetail(1)
        // Both contracts should be active.
        assertThat(activeContracts()).containsExactly(TestKey(), TestKeyWithId(1)).inOrder()

        // Pass feature updates on a background thread
        executor.execute {
            sendStateUpdate(initial, "main-state-1")
            sendStateUpdate(initial, "main-state-2")
            sendStateUpdate(initial, "main-state-3")

            sendStateUpdate(keyWithId, "detail-state-1")
            sendStateUpdate(keyWithId, "detail-state-2")
            sendStateUpdate(keyWithId, "detail-state-3")
            latch.countDown()
        }

        // Wait for background execution to finish
        if(!latch.await(100, TimeUnit.MILLISECONDS)) {
            throw IllegalStateException("timeout")
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val currentState = lastState?.outputs.orEmpty()
            .mapKeys { it.key.key }
            .mapValues { it.value.renderModel }

        val expected = mapOf(
            TestKey() to "main-state-3",
            TestKeyWithId(1) to "detail-state-3"
        )

        assertThat(currentState).isEqualTo(expected)
        assertThat(updateThreads).hasSize(1)
        assertThat(updateThreads).containsExactly(Thread.currentThread())
    }

    @Test fun `back callback blocks navigation`() {
        val key = TestKeyWithId(1)
        navigateToTaskDetail(id = key.id)

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        var onBackPressed = 0
        sendStateUpdate(key, TestBackCallbackRenderModel(
            onBackPressed = {
                onBackPressed += 1
            },
            blockBackCallback = true
        ))

        navigateBack()

        // We blocked navigation so visible fragment should still be details
        assertThat(onBackPressed).isEqualTo(1)
        assertVisibleContract(key)

        sendStateUpdate(key, TestBackCallbackRenderModel(
            onBackPressed = { onBackPressed += 1 },
            blockBackCallback = false
        ))

        navigateBack()

        assertThat(onBackPressed).isEqualTo(2)
        assertVisibleContract(TestKey())
    }

    @Test fun `notify fragment environment if setOutput throws an error`() {
        val key = TestKeyWithId(1)
        navigateToTaskDetail(id = key.id)

        sendStateUpdate(key, "crash")
        assertThat(renderCalls).isNotEmpty()

        assertThat(errors).hasSize(1)
    }

    @Test
    fun toStringContainsTagAndKey() {
        val fragment = FormulaFragment.newInstance(TestKey())
        val toStringValue = fragment.toString()
        assertThat(toStringValue).isEqualTo(
            "test key -> TestKey(tag=test key)"
        )
    }

    private fun navigateBack() {
        scenario.onActivity { it.onBackPressed() }
    }

    private fun navigateToTaskDetail(id: Int = 1, allowStateLoss: Boolean = false) {
        val fragmentKey = TestKeyWithId(id)
        scenario.showFragment(fragmentKey, allowStateLoss)
    }

    private fun assertFragmentViewIsCreated(key: FragmentKey) {
        scenario.onActivity {
            val view = it.supportFragmentManager.findFragmentByTag(key.tag)?.view
            assertThat(view).isNotNull()
        }
    }

    private fun activity(): TestFormulaActivity {
        return scenario.activity()
    }

    private fun activeContracts(): List<FragmentKey> {
        return lastState!!.activeIds.map { it.key }
    }

    private fun assertVisibleContract(contract: FragmentKey) {
        assertNoDuplicates(contract)
        // TODO: would be best to test visibleState() however `FragmentFlowState.states` is empty
        assertThat(lastState?.visibleIds?.lastOrNull()?.key).isEqualTo(contract)
    }

    private fun assertNoDuplicates(contract: FragmentKey) {
        assertThat(lastState?.visibleIds?.count { it.key == contract }).isEqualTo(1)
    }

    private fun sendStateUpdate(contract: FragmentKey, update: Any) {
        stateChangeRelay.accept(Pair(contract, update))
    }

    private fun stateChanges(contract: FragmentKey): Observable<Any> {
        return stateChangeRelay
            .filter { event ->
                event.first == contract
            }
            .map { it.second }
    }
}
