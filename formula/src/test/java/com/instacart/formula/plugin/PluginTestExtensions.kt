package com.instacart.formula.plugin

import com.instacart.formula.FormulaPlugins


fun <T: Plugin?> withPlugin(plugin: T, continuation: (T) -> Unit) {
    FormulaPlugins.setPlugin(plugin)

    try {
        continuation(plugin)
    } finally {
        FormulaPlugins.setPlugin(null)
    }
}