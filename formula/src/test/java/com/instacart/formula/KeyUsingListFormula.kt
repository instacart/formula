package com.instacart.formula

import com.instacart.formula.test.test
import com.instacart.formula.utils.TestUtils

object KeyUsingListFormula {
    data class State(val items: List<String>)

    data class RenderModel(val items: List<ItemRenderModel>)

    data class ItemRenderModel(
        val item: String,
        val onDeleteSelected: () -> Unit
    )

    data class Input(val items: List<String>)

    fun test(items: List<String>) = create().test(Input(items))

    private fun create() = TestUtils
        .lazyState(initialState = { input: Input -> State(input.items) }) { input, state, context ->
            val items = state.items.map { itemName ->
                context.key(itemName) {
                    ItemRenderModel(itemName, onDeleteSelected = context.callback {
                        state.copy(items = state.items.minus(itemName)).noMessages()
                    })
                }
            }

            Evaluation(renderModel = RenderModel(items))
        }
}
