package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.test.SimpleFormula.Input
import com.instacart.formula.test.SimpleFormula.Output

interface SimpleFormula : IFormula<Input, Output> {
    data class Input(val inputId: String = "inputId")
    data class Output(val outputId: Int, val text: String)

    override fun key(input: Input): Any? {
        return if (useCustomKey) {
            CUSTOM_KEY
        } else {
            super.key(input)
        }
    }

    abstract val useCustomKey: Boolean

    companion object {
        const val CUSTOM_KEY = "simple-formula-key"
    }
}

class TestSimpleFormula(
    private val initialOutput: Output = Output(
        outputId = 0,
        text = "",
    ),
    override val useCustomKey: Boolean = true,
) : SimpleFormula {
    override val implementation = testFormula(
        initialOutput = initialOutput,
    )
}