Formula supports composition — a parent formula can run child formulas via
`context.child(formula, input)` within `evaluate()`, passing data and callbacks down
through Input and receiving the child's Output.

```kotlin
override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
  val listOutput = context.child(formula = itemListFormula, input = ...)
  return Evaluation(
    output = Output(list = listOutput)
  )
}
```

## Passing Input to Children

### Input and Re-evaluation

The parent creates a new Input for each child every time `evaluate()` runs. Evaluation
is triggered when the parent's own input changes, its state changes, or a child's output
changes. This is how data flows to children — when the parent's state updates, the child
receives new Input reflecting those changes.

```kotlin
val listOutput = context.child(
  formula = itemListFormula,
  input = ItemListFormula.Input(
    items = state.items,
    onItemSelected = context.onEvent<ItemId> { itemId ->
      transition(state.copy(selectedItemId = itemId))
    }
  )
)
```

### Input Equality

A child only re-evaluates if its input actually changed. Formula compares the new input
to the previous one using `equals()`. If they're equal, the child skips re-evaluation
and `context.child()` returns the previously computed output.

### Listener Stability

When a parent creates a lambda inline in `evaluate()`, a new instance is created every
evaluation. Since lambdas use identity equality, the child's input equality check always
fails — causing unnecessary re-evaluation even when nothing actually changed.
`context.onEvent` and `context.callback` solve this by maintaining the same instance
across parent re-evaluations — the runtime matches them via composite key in
`LifecycleCache` and updates the internal transition, but the instance stays the same.

## Child Lifecycle

Child formulas declared via `context.child()` are started by the runtime and persist
across parent re-evaluations — the child keeps its state as long as it remains declared.
A child only re-evaluates if its input changed or state changed within its own hierarchy.

The runtime needs to match children across evaluations to maintain their state. It uses
a composite key of formula type + `formula.key(input)` to identify each child. As long
as the key stays the same, the child persists with its existing state. If `key(input)`
changes, the runtime treats it as a different child — terminates the old instance and
starts a new one with `initialState`.

### Conditional Children

Since children are declared in `evaluate()`, conditional logic controls their existence.

```kotlin
val dialog = if (state.showDialog) {
  context.child(formula = dialogFormula, input = Unit)
} else {
  null
}
```

When the condition becomes false, the child is terminated — its state is lost and its
actions are cancelled. When the condition becomes true again, a fresh child starts
with `initialState`.

### Formula Key

Override `formula.key(input)` when a parent runs multiple instances of the same formula
type. For example, rendering a list of items where each item is managed by the same formula:

```kotlin
state.items.map { item ->
  context.child(formula = itemFormula, input = ItemFormula.Input(itemId = item.id))
}
```

Without a key, the runtime cannot distinguish between instances. Override `key(input)` to
provide a unique identity:

```kotlin
override fun key(input: Input) = input.itemId
```

When the key changes, the runtime terminates the old instance and starts a new one with
fresh `initialState`.
