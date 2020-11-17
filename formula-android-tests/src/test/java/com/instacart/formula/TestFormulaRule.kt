package com.instacart.formula

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A rule to initialize FormulaAndroid.
 */
class TestFormulaRule(
    private val initFormula: (Application) -> Unit,
    private val cleanUp: () -> Unit = {}
) : TestWatcher() {
    private val errors = mutableListOf<Throwable>()

    override fun apply(base: Statement, description: Description): Statement {
        RxJavaPlugins.reset()
        RxJavaPlugins.setErrorHandler { errors.add(it) }

        try {
            val result = super.apply(base, description)
            assertNoErrors()
            return result
        } finally {
            errors.clear()
            RxJavaPlugins.reset()
        }
    }

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

    private fun assertNoErrors() {
        Truth.assertThat(errors).isEmpty()
    }
}
