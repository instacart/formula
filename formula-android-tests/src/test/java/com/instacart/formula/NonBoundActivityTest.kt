package com.instacart.formula

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instacart.testutils.android.TestActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests that formula-android module handles non-bound activities gracefully.
 */
@RunWith(AndroidJUnit4::class)
class NonBoundActivityTest {

    @get:Rule
    val rule = TestFormulaRule(
        initFormula = { app ->
            FormulaAndroid.init(app) {}
        }
    )

    @Test
    fun `full lifecycle`() {
        val scenario = ActivityScenario.launch(TestActivity::class.java)
        scenario.recreate()
        scenario.close()
    }
}