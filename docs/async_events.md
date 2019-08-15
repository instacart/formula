Formula provides a declarative API for handling asynchronous events. All the events we care about should
be declared within `context.updates` block and returned as part of `Evaluation.updates`.
```kotlin
class MyFormula : Formula {

  override fun evaluate(...): Evaluation<RenderModel> {
    return Evaluation(
      renderModel = ...,
      updates = context.updates {
        // all event declarations go here.
      }
    )
  }
}
```

Asynchronous events are very similar to UI events. For each type of event, we declare a callback that takes a message
and creates a transition.
```kotlin
context.updates {
  events(/* stream */) { event ->
    // perform a transition
  }
}
```

### Using RxJava
Let's say we have a task repository that exposes an RxJava `Observable<List<Task>>`.
```kotlin
context.updates {
  val taskStream = RxStream.fromObservable { repository.tasks() }
  events(taskStream) { tasks ->
    transition(state.copy(tasks = tasks))
  }
}
```

We use `RxStream.fromObservable` to wrap the `Observable`. Instead of us subscribing to the observable directly,
the runtime manages the subscriptions for us. It will subscribe the first time `events` is called and unsubscribe
when our Formula is removed or if we don't return it anymore. For example, it is okay to have conditional logic
within `context.updates` block.
```kotlin
context.updates {
  if (state.locationTrackingEnabled) {
    events(RxStream.fromObservable { locationManager.updates() }) { event ->
      transition(state.copy(location = event.location))
    }
  }
}
```

If `state.locationTrackingEnabled` changes from `true` to `false`, we won't return this `Stream` anymore and the runtime
will unsubscribe.

### Fetching data
Let's say we need to fetch an item that has a specific `item id`.
```kotlin
interface ItemApi {
  fun fetchItem(itemId: String): Observable<Item>
}
```

Using `ItemApi` directly:
```kotlin
class ItemDetailFormula(val itemApi: ItemApi): Formula {

  data class Input(
    val itemId: String
  )

  data class State(
    val item: Item? = null
  )

  override fun evaluate(
    input: Input,
    state: State,
    context: FormulaContext<..>
  ): Evaluation<RenderModel> {
    return Evaluation(
      updates = context.updates {
        val fetchItemStream = RxStream.withParameter(itemApi::fetchItem)
        events(fetchItemStream, input.itemId) { itemResponse ->
          transition(state.copy(item = itemResponse))
        }
      }
    )
  }
}
```

We can also extend `RxStream`:
```kotlin
class FetchItemStream(val itemApi: ItemApi): RxStream<String, Item> {

  override fun observable(parameter: String): Observable<Item> {
    return itemApi.fetchItem(parameter)
  }
}
```

And then update the formula to:
```kotlin
class ItemDetailFormula(val fetchItem: FetchItemStream): Formula {

  override fun evaluate(
    input: Input,
    state: State,
    context: FormulaContext<..>
  ): Evaluation<RenderModel> {
    return Evaluation(
      updates = context.updates {
        events(fetchItemStream, input.itemId) { itemResponse ->
          transition(state.copy(item = itemResponse))
        }
      }
    )
  }
}
```

Formula will call `Stream.start` with `input.itemId`. If `itemId` changes, Formula will cancel the previous instance
of the `Stream` and call `Stream.start` with new `itemId.`

```
Note: we are not handling errors in this example. The best practice is to emit errors as data using the onNext instead
of emitting them through onError.
```

### Not using RxJava
If you need to use a different mechanism for asynchronous events, you can extend `Stream` interface.
```kotlin
interface Stream<Parameter, Message> {
  fun start(parameter: Parameter, send: (Message) -> Unit): Cancelable?
}
```


For example, let's say we want to track network status (I'm going to use mock network status APIs).
```kotlin
class NetworkStatusStream(
  val manager: NetworkStatusManager
) : Stream<Unit, NetworkStatus> {

  override fun start(parameter: Unit, send: (NetworkStatus) -> Unit): Cancelable? {
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

  override fun evaluate(.., context: FormulaContext): Evaluation {
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