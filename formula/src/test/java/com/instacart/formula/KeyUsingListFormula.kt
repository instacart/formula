package com.instacart.formula

import com.instacart.formula.test.TestableRuntime

class KeyUsingListFormula : Formula<KeyUsingListFormula.Input, KeyUsingListFormula.State, KeyUsingListFormula.Output> {
    companion object {
        fun test(runtime: TestableRuntime, items: List<String>) = runtime.test(KeyUsingListFormula(), Input(items))
    }

    data class State(val items: List<String>)

    data class Output(val items: List<ItemRenderModel>)

    data class ItemRenderModel(
        val item: String,
        val onDeleteSelected: () -> Unit
    )

    data class Input(val items: List<String>)

    override fun initialState(input: Input) = State(input.items)

    override fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<Output> {

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
