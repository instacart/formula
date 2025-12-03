package com.instacart.formula.android

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.testutils.android.NoOpFeatureFactory
import com.instacart.testutils.android.TestFormulaActivity
import com.instacart.testutils.android.TestKey
import com.instacart.testutils.android.showFragment
import com.instacart.testutils.android.withFormulaAndroid
import org.junit.Test
import org.junit.runner.RunWith
import com.instacart.testutils.android.R as TestR

@RunWith(AndroidJUnit4::class)
class ViewFactoryTest {

    @Test fun fromLayout() {
        withFormulaAndroid(
            configure = {
                activity<TestFormulaActivity> {
                    ActivityStore(
                        navigationStore = NavigationStore.Builder().build {
                            val featureFactory = NoOpFeatureFactory(
                                viewFactory = ViewFactory.fromLayout(TestR.layout.test_fragment_layout) {
                                    featureView {  }
                                }
                            )
                            bind(featureFactory)
                        }
                    )
                }
            }
        ) {
            val fragmentKey = TestKey()

            val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
            scenario.showFragment(fragmentKey)
            scenario.onActivity {
                val fragment = it.supportFragmentManager.findFragmentByTag(fragmentKey.tag)
                assertThat(fragment).isNotNull()
            }
        }
    }
}