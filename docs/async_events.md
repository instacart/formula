It's worth reading [Event Handling](events.md) section first given that there is a lot of overlap.

To show how Formula handles asynchronous events, we'll use a task app example. Let's say we have
a task repository that exposes an RxJava `Observable<List<Task>>`.
```kotlin
interface TaskRepo {
  fun getTaskList(): Observable<List<Task>>
}
```

All asynchronous events have to be declared within `Formula.evaluate` function.
```kotlin
override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
  return Evaluation(
    output = createRenderModel(state.taskList),
    // All async events need to be declared within "context.actions" block.
    actions = context.actions {
      // Convert RxJava observable to a Formula Action.
      val taskAction = RxAction.fromObservable { taskRepo.getTaskList() }
      // Tell Formula that you want to listen to these events
      taskAction.onEvent { newTaskList ->
          // update our state
          transition(state.copy(taskList = newTaskList)) 
      }
    }
  )
}

```

Formula uses a `Action` interface to define an asynchronous event producers/sources.
```kotlin
interface Action<Event> {
  fun start(send: (Event) -> Unit): Cancelable?
}
```

In this example we used an `RxAction.fromObservable` to convert from an `Observable` to a `Action` instance.

Instead of us subscribing to the observable directly, the runtime manages the subscriptions for us.
It will subscribe the first time the action is returned as part of evaluation output and unsubscribe 
when our Formula is removed or if we don't return it anymore. For example, it is okay to have conditional logic.
```kotlin
context.actions {
  if (state.locationTrackingEnabled) {
    val locationAction = RxAction.fromObservable { locationManager.updates() }
    events(locationAction) { event ->
      transition(state.copy(location = event.location))
    }
  }
}
```

If `state.locationTrackingEnabled` changes from `true` to `false`, we won't return this `Action`
anymore and the runtime will unsubscribe.

### Fetching data
Let's say we need to fetch a task that has a specific `task id`.
```kotlin
interface TaskRepo {
  fun fetchTask(taskId: String): Observable<Task>
}
```

Using `TaskRepo` directly:
```kotlin
class TaskFormula(val taskRepo: TaskRepo): Formula {

  data class Input(
    val taskId: String
  )

  data class State(
    val task: Task? = null
  )

  override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
    return Evaluation(
      actions = context.actions {
        val fetchTask = RxAction.fromObservable(key = input.taskId) { taskRepo.fetchTask(input.taskId) }
        events(fetchTask) { taskResponse ->
          transition(state.copy(task = taskResponse))
        }
      }
    )
  }
}
```

The `key` parameter enables us to distinguish between different actions. If `input.taskId` changes, we will
cancel the currently running `Action` and start a new one.

```
Note: we are not handling errors in this example. The best practice is to emit errors as data using the onNext instead
of emitting them through onError.
```

### Extending Action Interface
If you need to use a different mechanism for asynchronous events, you can extend `Action` interface.
```kotlin
interface Action<Event> {
  fun start(send: (Event) -> Unit): Cancelable?
}
```


For example, let's say we want to track network status (I'm going to use mock network status APIs).
```kotlin
class GetNetworkStatusAction(
  val manager: NetworkStatusManager
) : Action<NetworkStatus> {

  override fun start(send: (NetworkStatus) -> Unit): Cancelable? {
    val listener = object: NetworkStatusListener {
      override fun onNetworkStatusChanged(status: NetworkStatus) = send(status)
    }

    manager.addNetworkStatusListener(listener)
    return Cancelable {
      manager.removeNetworkStatusListener(listener)
    }
  }
}
```

We can now hook this up within our Formula:
```kotlin
class MyFormula(val getNetworkStatusAction: GetNetworkStatusAction): Formula<Input, State, Output> {

  override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
    return Evaluation(
      actions = context.actions {
        getNetworkStatusAction.onEvent { status ->
          val updated = status.copy(isOnline = status.isOnline)
          transition(updated)
        }
      }
    )
  }
}
```
