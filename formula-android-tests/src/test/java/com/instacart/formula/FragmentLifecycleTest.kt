package com.instacart.formula

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentFlowStore
import com.instacart.formula.android.ViewFactory
import com.instacart.formula.test.TestFragmentActivity
import com.instacart.formula.test.TestFragmentLifecycleCallback
import com.instacart.formula.test.TestLifecycleKey
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

@RunWith(AndroidJUnit4::class)
class FragmentLifecycleTest {

    private lateinit var activityController: ActivityController<TestFragmentActivity>
    private lateinit var lifecycleCallback: TestFragmentLifecycleCallback
    private lateinit var contract: TestLifecycleKey

    @get:Rule val formulaRule = TestFormulaRule(initFormula = { app ->
        FormulaAndroid.init(app) {
            activity<TestFragmentActivity> {
                ActivityStore(
                    configureActivity = { activity ->
                        lifecycleCallback = TestFragmentLifecycleCallback()
                        contract = TestLifecycleKey()
                        activity.initialContract = contract
                    },
                    fragmentStore = FragmentFlowStore.init {
                        val featureFactory = object : FeatureFactory<Unit, TestLifecycleKey> {
                            override fun initialize(dependencies: Unit, key: TestLifecycleKey): Feature {
                                return Feature(
                                    state = Observable.empty(),
                                    viewFactory = ViewFactory.fromLayout(R.layout.test_empty_layout) {
                                        featureView(lifecycleCallback) {}
                                    }
                                )
                            }
                        }
                        bind(featureFactory)
                    }
                )
            }
        }
    })

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, TestFragmentActivity::class.java)
        val activityController = Robolectric.buildActivity(TestFragmentActivity::class.java, intent)
            .setup()

        this.activityController = activityController
    }

    @Test fun `creation callbacks`() {
        assertThat(contract).isNotNull()
        assertThat(lifecycleCallback.hasOnViewCreated).isTrue()
        assertThat(lifecycleCallback.hasOnActivityCreated).isTrue()
        assertThat(lifecycleCallback.hasOnStart).isTrue()
        assertThat(lifecycleCallback.hasOnResume).isTrue()
    }

    @Test fun `destroy callbacks`() {
        activityController.destroy()
        assertThat(lifecycleCallback.hasOnPauseEvent).isTrue()
        assertThat(lifecycleCallback.hasOnStop).isTrue()
    }

    @Test fun `save instance state callback`() {
        activityController.saveInstanceState(Bundle())
        assertThat(lifecycleCallback.hasOnSaveInstanceState).isTrue()
    }

    // Unfortunately, we cannot test destroy view with Robolectric
    // https://github.com/robolectric/robolectric/issues/1945
}
