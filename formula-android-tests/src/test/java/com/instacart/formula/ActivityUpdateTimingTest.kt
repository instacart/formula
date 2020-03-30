package com.instacart.formula

import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.integration.FormulaAppCompatActivity
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityUpdateTimingTest {

    class TestActivity : FormulaAppCompatActivity() {
        lateinit var updates: MutableList<String>

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.test_activity)

            // we delay initialization after super.onCreate() to check that Formula timing doesn't break.
            updates = mutableListOf()
        }

        fun applyUpdate(update: String) {
            updates.add(update)
        }
    }

    private val updateRelay = Observable.just("update-1", "update-2")

    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {
                activity<TestActivity> {
                    store(
                        streams = {
                            update(updateRelay, TestActivity::applyUpdate)
                        }
                    )
                }
            }
        })

    private val activityRule = ActivityScenarioRule(TestActivity::class.java)

    @get:Rule val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestActivity>

    @Before fun setup() {
        scenario = activityRule.scenario
    }

    @Test fun `last update arrives`() {
        val updates = scenario.get { updates }
        assertThat(updates).containsExactly("update-2")
    }
}
