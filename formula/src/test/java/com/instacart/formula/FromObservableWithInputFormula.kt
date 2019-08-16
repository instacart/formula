package com.instacart.formula

import io.reactivex.Observable

class FromObservableWithInputFormula : StatelessFormula<FromObservableWithInputFormula.Input, Unit>() {
    data class Input(
        val itemId: String,
        val onItem: (Item) -> Unit
    )

    data class Item(val itemId: String)

    class Repo {
        fun fetchItem(itemId: String): Observable<Item> {
            return Observable.just(Item(itemId))
        }
    }

    private val repo = Repo()

    override fun evaluate(input: Input, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(
            renderModel = Unit,
            updates = context.updates {
                events(RxStream.fromObservable(repo::fetchItem), input.itemId) {
                    message(input.onItem, it)
                }
            }
        )
    }
}
