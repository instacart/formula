Output is an immutable data class returned from `evaluate()`. It passes data out to the
consumer and defines events the consumer can trigger back.

```kotlin
data class CounterOutput(
  val count: Int,
  val onIncrement: () -> Unit,
  val onDecrement: () -> Unit,
)
```

Output is created within `evaluate()` from the current state, input, and child outputs.
It is recreated every time `evaluate()` runs — triggered by input, state, or child
hierarchy state changes.

```kotlin
override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
  return Evaluation(
    output = CounterOutput(
      count = state.count,
      onIncrement = context.onEvent {
        transition(state.copy(count = state.count + 1))
      },
      onDecrement = context.onEvent {
        transition(state.copy(count = state.count - 1))
      }
    )
  )
}
```

Every time `evaluate()` runs, a new output is produced. This makes Formula reactive —
state changes automatically propagate as updated output to the consumer.

## Event Listeners and Transitions

When an event carries data, use `context.onEvent`:

```kotlin
FormOutput(
  onNameChanged = context.onEvent { newName ->
    transition(state.copy(name = newName))
  }
)
```

When no data is needed, use `context.callback`:

```kotlin
FormOutput(
  onSaveSelected = context.callback {
    transition { userService.updateName(state.name) }
  }
)
```

Both trigger a transition when called — updating state, executing side effects, or both.

## Listener Lifecycle

Output is recreated every evaluation, but listeners need stable identity. If a listener
were a new instance every time, output equality would always fail — even when nothing
actually changed.

`context.onEvent` and `context.callback` maintain the same instance across evaluations.
The runtime matches them via composite key (positional key based on anonymous class type
+ optional user-provided key) in `LifecycleCache`. The internal transition is updated
each evaluation to reference the latest state, but the instance itself stays the same.

Listeners not requested during an evaluation are disabled and cleaned up.

### Listener Keying

The default positional key works when each listener has a unique call site. When multiple
listeners share the same call site — such as creating listeners inside a loop — provide
an explicit key.

```kotlin
state.items.map { item ->
  ItemOutput(
    name = item.name,
    onSelected = context.onEvent(key = item.id) {
      transition(state.copy(selectedItem = item))
    }
  )
}
```

## Composing Outputs

Outputs are data classes — they naturally compose by nesting smaller outputs as fields.
Design your Output to reflect the structure of your feature.

```kotlin
data class OrderDetailOutput(
  val orderSummary: OrderSummaryOutput,
  val itemList: ItemListOutput,
  val paymentInfo: PaymentInfoOutput,
  val cancelOrder: ButtonOutput?,
)
```

## Loading and Error States in Output

Output can represent different states such as loading, content, and error. The
[LCE library](https://github.com/Laimiux/lce/) provides type-safe wrappers for this.

```kotlin
data class ItemDetailOutput(
  val item: UCT<Item>,
)
```

`UCT<Content>` is an alias for `Lce<Unit, Content, Throwable>` — it can be loading,
content, or error.
