package com.instacart.formula

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.ActivityStore
import com.instacart.testutils.android.TestFormulaActivity
import com.instacart.testutils.android.get
import com.jakewharton.rxrelay3.PublishRelay
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityUpdateTest {

    private val updates = mutableListOf<Pair<Activity, String>>()
    private val updateRelay = PublishRelay.create<String>()

    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {
                activity<TestFormulaActivity> {
                    ActivityStore(
                        streams = {
                            update(updateRelay) { activity, state ->
                                updates.add(activity to state)
                            }
                        }
                    )
                }
            }
        })

    private val activityRule = ActivityScenarioRule(TestFormulaActivity::class.java)

    @get:Rule val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestFormulaActivity>

    @Before fun setup() {
        scenario = activityRule.scenario
    }

    @After fun cleanup() {
        updates.clear()
    }

    @Test fun `basic updates`() {
        updateRelay.accept("update-1")
        updateRelay.accept("update-2")

        assertThat(updates()).containsExactly("update-1", "update-2").inOrder()
    }

    @Test fun `last update is applied after configuration changes`() {
        updateRelay.accept("update-1")
        updateRelay.accept("update-2")

        scenario.recreate()

        assertThat(updates()).containsExactly("update-2").inOrder()
    }

    @Test fun `updates are unsubscribed from when activity is finished`() {
        assertThat(updateRelay.hasObservers()).isTrue()

        scenario.close()

        assertThat(updateRelay.hasObservers()).isFalse()
    }

    private fun updates() = scenario.get {
        updates.filter { it.first == this }.map { it.second }
    }
}
