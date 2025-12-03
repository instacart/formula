package com.instacart.formula.r8.runner

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.instacart.formula.FormulaPlugins
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.FormulaError
import com.instacart.formula.plugin.Plugin

/**
 * Custom test runner that initializes FormulaPlugins before tests run.
 *
 * This runner is configured in build.gradle.kts as the testInstrumentationRunner.
 * It sets up the Formula plugin to use the main thread dispatcher for tests,
 * which makes tests synchronous and deterministic.
 */
class FormulaTestRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle?) {
        // Initialize FormulaPlugins before any tests run
        FormulaPlugins.setPlugin(createTestPlugin())
        super.onCreate(arguments)
    }

    private fun createTestPlugin(): Plugin {
        return object : Plugin {
            override fun onError(error: FormulaError) {
                throw error.error
            }
        }
    }
}
