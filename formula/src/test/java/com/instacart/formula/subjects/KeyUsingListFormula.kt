package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.test.TestableRuntime

class KeyUsingListFormula :
    Formula<KeyUsingListFormula.Input, KeyUsingListFormula.State, KeyUsingListFormula.Output>() {
    companion object {
        fun test(runtime: TestableRuntime, items: List<String>) = runtime.test(KeyUsingListFormula(), Input(items))
    }

    data class State(val items: List<String>)

    data class Output(val items: List<ItemRenderModel>)

    data class ItemRenderModel(
        val item: String,
        val onDeleteSelected: Listener<Unit>
    )

    data class Input(val items: List<String>)

    override fun initialState(input: Input) = State(input.items)

    override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {

        val items = state.items.map { itemName ->
            context.key(itemName) {
                ItemRenderModel(itemName, onDeleteSelected = context.onEvent {
                    transition(state.copy(items = state.items.minus(itemName)))
                })
            }
        }

        return Evaluation(output = Output(items))
    }
}
