package com.instacart.formula

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instacart.testutils.android.TestFragmentActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * Tests that formula-android module handles non-bound fragment activities gracefully.
 */
@RunWith(AndroidJUnit4::class)
class NonBoundFragmentActivityTest {
    private val formulaRule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {}
        }
    )

    private val activityRule = ActivityScenarioRule(TestFragmentActivity::class.java)

    @get:Rule
    val rule = RuleChain.outerRule(formulaRule).around(activityRule)
    lateinit var scenario: ActivityScenario<TestFragmentActivity>

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