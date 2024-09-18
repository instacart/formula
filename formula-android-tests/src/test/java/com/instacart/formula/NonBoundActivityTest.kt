package com.instacart.formula

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * Tests that formula-android module handles non-bound activities gracefully.
 */
@RunWith(AndroidJUnit4::class)
class NonBoundActivityTest {
    class TestActivity : Activity()

    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {}
        }
    )

    private val activityRule = ActivityScenarioRule(TestActivity::class.java)

    @get:Rule
    val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestActivity>

    @Before
    fun setup() {
        scenario = activityRule.scenario
    }

    @Test
    fun `full lifecycle`() {
        scenario.recreate()
        scenario.close()
    }
}