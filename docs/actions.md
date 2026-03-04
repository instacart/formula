Actions perform work and emit events back to the formula — observing streams, running
async operations, and responding to lifecycle. Declared within `evaluate()`, the runtime
manages their lifecycle: starting them when declared, cancelling them when removed.

```kotlin
override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
    return Evaluation(
        output = ...,
        actions = context.actions {
            // actions are declared here
            Action.fromFlow { repository.observeUser(userId) }.onEvent { user ->
                transition(state.copy(user = user))
            }
        }
    )
}
```

## Integrating Coroutine actions

For suspend functions that return a single result, use `Action.launch`:
```kotlin
Action.launch { fetchUser(userId) }.onEvent { user ->
    // Update state
    transition(state.copy(user = user))
}
```

To handle errors, use `Action.launchCatching` which wraps the result in `kotlin.Result`:
```kotlin
Action.launchCatching { fetchUser(userId) }.onEvent { result ->
    val newState = if (result.isSuccess) {
        state.copy(user = result.getOrNull())
    } else {
        state.copy(error = result.exceptionOrNull())
    }
    transition(newState)
}
```

To start and collect Flow events:
```kotlin
Action.fromFlow { repository.observeUser(userId) }.onEvent { user ->
    // Update state
    transition(state.copy(user = user))
}
```

## Responding to lifecycle events

Emits when action is initialized:
```kotlin
Action.onInit().onEvent {
    transition { analytics.trackScreenOpen() }
}
```

Emits when action is terminated (state transitions are discarded, only side effects):
```kotlin
Action.onTerminate().onEvent {
    transition { analytics.trackCloseEvent() }
}
```

Emits `data` on initialization and re-emits whenever `data` changes. This uses the key
mechanism — the data is the key, so when it changes the runtime restarts the action:
```kotlin
Action.onData(itemId).onEvent {
    transition { analytics.trackItemLoaded(itemId) }
}
```

## Action Lifecycle

`evaluate()` is called many times over a formula's lifetime. Actions need to persist across
these evaluations — the runtime starts them when first declared, keeps them running across
re-evaluations, and cancels them when no longer declared.

### Using Keys

To match actions across evaluations, the runtime uses a composite key (positional key
based on anonymous class type + optional user-provided key). During evaluation, each
action declaration looks up its key in `LifecycleCache` — if a matching action exists,
it is reused; if not, a new one is started. Actions not declared during an evaluation
are cancelled afterward.

The `key` parameter enables distinguishing between different actions. If the key changes,
the runtime cancels the old action and starts a new one.
```kotlin
Action.fromFlow(key = input.taskId) { taskRepo.fetchTask(input.taskId) }.onEvent { taskResponse ->
    transition(state.copy(task = taskResponse))
}
```

If `input.taskId` changes, the runtime cancels the old flow and starts a new one.

### Conditional Logic

Since the runtime manages actions based on what's declared in evaluation, conditional
logic controls when actions run.
```kotlin
if (state.locationTrackingEnabled) {
    Action.fromFlow { locationManager.updates() }.onEvent { event ->
        transition(state.copy(location = event.location))
    }
}
```

If `state.locationTrackingEnabled` changes from `true` to `false`, the action is no
longer declared and the runtime cancels it.
