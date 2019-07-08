package com.instacart.formula.internal

import com.instacart.formula.IFormula
import com.instacart.formula.Logger

class FormulaLogger(
    @PublishedApi internal val prefix: String,
    @PublishedApi internal val name: String,
    @PublishedApi internal val key: String,
    @PublishedApi internal val delegate: Logger?
) {

    constructor(
        formula: IFormula<*, *>,
        key: Any?,
        delegate: Logger?,
        prefix: String = "",
    ): this(
        prefix = prefix,
        name = formula::class.simpleName ?: "",
        key = key?.toString() ?: "",
        delegate = delegate,
    )

    inline fun log(message: () -> String) {
        if (delegate != null) {
            // TODO: figure good way to log nested relationships
            val eventMessage = buildString {
                if (prefix.isNotBlank()) {
                    append(prefix)
                    append(' ')
                }
                append(name)
                if (key.isNotBlank()) {
                    append("-$key")
                }
                append(":")
                append(message())
            }

            delegate.logEvent(eventMessage)
        }
    }

    fun childLogger(formula: IFormula<*, *>, key: Any?): FormulaLogger {
        val childPrefix = if (prefix.isEmpty()) {
            "|-"
        } else {
            "$prefix-"
        }
        return FormulaLogger(formula, key, delegate, childPrefix)
    }
}