package com.instacart.formula.internal

import com.instacart.formula.FormulaPlugins
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ClearPluginsRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                FormulaPlugins.setPlugin(null)
                try {
                    base.evaluate()
                } finally {
                    FormulaPlugins.setPlugin(null)
                }
            }
        }
    }
}