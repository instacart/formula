Input is a Kotlin data class used to pass data and event callbacks to the Formula instance. Let's say we need to 
pass an item id to `ItemDetailFormula`. 
```kotlin
class ItemDetailFormula() : Formula<ItemDetailFormula.Input, ..., ...> {

  // Input declaration
  data class Input(val itemId: String)

  // Use input to initialize state
  override fun initialState(input: Input): State = ...
  
  // Respond to Input changes.
  override fun onInputChanged(oldInput: Input, input: Input, state: State): State {
      // We can compare old and new inputs and create
      // a new state before `Formula.evaluate` is called.
      return state 
  }
  
  // Using input within evaluate block
  override fun evaluate(
    input: Input,
    state: ..,
    context: ..
  ): Evaluation<...> {
    val itemId = input.itemId
    // We can use the input here to fetch the item from the repo.
  }
}
```

To pass the input to `ItemDetailFormula`
```kotlin
val itemDetailFormula: ItemDetailFormula = ...
itemDetailFormula
  .toObservable(ItemDetailFormula.Input(itemId = "1"))
  .subscribe { renderModel ->
    
  }
```

You could also pass an `Observable<ItemDetailFormula.Input>`
```kotlin
val itemDetailInput: Observable<ItemDetailFormula.Input> = ...
itemDetailFormula
  .toObservable(itemDetailInput)
  .subscribe { renderModel ->
    
  }
```

## Equality
Formula uses input equality to determine if it should re-evaluate. A parent can cause 
a child formula to re-evaluate by changing the input it passes. This will also trigger
`Formula.onInputChanged` callback on the child formula.

This is a desired behavior as we do want the child to react when the data that we pass changes.
```kotlin
data class ItemInput(
    val itemId: String
)
```

Making `Input` a data class and passing data as part of its properties makes it easy to reason
about its equality. In some cases though, we want to pass objects that don't have property based
equality such as callback functions or observables. 

### Maintaining callback equality
In many cases we want to pass callbacks to listen to formula events. 

```kotlin
data class ItemListInput(
    val onItemSelected: (Item) -> Unit
)
```

Formula provides an easy way to maintain callback equality. Within your parent formula, 
you can use `FormulaContext` to instantiate callbacks by using:

- `FormulaContext.onEvent`

**Don't:** Don't instantiate functions within `Formula.evaluate`.
```kotlin
override fun evaluate(...) {
    val itemListInput = ItemListInput(
        onItemSelected = {
            analytics.track("item_selected")
        }
    )
}
```

**Do:** Use `eventCallback`
```kotlin
override fun evaluate(...) {
    val itemListInput = ItemListInput(
        onItemSelected = context.onEvent { _ ->
            transition { analytics.track("item_selected") }
        }
    )
}
```

**Do:** Use already constructed callbacks
```kotlin
// Router is constructed outside of the "evaluate" function block
val router: ItemRouter = ...

override fun evaluate(...): ... {
    val itemListInput = ItemListInput(
        onItemSelected = router::onItemSelected
    )
}
```

**Do:** Delegate to parent input directly
```kotlin
override fun evaluate(input: Input, ...): ... {
    val itemListInput = ItemListInput(
        onItemSelected = input.onItemSelected
    )
}
```

### Passing observables
Observables have identity equality which make maintaining input equality a bit tricky.
```kotlin
data class MyInput(
  val eventObservable: Observable<Event>
)
```

**Don't:** create a new observable within `Formula.evaluate`
```kotlin
override fun evaluate(...) {
    val input = MyInput(
        eventObservable = relay.map { Event() }
    )
}
```

**Do:** create observable outside of `Formula.evaluate`

```kotlin
private val eventObservable = relay.map { Event() }

override fun evaluate(...) {
    val input = MyInput(
        eventObservable = eventObservable
    )
}
```

**Do:** use State to instantiate observable once
```kotlin
data class State(
    val eventObservable: Observable<Event>
)

override fun initialState(input: Input) = State(
  eventObservable = input.eventObservable.map { Event() }
)

override fun evaluate(...) {
    val input = MyInput(
        eventObservable = state.eventObservable
    )
}
```

**Don't:** pass data observables
```kotlin
data class Input(
    val dataObservable: Observable<Data>
)
```

**Do:** pass data directly
```kotlin
data class Input(
    val data: Data?
)
```