package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.rxjava3.RxAction
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

    override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            actions = context.actions {
                val fetchItem = RxAction.fromObservable(key = input.itemId) {
                    repo.fetchItem(input.itemId)
                }
                events(fetchItem) {
                    transition { input.onItem(it) }
                }
            }
        )
    }
}
