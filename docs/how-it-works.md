# How It Works
Formula has a similar mental model to Jetpack Compose — it has a declarative `evaluate()`
function which describes what should exist right now. It produces output and actions based
on current input, state, and child outputs. It re-runs when **inputs**, **state**, or 
**child hierarchy state changes**. For every change, the root formula re-evaluates all the 
way down to the changed area.

## Transitions
Formula provides a mechanism to declare event listeners which respond to events by
producing transitions. A transition can update the formula's state and optionally execute
side effects. When state changes, it propagates up to the root, which re-runs `evaluate()`
down to the changed area.

## Maintaining components across evaluations
`evaluate()` is called many times over a formula's lifetime. Actions, child formulas, and
listeners need to persist across these evaluations — actions should keep running, children
should keep their state, and listeners should maintain stable identity. Formula uses a
key-based mechanism to match components across evaluations and manage their lifecycle.

### Action Lifecycle
Actions declared during evaluation are started by the runtime. If an action is no longer
declared in a subsequent evaluation, the runtime cancels it. If an action's key changes,
the runtime cancels the old one and starts a new one.

To match actions across evaluations, the runtime uses a composite key (positional key
based on anonymous class type + optional user-provided key). During evaluation, each
action declaration looks up its key in `LifecycleCache` — if a matching action exists,
it is reused; if not, a new one is started. Actions not declared during an evaluation
are cancelled afterward.

```kotlin
  // Conditional: action only runs when userId is set.
// If userId becomes null, the runtime cancels it.
if (state.userId != null) {
    // Key: if userId changes, runtime cancels the old action and starts a new one.
    Action.fromFlow(key = state.userId) {
        repository.observeUser(state.userId)
    }.onEvent { user ->
        transition(state.copy(user = user))
    }
}
```

### Child Formula Lifecycle
Child formulas declared via `context.child()` during evaluation are started by the runtime.
If a child is no longer declared in a subsequent evaluation, the runtime terminates it.

To match child formulas across evaluations, the runtime uses a composite key of formula
type + `formula.key(input)`. If `key(input)` changes, the runtime terminates the old
instance and starts a new one with `initialState`.

A child only re-evaluates if its input changed or state changed within its hierarchy.
Otherwise, `context.child()` returns the previously computed output.

```kotlin
val headerOutput = context.child(headerFormula, HeaderInput(title = state.title))

val dialog = if (state.showDialog) {
    context.child(dialogFormula, Unit)
} else {
    null
}
```

### Listener Lifecycle
Listeners created via `context.onEvent` and `context.callback` maintain the same instance
across evaluations. This is important because listeners are placed on outputs and passed
to child formulas via input — if a new instance were created every evaluation, equality
checks would always fail, triggering unnecessary re-evaluation.

Like actions, listeners are matched across evaluations using a composite key (positional
key + optional user-provided key). The listener's internal transition is updated each
evaluation to reference the latest state, but the instance itself stays the same.

Listeners not requested during an evaluation are disabled and cleaned up.

### Key Collisions and Indexing
When multiple components share the same positional key — for example, creating listeners
inside a `map` loop — Formula disambiguates them by index. The first occurrence gets
index 0, the second gets index 1, and so on.

This is fragile: if item order changes or items are removed, indexes shift and components
can get matched to wrong instances. To avoid this, provide stable, explicit keys such as
a unique identifier for the object.
