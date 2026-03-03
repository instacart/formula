The Action API provides access to formula evaluation lifecycle, enabling event streams, async 
operations, and side effects to be tied to the current evaluation state.

Actions can emit events. To declare an action and handle its events, you use `.onEvent { }` which 
registers the action with the runtime and provides a transition handler for emitted events. A 
transition can update the formula's state, execute side effects, or both.

```kotlin
override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
  return Evaluation(
    output = createOutput(state),
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

Emits `data` on initialization and re-emits whenever `data` changes:
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


## Extending Action Interface

If you need to use a different mechanism for asynchronous events, you can extend `Action` interface.
```kotlin
interface Action<Event> {
  fun start(scope: CoroutineScope, emitter: Emitter<Event>): Cancelable?
  fun key(): Any?
}
```

For example, let's say we want to track network status (I'm going to use mock network status APIs).
```kotlin
class GetNetworkStatusAction(
  val manager: NetworkStatusManager
) : Action<NetworkStatus> {

  override fun start(scope: CoroutineScope, emitter: Emitter<NetworkStatus>): Cancelable? {
    val listener = object: NetworkStatusListener {
      override fun onNetworkStatusChanged(status: NetworkStatus) {
          emitter.onEvent(status)
      }
    }

    manager.addNetworkStatusListener(listener)
    return Cancelable {
      manager.removeNetworkStatusListener(listener)
    }
  }

  override fun key(): Any? = null
}
```

We can now hook this up within our Formula:
```kotlin
class MyFormula(
    private val getNetworkStatusAction: GetNetworkStatusAction,
): Formula<Input, State, Output> {

  override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
    return Evaluation(
      actions = context.actions {
        getNetworkStatusAction.onEvent { status ->
          val newState = state.copy(isOnline = status.isOnline)
          transition(newState)
        }
      }
    )
  }
}
```
