package com.instacart.formula

import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.FragmentState
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.BackCallback
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentStore
import com.instacart.formula.test.TestKey
import com.instacart.formula.test.TestKeyWithId
import com.instacart.formula.test.TestFragmentActivity
import com.instacart.formula.test.TestLifecycleKey
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.LooperMode
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FormulaFragmentTest {

    class HeadlessFragment : Fragment()

    private var lastState: FragmentState? = null
    private val stateChangeRelay = PublishRelay.create<Pair<FragmentKey, Any>>()
    private var onPreCreated: (TestFragmentActivity) -> Unit = {}
    private var updateThreads = linkedSetOf<Thread>()
    private val errors = mutableListOf<Throwable>()
    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            val environment = FragmentEnvironment(
                onScreenError = { _, error ->
                    errors.add(error)
                }
            )
            FormulaAndroid.init(app, environment) {
                activity<TestFragmentActivity> {
                    ActivityStore(
                        configureActivity = { activity ->
                            activity.initialContract = TestKey()
                            onPreCreated(activity)
                        },
                        onRenderFragmentState = { a, state ->
                            lastState = state

                            updateThreads.add(Thread.currentThread())
                        },
                        fragmentStore = FragmentStore.init {
                            bind(TestFeatureFactory<TestKey> { stateChanges(it) })
                            bind(TestFeatureFactory<TestKeyWithId>(
                                applyOutput = { output ->
                                    if (output == "crash") {
                                        throw IllegalStateException("crashing")
                                    }
                                },
                                state = {
                                    stateChanges(it)
                                }
                            ))
                        }
                    )
                }

            }
        },
        cleanUp = {
            lastState = null
            updateThreads = linkedSetOf()
        }
    )

    private val activityRule = ActivityScenarioRule(TestFragmentActivity::class.java)

    @get:Rule val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestFragmentActivity>

    @Before fun setup() {
        scenario = activityRule.scenario
    }

    @Test fun `add fragment lifecycle event`() {
        assertThat(activeContracts()).containsExactly(TestKey()).inOrder()
    }

    @Test fun `pop backstack lifecycle event`() {
        navigateToTaskDetail()
        navigateBack()

        assertThat(activeContracts()).containsExactly(TestKey()).inOrder()
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
        val activity = activity()
        sendStateUpdate(TestKey(), "update")
        assertThat(activity.renderCalls).containsExactly(TestKey() to "update").inOrder()
    }

    @Test fun `render model is not passed to not visible fragment`() {
        navigateToTaskDetail()

        val activity = activity()
        sendStateUpdate(TestKey(), "update")
        assertThat(activity.renderCalls).isEqualTo(emptyList<Any>())
    }

    @Test fun `visible fragments are updated when navigating`() {
        navigateToTaskDetail()

        val contract = TestKeyWithId(1)

        val activity = activity()
        sendStateUpdate(contract, "update")
        assertThat(activity.renderCalls).containsExactly(contract to "update").inOrder()

        navigateBack()

        sendStateUpdate(contract, "update-two")
        assertThat(activity.renderCalls).containsExactly(contract to "update").inOrder()
    }

    @LooperMode(LooperMode.Mode.LEGACY)
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
            stateChangeRelay.accept(initial to "main-state-1")
            stateChangeRelay.accept(initial to "main-state-2")
            stateChangeRelay.accept(initial to "main-state-3")

            stateChangeRelay.accept(keyWithId to "detail-state-1")
            stateChangeRelay.accept(keyWithId to "detail-state-2")
            stateChangeRelay.accept(keyWithId to "detail-state-3")
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

    @Test fun `notify fragment environment if setOutput throws an error`() {
        val key = TestKeyWithId(1)
        navigateToTaskDetail(id = key.id)

        val activity = activity()
        sendStateUpdate(key, "crash")
        assertThat(activity.renderCalls).isNotEmpty()

        assertThat(errors).hasSize(1)
    }

    @Test
    fun toStringContainsTagAndKey() {
        val fragment = FormulaFragment.newInstance(TestLifecycleKey())
        val toStringValue = fragment.toString()
        assertThat(toStringValue).isEqualTo(
            "test-lifecycle -> TestLifecycleKey(tag=test-lifecycle)"
        )
    }

    private fun navigateBack() {
        scenario.onActivity { it.onBackPressed() }
    }

    private fun navigateToTaskDetail(id: Int = 1, allowStateLoss: Boolean = false) {
        scenario.onActivity {
            it.navigateTo(TestKeyWithId(id), allowStateLoss = allowStateLoss)
        }
    }

    private fun assertFragmentViewIsCreated(key: FragmentKey) {
        scenario.onActivity {
            val view = it.supportFragmentManager.findFragmentByTag(key.tag)?.view
            assertThat(view).isNotNull()
        }
    }

    private fun activity(): TestFragmentActivity {
        return scenario.activity()
    }

    private fun activeContracts(): List<FragmentKey> {
        return scenario.get {
            lastState!!.activeIds.map { it.key }
        }
    }

    private fun assertVisibleContract(contract: FragmentKey) {
        assertNoDuplicates(contract)
        // TODO: would be best to test visibleState() however `FragmentFlowState.states` is empty
        assertThat(scenario.get { lastState?.visibleIds?.lastOrNull()?.key }).isEqualTo(contract)
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
