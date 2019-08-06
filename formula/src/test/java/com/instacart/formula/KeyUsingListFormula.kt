package com.instacart.formula

import com.instacart.formula.test.test

class KeyUsingListFormula : Formula<KeyUsingListFormula.Input, KeyUsingListFormula.State, KeyUsingListFormula.RenderModel> {
    companion object {
        fun test(items: List<String>) = KeyUsingListFormula().test(Input(items))
    }

    data class State(val items: List<String>)

    data class RenderModel(val items: List<ItemRenderModel>)

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
    ): Evaluation<RenderModel> {

        val items = state.items.map { itemName ->
            context.key(itemName) {
                ItemRenderModel(itemName, onDeleteSelected = context.callback {
                    state.copy(items = state.items.minus(itemName)).noMessages()
                })
            }
        }

        return Evaluation(renderModel = RenderModel(items))
    }
}
