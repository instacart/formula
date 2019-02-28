package com.instacart.formula

import androidx.fragment.app.Fragment
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentFlowRenderViewTest {

    class HeadlessFragment : Fragment() {

    }

    @get:Rule val rule = ActivityTestRule(BasicIntegrationActivity::class.java)

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
                rule.activity.onBackPressed()
            }
            .values()
            .apply {
                assertThat(this.last().backStack.keys).isEmpty()
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
}
