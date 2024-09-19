package com.instacart.formula

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentStore
import com.instacart.formula.test.TestFragmentLifecycleCallback
import com.instacart.formula.test.TestLifecycleKey
import com.instacart.testutils.android.NoOpFeatureFactory
import com.instacart.testutils.android.TestFormulaActivity
import com.instacart.testutils.android.TestViewFactory
import com.instacart.testutils.android.showFragment
import com.instacart.testutils.android.withFormulaAndroid
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentLifecycleTest {

    private fun runTest(
        continuation: (ActivityScenario<TestFormulaActivity>, TestFragmentLifecycleCallback) -> Unit
    ) {
        val lifecycleCallback = TestFragmentLifecycleCallback()
        withFormulaAndroid(
            configure = {
                activity<TestFormulaActivity> {
                    ActivityStore(
                        fragmentStore = FragmentStore.init {
                            val featureFactory = NoOpFeatureFactory<TestLifecycleKey>(
                                viewFactory = TestViewFactory(lifecycleCallback)
                            )
                            bind(featureFactory)
                        },
                    )
                }
            }
        ) {
            val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
            scenario.showFragment(TestLifecycleKey())
            continuation(scenario, lifecycleCallback)
        }
    }

    @Test fun `creation callbacks`() {
        runTest { _, lifecycleCallback ->
            assertThat(lifecycleCallback.hasOnViewCreated).isTrue()
            assertThat(lifecycleCallback.hasOnActivityCreated).isTrue()
            assertThat(lifecycleCallback.hasOnStart).isTrue()
            assertThat(lifecycleCallback.hasOnResume).isTrue()
        }
    }

    @Test fun `destroy callbacks`() {
        runTest { scenario, lifecycleCallback ->
            scenario.close()

            assertThat(lifecycleCallback.hasOnPauseEvent).isTrue()
            assertThat(lifecycleCallback.hasOnStop).isTrue()
            assertThat(lifecycleCallback.hasOnDestroyView).isTrue()
        }
    }

    @Test fun `save instance state callback`() {
        runTest { scenario, lifecycleCallback ->
            assertThat(lifecycleCallback.hasOnSaveInstanceState).isFalse()
            scenario.recreate()
            assertThat(lifecycleCallback.hasOnSaveInstanceState).isTrue()
        }
    }

    @Test fun `low memory`() {
        runTest { scenario, lifecycleCallback ->
            scenario.onActivity {
                val fragment = it.supportFragmentManager.fragments
                    .filterIsInstance<FormulaFragment>()
                    .first()

                fragment.onLowMemory()
            }
            assertThat(lifecycleCallback.hasCalledLowMemory).isTrue()
        }
    }
}
