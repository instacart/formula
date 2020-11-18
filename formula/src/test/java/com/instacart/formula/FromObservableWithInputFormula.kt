package com.instacart.formula

import com.instacart.formula.rxjava3.RxStream
import io.reactivex.rxjava3.core.Observable

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
            output = Unit,
            updates = context.updates {
                val fetchItem = RxStream.fromObservable(key = input.itemId) {
                    repo.fetchItem(input.itemId)
                }
                events(fetchItem) {
                    transition { input.onItem(it) }
                }
            }
        )
    }
}
