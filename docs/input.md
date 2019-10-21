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
  .start(ItemDetailFormula.Input(itemId = "1"))
  .subscribe { renderModel ->
    
  }
```

You could also pass an `Observable<ItemDetailFormula.Input>`
```kotlin
val itemDetailInput: Observable<ItemDetailFormula.Input> = ...
itemDetailFormula
  .start(itemDetailInput)
  .subscribe { renderModel ->
    
  }
```
