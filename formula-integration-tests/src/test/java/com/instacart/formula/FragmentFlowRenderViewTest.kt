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
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentFlowRenderViewTest {

    class HeadlessFragment : Fragment()

    @get:Rule val rule = ActivityScenarioRule(TestFlowViewActivity::class.java)
    lateinit var scenario: ActivityScenario<TestFlowViewActivity>

    @Before fun setup() {
        scenario = rule.scenario
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
        val viewModel = viewModel()
        viewModel.sendStateUpdate(TaskListContract(), "update")
        assertThat(viewModel.renderCalls).containsExactly(TaskListContract() to "update")
    }

    @Test fun `render model is not passed to not visible fragment`() {
        navigateToTaskDetail()

        val viewModel = viewModel()
        viewModel.sendStateUpdate(TaskListContract(), "update")
        assertThat(viewModel.renderCalls).isEqualTo(emptyList<Any>())
    }

    @Test fun `visible fragments are updated when navigating`() {
        navigateToTaskDetail()

        val contract = TaskDetailContract(1)

        val viewModel = viewModel()
        viewModel.sendStateUpdate(contract, "update")
        assertThat(viewModel.renderCalls).containsExactly(contract to "update")

        navigateBack()

        viewModel.sendStateUpdate(contract, "update-two")
        assertThat(viewModel.renderCalls).containsExactly(contract to "update")
    }

    @Test fun `delegates back press to current render model`() {
        navigateToTaskDetail()

        var backPressed = 0

        val contract = TaskDetailContract(1)
        val viewModel = viewModel()
        viewModel.sendStateUpdate(contract, object : BackCallback {
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

        val previous = viewModel()

        scenario.recreate()

        // Verify that view models have changed
        val new = viewModel()
        assertThat(previous).isNotEqualTo(new)

        // Only restores the current fragment
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

    private fun viewModel(): TestFragmentFlowViewModel {
        return get { viewModel }
    }

    private fun currentBackstack(): List<FragmentContract<*>> {
        return get {
            viewModel.state.test().values().last().backStack.keys
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
