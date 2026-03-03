Input is an immutable data class that defines a formula's contract with its host — the
component that runs it. A host can be a ViewModel running the formula via `runAsStateFlow`,
or a parent formula using `context.child()`. Input specifies what data the host provides
and what events the formula reports back. When input changes, the formula re-evaluates.

```kotlin
class ItemFormula : Formula<Input, State, Output>() {

  data class Input(
    val itemId: String,
    val onItemNotFound: () -> Unit,
  )
}
```

## Using Input in Evaluate

Input is available within `evaluate()` via the `input` property. When input changes,
`evaluate()` is called again with the new values. Input data can be used to create output,
drive actions, and determine what the formula does.

To report events back to the host, call the callbacks defined on input within a transition.

```kotlin
override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
  return Evaluation(
    output = ...,
    actions = context.actions {
      Action.launchCatching(key = input.itemId) {
        repo.fetchItem(input.itemId)
      }.onEvent { result ->
        if (result.isSuccess) {
          transition(state.copy(item = result.getOrNull()))
        } else {
          // Notify host that item was not found
          transition { input.onItemNotFound() }
        }
      }
    }
  )
}
```

If `input.itemId` changes, the formula re-evaluates — the action's key changes so the
runtime cancels the old fetch and starts a new one.

## Hosting as a StateFlow

A formula can be run as a `StateFlow` using `runAsStateFlow`. The host creates input
directly and collects output.

```kotlin
val output: StateFlow<Output> = formula.runAsStateFlow(
  scope = viewModelScope,
  input = Input(
    itemId = "1",
    onItemNotFound = { navigateBack() },
  ),
)
```

## Hosting Within Another Formula

A parent formula hosts a child via `context.child()`, building input from its own state
and callbacks.

```kotlin
val childOutput = context.child(
  formula = itemFormula,
  input = ItemFormula.Input(
    itemId = state.selectedId,
    onItemNotFound = context.callback {
      transition(state.copy(selectedId = null))
    },
  ),
)
```

See [Composition](composition.md) for details on child lifecycle, listener stability,
and formula keys.
