package com.instacart.formula

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentStore
import com.instacart.formula.android.ViewFactory
import com.instacart.formula.test.TestFragmentActivity
import com.instacart.formula.test.TestFragmentLifecycleCallback
import com.instacart.formula.test.TestLifecycleKey
import com.instacart.testutils.android.R as TestR
import io.reactivex.rxjava3.core.Observable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentLifecycleTest {

    private lateinit var lifecycleCallback: TestFragmentLifecycleCallback

    @get:Rule val formulaRule = TestFormulaRule(initFormula = { app ->
        FormulaAndroid.init(app) {
            activity<TestFragmentActivity> {
                lifecycleCallback = TestFragmentLifecycleCallback()
                ActivityStore(
                    configureActivity = { activity ->
                        activity.initialKey = TestLifecycleKey()
                    },
                    fragmentStore = FragmentStore.init {
                        val featureFactory = object : FeatureFactory<Unit, TestLifecycleKey> {
                            override fun initialize(dependencies: Unit, key: TestLifecycleKey): Feature {
                                return Feature(
                                    state = Observable.empty(),
                                    viewFactory = ViewFactory.fromLayout(TestR.layout.test_fragment_layout) {
                                        featureView(lifecycleCallback) {}
                                    }
                                )
                            }
                        }
                        bind(featureFactory)
                    },
                )
            }
        }
    })

    @Test fun `creation callbacks`() {
        ActivityScenario.launch(TestFragmentActivity::class.java)

        assertThat(lifecycleCallback.hasOnViewCreated).isTrue()
        assertThat(lifecycleCallback.hasOnActivityCreated).isTrue()
        assertThat(lifecycleCallback.hasOnStart).isTrue()
        assertThat(lifecycleCallback.hasOnResume).isTrue()
    }

    @Test fun `destroy callbacks`() {
        val scenario = ActivityScenario.launch(TestFragmentActivity::class.java)
        scenario.close()

        assertThat(lifecycleCallback.hasOnPauseEvent).isTrue()
        assertThat(lifecycleCallback.hasOnStop).isTrue()
        assertThat(lifecycleCallback.hasOnDestroyView).isTrue()
    }

    @Test fun `save instance state callback`() {
        val scenario = ActivityScenario.launch(TestFragmentActivity::class.java)

        assertThat(lifecycleCallback.hasOnSaveInstanceState).isFalse()
        scenario.recreate()
        assertThat(lifecycleCallback.hasOnSaveInstanceState).isTrue()
    }

    @Test fun `low memory`() {
        val scenario = ActivityScenario.launch(TestFragmentActivity::class.java)
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments
                .filterIsInstance<FormulaFragment>()
                .first()

            fragment.onLowMemory()
        }
        assertThat(lifecycleCallback.hasCalledLowMemory).isTrue()
    }
}
