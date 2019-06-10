package com.instacart.formula

import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.integration.BackCallback
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
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
                activity(TestFlowViewActivity::class) {
                    store(
                        onRenderFragmentState = { a, state ->
                            lastState = state
                        }
                    ) {
                        bind { key: TestContract ->
                            stateChanges(key)
                        }

                        bind { key: TestContractWithId ->
                            stateChanges(key)
                        }
                    }
                }

            }
        },
        cleanUp = {
            lastState = null
        })

    private val activityRule = ActivityScenarioRule(TestFlowViewActivity::class.java)

    @get:Rule val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestFlowViewActivity>

    @Before fun setup() {
        scenario = activityRule.scenario
    }

    @Test fun `add fragment lifecycle event`() {
        assertThat(currentBackstack()).containsExactly(TestContract())
    }

    @Test fun `pop backstack lifecycle event`() {
        navigateToTaskDetail()
        navigateBack()

        assertThat(currentBackstack()).containsExactly(TestContract())
    }

    @Test fun `navigating forward should have both keys in backstack`() {
        navigateToTaskDetail()

        assertThat(currentBackstack()).containsExactly(
            TestContract(),
            TestContractWithId(1)
        )
    }

    @Test fun `ignore headless fragments`() {
        // add headless fragment
        scenario.onActivity {
            it.supportFragmentManager
                .beginTransaction()
                .add(HeadlessFragment(), "headless")
                .commitNow()
        }

        assertThat(currentBackstack()).containsExactly(TestContract())
    }

    @Test fun `render model is passed to visible fragment`() {
        val activity = activity()
        sendStateUpdate(TestContract(), "update")
        assertThat(activity.renderCalls).containsExactly(TestContract() to "update")
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
        assertThat(activity.renderCalls).containsExactly(contract to "update")

        navigateBack()

        sendStateUpdate(contract, "update-two")
        assertThat(activity.renderCalls).containsExactly(contract to "update")
    }

    @Test fun `delegates back press to current render model`() {
        navigateToTaskDetail()

        var backPressed = 0

        val contract = TestContractWithId(1)
        sendStateUpdate(contract, object : BackCallback {
            override fun onBackPressed() {
                backPressed += 1
            }
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

        // Both contracts should be in the backstack.
        assertThat(currentBackstack()).containsExactly(TestContract(), TestContractWithId(1))
    }

    @Test fun `process death imitation`() {
        navigateToTaskDetail()

        val previous = activity()
        formulaRule.fakeProcessDeath()

        scenario.recreate()

        // Verify that activity has changed
        val new = activity()
        assertThat(previous).isNotEqualTo(new)

        // When activity is recreated, it only triggers event for current fragment
        assertThat(currentBackstack()).containsExactly(TestContractWithId(1))

        navigateBack()

        assertThat(currentBackstack()).containsExactly(TestContract())
    }

    private fun navigateBack() {
        scenario.onActivity { it.onBackPressed() }
    }

    private fun navigateToTaskDetail() {
        val detail = TestContractWithId(1)
        scenario.onActivity {
            it.supportFragmentManager.beginTransaction()
                .remove(it.supportFragmentManager.findFragmentByTag(TestContract().tag)!!)
                .add(R.id.activity_content, FormulaFragment.newInstance(detail), detail.tag)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun activity(): TestFlowViewActivity {
        return scenario.activity()
    }

    private fun currentBackstack(): List<FragmentContract<*>> {
        return scenario.get {
            lastState!!.backStack.keys
        }
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
