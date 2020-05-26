package com.instacart.formula

import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.integration.FormulaAppCompatActivity
import com.jakewharton.rxrelay3.PublishRelay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityUpdateTest {

    class TestActivity : FormulaAppCompatActivity() {
        val updates = mutableListOf<String>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.test_activity)
        }

        fun applyUpdate(update: String) {
            updates.add(update)
        }
    }

    private val updateRelay = PublishRelay.create<String>()

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

    @Test fun `basic updates`() {
        updateRelay.accept("update-1")
        updateRelay.accept("update-2")

        assertThat(updates()).containsExactly("update-1", "update-2")
    }

    @Test fun `last update is applied after configuration changes`() {
        updateRelay.accept("update-1")
        updateRelay.accept("update-2")

        scenario.recreate()

        assertThat(updates()).containsExactly("update-2")
    }

    @Test fun `updates are unsubscribed from when activity is finished`() {
        assertThat(updateRelay.hasObservers()).isTrue()

        scenario.close()

        assertThat(updateRelay.hasObservers()).isFalse()
    }

    private fun updates() = scenario.get { updates }
}
