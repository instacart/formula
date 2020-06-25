package com.instacart.formula

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.activity.ActivityResult
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
                    store(
                        configureActivity = {
                            initialContract = TestLifecycleContract()
                        },
                        contracts =  {
                            bind(TestLifecycleContract::class) { _ ->
                                activityResults().flatMap {
                                    activityResults.add(it)
                                    Observable.empty<Any>()
                                }
                            }
                        }
                    )
                }
            }
        },
        cleanUp = {
            activityResults.clear()
        })

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
