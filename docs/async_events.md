It's worth reading [Event Handling](events.md) section first given that there is a lot of overlap.

To show how Formula handles asynchronous events, we'll use a task app example. Let's say we have
a task repository that exposes an RxJava `Observable<List<Task>>`.
```kotlin
interface TaskRepo {
  fun tasks(): Observable<List<Task>>
}
```

All asynchronous events have to be declared within `Formula.evaluate` function.
```kotlin
override fun evaluate(input: Input, state: State, context: FormulaContext): ... {
  return Evaluation(
    renderModel = createRenderModel(state.taskList),
    // All async events need to be declared within "context.updates" block.
    updates = context.updates {
      // Convert RxJava observable to a Formula Stream.
      val taskStream = RxStream.fromObservable(taskRepo::tasks)
      // Tell Formula that you want to listen to these events
      events(taskStream) { newTaskList ->
        // update our state
        transition(state.copy(taskList = newTaskList))
      }
    }
  )
}

```

Formula uses a `Stream` interface to define an asynchronous event producers/sources.
```kotlin
interface Stream<Message> {
  fun start(send: (Message) -> Unit): Cancelable?
}
```

In this example we used an `RxStream.fromObservable` to convert from an `Observable` to a `Stream` instance.

Instead of us subscribing to the observable/stream directly, the runtime manages the subscriptions for us.
It will subscribe the first time `events` is called and unsubscribe when our Formula is removed or
if we don't return it anymore. For example, it is okay to have conditional logic.
```kotlin
context.updates {
  if (state.locationTrackingEnabled) {
    val locationStream = RxStream.fromObservable { locationManager.updates() }
    events(locationStream) { event ->
      transition(state.copy(location = event.location))
    }
  }
}
```

If `state.locationTrackingEnabled` changes from `true` to `false`, we won't return this `Stream`
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

  override fun evaluate(
    input: Input,
    state: State,
    context: FormulaContext<..>
  ): Evaluation<RenderModel> {
    return Evaluation(
      updates = context.updates {
        val fetchTask = RxStream.fromObservable(key = input.taskId) { taskRepo.fetchTask(input.taskId) }
        events(fetchTask) { taskResponse ->
          transition(state.copy(task = taskResponse))
        }
      }
    )
  }
}
```

The `key` parameter enables us to distinguish between different streams. If `input.taskId` changes, we will
cancel the currently running `Stream` and start a new one.

```
Note: we are not handling errors in this example. The best practice is to emit errors as data using the onNext instead
of emitting them through onError.
```

### Extending Stream Interface
If you need to use a different mechanism for asynchronous events, you can extend `Stream` interface.
```kotlin
interface Stream<Message> {
  fun start(send: (Message) -> Unit): Cancelable?
}
```


For example, let's say we want to track network status (I'm going to use mock network status APIs).
```kotlin
class NetworkStatusStream(
  val manager: NetworkStatusManager
) : Stream<NetworkStatus> {

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
class MyFormula(val networkStatus: NetworkStatusStream): Formula {

  override fun evaluate(input: .., state: .., context: FormulaContext): .. {
    return Evaluation(
      updates = context.updates {
        events(networkStatus) { status ->
          val updated = status.copy(isOnline = status.isOnline)
          transition(updated)
        }
      }
    )
  }
}
```
