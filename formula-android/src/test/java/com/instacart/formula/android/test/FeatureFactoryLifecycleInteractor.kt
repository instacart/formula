package com.instacart.formula.android.test

import androidx.test.core.app.ActivityScenario
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.NavigationStore
import com.instacart.testutils.android.NoOpFeatureFactory
import com.instacart.testutils.android.TestFormulaActivity
import com.instacart.testutils.android.TestFragmentLifecycleCallback
import com.instacart.testutils.android.TestKey
import com.instacart.testutils.android.TestViewFactory
import com.instacart.testutils.android.showFragment
import com.instacart.testutils.android.withFormulaAndroid

class FeatureFactoryLifecycleInteractor(
    val scenario: ActivityScenario<TestFormulaActivity>,
    val lifecycleCallback: TestFragmentLifecycleCallback,
)

fun runFeatureFactoryLifecycleTest(
    continuation: FeatureFactoryLifecycleInteractor.() -> Unit
) {
    val lifecycleCallback = TestFragmentLifecycleCallback()
    withFormulaAndroid(
        configure = {
            activity<TestFormulaActivity> {
                ActivityStore(
                    navigationStore = NavigationStore.Builder().build {
                        val featureFactory = NoOpFeatureFactory<TestKey>(
                            viewFactory = TestViewFactory(lifecycleCallback)
                        )
                        bind(featureFactory)
                    },
                )
            }
        }
    ) {
        val scenario = ActivityScenario.launch(TestFormulaActivity::class.java)
        scenario.showFragment(TestKey())
        val interactor = FeatureFactoryLifecycleInteractor(scenario, lifecycleCallback)
        continuation(interactor)
    }
}