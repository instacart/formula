package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot

class NullableStateFormula : Formula<Unit, String?, NullableStateFormula.Output>() {

    data class Output(
        val state: String?,
        val updateState: (String?) -> Unit
    )

    override fun initialState(input: Unit): String? = null

    override fun Snapshot<Unit, String?>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                state = state,
                updateState = context.onEvent<String?> {
                    transition(it)
                }
            )
        )
    }
}