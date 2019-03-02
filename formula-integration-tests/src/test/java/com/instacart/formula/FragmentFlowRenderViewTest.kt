package com.instacart.formula

import androidx.fragment.app.Fragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.integration.BackCallback
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentFlowRenderViewTest {

    class HeadlessFragment : Fragment()

    @get:Rule val rule = ActivityTestRule(TestActivity::class.java)

    @Test fun `add fragment lifecycle event`() {
        rule.activity.component.state.test().values().apply {
            assertThat(this.last().backStack.keys).containsExactly(
                TaskListContract()
            )
        }
    }

    @Test fun `pop backstack lifecycle event`() {
        navigateToTaskDetail()
        rule.activity.onBackPressed()

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
        rule.activity.supportFragmentManager
            .beginTransaction()
            .add(HeadlessFragment(), "headless")
            .commitNow()

        assertThat(currentBackstack()).containsExactly(TaskListContract())
    }

    @Test fun `render model is passed to visible fragment`() {
        val component = rule.activity.component
        component.sendStateUpdate(TaskListContract(), "update")
        assertThat(component.renderCalls).containsExactly(TaskListContract() to "update")
    }

    @Test fun `render model is not passed to not visible fragment`() {
        navigateToTaskDetail()

        val component = rule.activity.component
        component.sendStateUpdate(TaskListContract(), "update")
        assertThat(component.renderCalls).isEqualTo(emptyList<Any>())
    }

    @Test fun `visible fragments are updated when navigating`() {
        navigateToTaskDetail()

        val contract = TaskDetailContract(1)

        val component = rule.activity.component
        component.sendStateUpdate(contract, "update")
        assertThat(component.renderCalls).containsExactly(contract to "update")

        rule.activity.onBackPressed()

        component.sendStateUpdate(contract, "update-two")
        assertThat(component.renderCalls).containsExactly(contract to "update")
    }

    @Test fun `delegates back press to current render model`() {
        navigateToTaskDetail()

        var backPressed = 0

        val contract = TaskDetailContract(1)
        val component = rule.activity.component
        component.sendStateUpdate(contract, object : BackCallback {
            override fun onBackPressed() {
                backPressed += 1
            }
        })

        rule.activity.onBackPressed()
        rule.activity.onBackPressed()

        assertThat(backPressed).isEqualTo(2)
    }

    private fun navigateToTaskDetail() {
        val detail = TaskDetailContract(1)
        rule.activity.supportFragmentManager.beginTransaction()
            .remove(rule.activity.supportFragmentManager.findFragmentByTag(TaskListContract().tag)!!)
            .add(R.id.activity_content, FormulaFragment.newInstance(detail), detail.tag)
            .addToBackStack(null)
            .commit()
    }

    private fun currentBackstack(): List<FragmentContract<*>> {
        return rule.activity.component.currentFragmentState().backStack.keys
    }
}
