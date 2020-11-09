package com.instacart.formula.samples.composition

import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaContext
import com.instacart.formula.StatelessFormula
import com.instacart.formula.samples.composition.item.ItemFormula

class ItemPageFormula : StatelessFormula<Unit, ItemPageRenderModel>() {
    private val itemFormula = ItemFormula()

    private val items = listOf(
        "Apple",
        "Avocado",
        "Banana",
        "Cabbage",
        "Carrot",
        "Potato",
        "Watermelon"
    )

    override fun evaluate(
        input: Unit,
        context: FormulaContext<Unit>
    ): Evaluation<ItemPageRenderModel> {
        val items = items.map {
            context.child(itemFormula, ItemFormula.Input(itemName = it))
        }
        return Evaluation(
            output = ItemPageRenderModel(
                title = "Items",
                items = items
            )
        )
    }
}