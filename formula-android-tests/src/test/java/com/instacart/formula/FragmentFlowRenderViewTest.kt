package com.instacart.formula

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.BackCallback
import com.instacart.formula.test.TestContract
import com.instacart.formula.test.TestContractWithId
import com.instacart.formula.test.TestFragmentActivity
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentFlowRenderViewTest {

    class HeadlessFragment : Fragment()

    private var lastState: FragmentFlowState? = null
    private val stateChangeRelay = PublishRelay.create<Pair<FragmentContract<*>, Any>>()
    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {
                activity<TestFragmentActivity> {
                    store(
                        configureActivity = {
                            initialContract = TestContract()
                        },
                        onRenderFragmentState = { a, state ->
                            lastState = state
                        },
                        contracts =  {
                            bind { key: TestContract ->
                                stateChanges(key)
                            }

                            bind { key: TestContractWithId ->
                                stateChanges(key)
                            }
                        }
                    )
                }

            }
        },
        cleanUp = {
            lastState = null
        })

    private val activityRule = ActivityScenarioRule(TestFragmentActivity::class.java)

    @get:Rule val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestFragmentActivity>

    @Before fun setup() {
        scenario = activityRule.scenario
    }

    @Test fun `add fragment lifecycle event`() {
        assertThat(activeContracts()).containsExactly(TestContract()).inOrder()
    }

    @Test fun `pop backstack lifecycle event`() {
        navigateToTaskDetail()
        navigateBack()

        assertThat(activeContracts()).containsExactly(TestContract()).inOrder()
    }

    @Test fun `navigating forward should have both keys in backstack`() {
        navigateToTaskDetail()

        assertThat(activeContracts()).containsExactly(
            TestContract(),
            TestContractWithId(1)
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

        assertVisibleContract(TestContract())
        assertThat(activeContracts()).containsExactly(TestContract()).inOrder()
    }

    @Test fun `render model is passed to visible fragment`() {
        val activity = activity()
        sendStateUpdate(TestContract(), "update")
        assertThat(activity.renderCalls).containsExactly(TestContract() to "update").inOrder()
    }

    @Test fun `render model is not passed to not visible fragment`() {
        navigateToTaskDetail()

        val activity = activity()
        sendStateUpdate(TestContract(), "update")
        assertThat(activity.renderCalls).isEqualTo(emptyList<Any>())
    }

    @Test fun `visible fragments are updated when navigating`() {
        navigateToTaskDetail()

        val contract = TestContractWithId(1)

        val activity = activity()
        sendStateUpdate(contract, "update")
        assertThat(activity.renderCalls).containsExactly(contract to "update").inOrder()

        navigateBack()

        sendStateUpdate(contract, "update-two")
        assertThat(activity.renderCalls).containsExactly(contract to "update").inOrder()
    }

    @Test fun `delegates back press to current render model`() {
        navigateToTaskDetail()

        var backPressed = 0

        val contract = TestContractWithId(1)
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

        assertVisibleContract(TestContractWithId(1))
        // Both contracts should be active.
        assertThat(activeContracts()).containsExactly(TestContract(), TestContractWithId(1)).inOrder()
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
        assertVisibleContract(TestContractWithId(1))
        assertThat(activeContracts()).containsExactly(TestContractWithId(1), TestContract()).inOrder()

        navigateBack()

        assertVisibleContract(TestContract())
        assertThat(activeContracts()).containsExactly(TestContract()).inOrder()
    }

    @Test fun `add multiple fragments with same fragment key`() {
        scenario.moveToState(Lifecycle.State.RESUMED)

        navigateToTaskDetail(id = 1)
        navigateToTaskDetail(id = 2)
        navigateToTaskDetail(id = 1)

        assertFragmentViewIsCreated(TestContractWithId(1))
        assertThat(activeContracts()).containsExactly(TestContract(), TestContractWithId(1), TestContractWithId(2), TestContractWithId(1)).inOrder()

        navigateBack()
        navigateBack()

        assertFragmentViewIsCreated(TestContractWithId(1))
        assertThat(activeContracts()).containsExactly(TestContract(), TestContractWithId(1)).inOrder()
    }

    private fun navigateBack() {
        scenario.onActivity { it.onBackPressed() }
    }

    private fun navigateToTaskDetail(id: Int = 1) {
        scenario.onActivity {
            it.navigateTo(TestContractWithId(id))
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

    private fun assertVisibleContract(contract: FragmentContract<*>) {
        assertNoDuplicates(contract)
        // TODO: would be best to test visibleState() however `FragmentFlowState.states` is empty
        assertThat(scenario.get { lastState?.visibleIds?.lastOrNull()?.key }).isEqualTo(contract)
    }

    private fun assertNoDuplicates(contract: FragmentContract<*>) {
        assertThat(lastState?.visibleIds?.count { it.key == contract }).isEqualTo(1)
    }

    private fun <T : Any> sendStateUpdate(contract: FragmentContract<T>, update: T) {
        stateChangeRelay.accept(Pair(contract, update))
    }

    private fun stateChanges(contract: FragmentContract<*>): Observable<Any> {
        return stateChangeRelay
            .filter { event ->
                event.first == contract
            }
            .map { it.second }
    }
}
