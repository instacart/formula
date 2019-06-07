package com.instacart.formula

import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.integration.BackCallback
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentFlowRenderViewTest {

    class HeadlessFragment : Fragment()

    val formulaRule = TestFormulaRule(TestFlowViewActivity::class)
    val activityRule = ActivityScenarioRule(TestFlowViewActivity::class.java)
    @get:Rule val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestFlowViewActivity>

    @Before fun setup() {
        scenario = activityRule.scenario
    }

    fun <T : Any> sendStateUpdate(contract: FragmentContract<T>, update: T) {
        formulaRule.sendStateUpdate(contract, update)
    }

    @Test fun `add fragment lifecycle event`() {
        assertThat(currentBackstack()).containsExactly(TaskListContract())
    }

    @Test fun `pop backstack lifecycle event`() {
        navigateToTaskDetail()
        navigateBack()

        assertThat(currentBackstack()).containsExactly(TaskListContract())
    }

    @Test fun `navigating forward should have both keys in backstack`() {
        navigateToTaskDetail()

        assertThat(currentBackstack()).containsExactly(
            TaskListContract(),
            TaskDetailContract(1)
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

        assertThat(currentBackstack()).containsExactly(TaskListContract())
    }

    @Test fun `render model is passed to visible fragment`() {
        val viewModel = activity()
        sendStateUpdate(TaskListContract(), "update")
        assertThat(viewModel.renderCalls).containsExactly(TaskListContract() to "update")
    }

    @Test fun `render model is not passed to not visible fragment`() {
        navigateToTaskDetail()

        val viewModel = activity()
        sendStateUpdate(TaskListContract(), "update")
        assertThat(viewModel.renderCalls).isEqualTo(emptyList<Any>())
    }

    @Test fun `visible fragments are updated when navigating`() {
        navigateToTaskDetail()

        val contract = TaskDetailContract(1)

        val viewModel = activity()
        sendStateUpdate(contract, "update")
        assertThat(viewModel.renderCalls).containsExactly(contract to "update")

        navigateBack()

        sendStateUpdate(contract, "update-two")
        assertThat(viewModel.renderCalls).containsExactly(contract to "update")
    }

    @Test fun `delegates back press to current render model`() {
        navigateToTaskDetail()

        var backPressed = 0

        val contract = TaskDetailContract(1)
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

        // Verify that view models have changed
        val new = activity()
        assertThat(previous).isNotEqualTo(new)

        // Both contracts should be in the backstack.
        assertThat(currentBackstack()).containsExactly(TaskListContract(), TaskDetailContract(1))
    }

    @Test fun `process death imitation`() {
        navigateToTaskDetail()

        val previous = activity()
        formulaRule.fakeProcessDeath()

        scenario.recreate()

        // Verify that view models have changed
        val new = activity()
        assertThat(previous).isNotEqualTo(new)

        // When activity is recreated, it only triggers event for current fragment
        assertThat(currentBackstack()).containsExactly(TaskDetailContract(1))

        navigateBack()

        assertThat(currentBackstack()).containsExactly(TaskListContract())
    }

    private fun navigateBack() {
        scenario.onActivity { it.onBackPressed() }
    }

    private fun navigateToTaskDetail() {
        val detail = TaskDetailContract(1)
        scenario.onActivity {
            it.supportFragmentManager.beginTransaction()
                .remove(it.supportFragmentManager.findFragmentByTag(TaskListContract().tag)!!)
                .add(R.id.activity_content, FormulaFragment.newInstance(detail), detail.tag)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun activity(): TestFlowViewActivity {
        return get { this }
    }

    private fun currentBackstack(): List<FragmentContract<*>> {
        return get {
            formulaRule.lastState!!.backStack.keys
        }
    }

    private fun <T> get(select: TestFlowViewActivity.() -> T): T {
        val list: MutableList<T> = mutableListOf()
        scenario.onActivity {
            list.add(it.select())
        }
        return list.first()
    }
}
