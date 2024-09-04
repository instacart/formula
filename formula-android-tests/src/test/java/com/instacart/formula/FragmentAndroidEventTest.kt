package com.instacart.formula

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentStore
import com.instacart.formula.android.ViewFactory
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.test.TestFragmentActivity
import com.instacart.formula.test.TestLifecycleKey
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentAndroidEventTest {
    private val activityResults = mutableListOf<ActivityResult>()

    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {
                activity<TestFragmentActivity> {
                    ActivityStore(
                        configureActivity = {
                            it.initialContract = TestLifecycleKey()
                        },
                        fragmentStore = FragmentStore.init {
                            val featureFactory = object : FeatureFactory<Unit, TestLifecycleKey> {
                                override fun initialize(dependencies: Unit, key: TestLifecycleKey): Feature {
                                    return Feature(
                                        state = activityResults().flatMap {
                                            activityResults.add(it)
                                            Observable.empty()
                                        },
                                        viewFactory = ViewFactory.fromLayout(R.layout.test_empty_layout) {
                                            featureView { }
                                        }
                                    )
                                }
                            }

                            bind(featureFactory)
                        }
                    )
                }
            }
        },
        cleanUp = {
            activityResults.clear()
        }
    )

    private val activityRule = ActivityScenarioRule(TestFragmentActivity::class.java)

    @get:Rule val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestFragmentActivity>

    @Before fun setup() {
        scenario = activityRule.scenario
    }

    @Test fun `activity result`() {
        FormulaAndroid.onActivityResult(scenario.activity(), 1, 2, null)

        val expected = listOf(ActivityResult(1, 2, null))
        assertThat(activityResults).isEqualTo(expected)
    }
}
