package com.instacart.formula

import androidx.fragment.app.Fragment
import androidx.test.rule.ActivityTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.fragment.FormulaFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentFlowRenderViewTest {

    class HeadlessFragment : Fragment()

    @get:Rule val rule = ActivityTestRule(TestActivity::class.java)

    @Test fun `add fragment lifecycle event`() {
        rule.activity.store.state().test().values().apply {
            assertThat(this.last().backStack.keys).containsExactly(
                TaskListContract()
            )
        }
    }

    @Test fun `pop backstack lifecycle event`() {
        rule.activity.store.state().test()
            .apply {
                navigateToTaskDetail()
            }
            .apply {
                rule.activity.onBackPressed()
            }
            .values()
            .apply {
                assertThat(this.last().backStack.keys).containsExactly(TaskListContract())
            }
    }

    @Test fun `navigating forward should have both keys in backstack`() {
        rule.activity.store.state().test()
            .apply { navigateToTaskDetail() }
            .values()
            .apply {
                assertThat(this.last().backStack.keys).containsExactly(
                    TaskListContract(),
                    TaskDetailContract(1)
                )
            }
    }

    @Test fun `ignore headless fragments`() {
        rule.activity.store.state().test()
            .apply {
                rule.activity.supportFragmentManager
                    .beginTransaction()
                    .add(HeadlessFragment(), "headless")
                    .commitNow()
            }
            .values()
            .apply {
                assertThat(this.last().backStack.keys).containsExactly(
                    TaskListContract()
                )
            }
    }

    private fun navigateToTaskDetail() {
        val detail = TaskDetailContract(1)
        rule.activity.supportFragmentManager.beginTransaction()
            .remove(rule.activity.supportFragmentManager.findFragmentByTag(TaskListContract().tag)!!)
            .add(R.id.activity_content, FormulaFragment.newInstance(detail), detail.tag)
            .addToBackStack(null)
            .commit()
    }
}
