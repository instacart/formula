package com.instacart.formula

import com.instacart.formula.utils.TestUtils
import io.reactivex.Observable

object FromObservableWithInputFormula {
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

    fun create() = run {
        val repo = Repo()
        TestUtils.stateless { input: Input, context ->
            Evaluation(
                renderModel = Unit,
                updates = context.updates {
                    val fetchItem = RxStream.fromObservable(key = input.itemId) {
                        repo.fetchItem(input.itemId)
                    }
                    events(fetchItem) {
                        message(input.onItem, it)
                    }
                }
            )
        }
    }
}
