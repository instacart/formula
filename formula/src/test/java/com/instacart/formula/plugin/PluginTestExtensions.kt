package com.instacart.formula.plugin

import com.instacart.formula.FormulaPlugins


fun withPlugin(plugin: Plugin?, continuation: () -> Unit) {
    FormulaPlugins.setPlugin(plugin)

    try {
        continuation()
    } finally {
        FormulaPlugins.setPlugin(null)
    }
}