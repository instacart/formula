Let's say your feature needs to fetch an item. Formula uses RxJava
to define asynchronous operations. We will assume that you are familiar
with `Retrofit` library.

```kotlin
class FetchItemStream(private val itemApi: RetrofitItemApi) : RxStream<FetchItemRxStream.Request, Item> {
    data class Request(val itemId: String)

    override fun observable(input: Request): Observable<Item> {
        return itemApi.fetchItem(input.itemId)
    }
}
```

Now, let's define our Formula.
```kotlin
class ItemDetailFormula(
    private val fetchItem: FetchItemStream
) : Formula<Input, State, Unit, RenderMode> {

    data class Input(val itemId: String)

    data class State(
        val fetchItemRequest: FetchItemRxStream.Request? = null,
        val item: Item? = null
    )

    override fun initialState(input: Input): State {
        return State(fetchItemRequest = FetchItemRxStream.Request(input.itemId))
    }

    override fun evaluate(input: Input, state: State, context: FormulaContext<State, Unit>): Evaluation<RenderModel> {
        return Evaluation(
            updates = context.updates {
                if (state.fetchItemRequest != null) {
                    events(fetchItem, state.fetchItemRequest) { item ->
                        state.copy(fetchItemRequest = null, item = item).noMessages()
                    }
                }
            }
        )
    }
}
```

We define `fetchItemRequest` on our `State` class and use this property to decide if we want to
fetch an item. Once `item` response arrives, we set the `fetchItemRequest` to `null`.
```
Note: we are not handling errors in this example. The best practice is to emit errors as data using the onNext instead
of emitting them through onError.
```
