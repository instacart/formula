State is an internal object managed by a formula. It is typically an immutable data class —
transitions provide a new state instance which triggers re-evaluation. State is created by 
`initialState(input)` when the formula starts and used within `evaluate()` to define actions, 
child formulas, and build output.

```kotlin
class CounterFormula : Formula<Unit, CounterFormula.State, CounterOutput>() {

    data class State(
        val count: Int,
    )

    override fun initialState(input: Unit) = State(count = 0)

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<CounterOutput> {
        return Evaluation(
            output = CounterOutput(
                count = state.count,
                onIncrement = context.onEvent {
                    transition(state.copy(count = state.count + 1))
                },
            )
        )
    }
}
```

## Initial State

`initialState(input)` is called once when the formula first starts. It can use input to
seed values.

```kotlin
override fun initialState(input: Input) = State(
    itemId = input.itemId,
    item = null,
)
```

It is also called again if `key(input)` changes — covered in [Formula Key](#formula-key).

## Transitions

State changes happen via the `transition()` DSL within event listeners and action handlers.

Update state — triggers re-evaluation:

```kotlin
context.onEvent { newName: String ->
    transition(state.copy(name = newName))
}
```

Side effect only — no state change:

```kotlin
context.callback {
    transition {
        analytics.trackSaveClicked()
    }
}
```

Both — update state and execute a side effect:

```kotlin
context.callback {
    transition(state.copy(saved = true)) {
        userService.save(state.data)
    }
}
```

Do nothing:

```kotlin
context.callback {
    none()
}
```

Side effects execute after state changes — the formula is in the correct state before
any effect runs.

## Responding to Input Changes

`onInputChanged` is called before `evaluate()` when input changes. It returns the new
state to use for evaluation.

```kotlin
override fun onInputChanged(
    oldInput: Input,
    input: Input,
    state: State,
): State {
    return if (oldInput.itemId != input.itemId) {
        state.copy(item = null)
    } else {
        state
    }
}
```

Use this to reset or adjust state when a key piece of input changes.

## Formula Key

`key(input)` defines the formula's identity. If the return value changes, the formula
restarts — state is discarded and `initialState` is called again.

```kotlin
override fun key(input: Input) = input.itemId
```

Use this when a formula should fully reset when what it represents changes — for example,
an item detail screen that should start fresh when navigating to a different item.
