package com.instacart.formula

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A rule to initialize FormulaAndroid.
 */
class TestFormulaRule(
    private val initFormula: (Application) -> Unit,
    private val cleanUp: () -> Unit = {}
) : TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        initializeFormula()
    }

    override fun finished(description: Description?) {
        FormulaAndroid.reset()
        cleanUp()
    }

    fun fakeProcessDeath() {
        FormulaAndroid.reset()
        cleanUp()
        initializeFormula()
    }

    private fun initializeFormula() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        initFormula(context)
    }
}
