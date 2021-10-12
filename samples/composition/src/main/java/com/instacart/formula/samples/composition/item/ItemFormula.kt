package com.instacart.formula.samples.composition.item

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import kotlin.math.max

class ItemFormula : Formula<ItemFormula.Input, ItemFormula.State, ItemRenderModel>() {
    data class Input(
        val itemName: String
    )

    data class State(
        val quantity: Int
    )

    override fun key(input: Input): Any? = input.itemName

    override fun initialState(input: Input): State = State(quantity = 0)

    override fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<ItemRenderModel> {
        return Evaluation(
            output = ItemRenderModel(
                name = input.itemName,
                displayQuantity = "${state.quantity}",
                isDecrementEnabled = state.quantity > 0,
                onDecrement = context.onEvent {
                    val newQuantity = max(0, state.quantity - 1)
                    transition(state.copy(quantity = newQuantity))
                },
                onIncrement = context.onEvent {
                    transition(state.copy(quantity = state.quantity + 1))
                }
            )
        )
    }
}