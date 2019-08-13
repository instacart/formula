Messages are objects used to request execution of impure code. Use messages to execute operations such as
logging, database updates, firing network requests, notifying a parent and etc.


### Receive UI messages
To listen and respond to UI events, declare a callback on the `Render Model` for each type of UI event you care about.
```kotlin
data class FormRenderModel(
  val email: String,
  val onEmailChanged: (String) -> Unit,
  val onSaveSelected: () -> Unit
)
```

Use `FormulaContext.callback` and `FormulaContext.eventCallback` within `Formula.evaluate` block to create them.  
```kotlin
FormRenderModel(
  email = state.email,
  onEmailChanged = context.eventCallback { newEmail ->
    state.copy(email = newEmail).noMessages()
  },
  onSaveSelected = context.callback {
    message(userService::updateEmail, state.email)
  }
)
```

We need to return a `Transition<State>` within each of the callbacks. Transition enables you to specify a new
state and/or send 0..N messages. It uses Kotlin receiver parameter to provide you with `noMessages` and `message`
utility functions to construct a transition. Take a look at `Transition.Factory` for all available utilities.

*Note*: `FormulaContext.callback` takes no parameters and `FormulaContext.eventCallback` takes one generic parameter. 

### Sending a message 
For example, lets say we want to fire analytics event when user clicks a button.
```kotlin
class UserProfileFormula(
  val userAnalyticsService: UserAnalyticsService
) : Formula<...> {

  override fun evaluate(...): Evaluation<UserProfileRenderModel> {
    return Evaluation(
      renderModel = UserProfileRenderModel(
        onSaveSelected = context.callback {
          message(userAnalyticsService::trackSaveSelected)
        }
      )
    )
  }
}
```

The main part is the declaration within the `context.callback` block.
```kotlin
context.callback {
  // We do a state transition and declare 0..N messages that we want to execute.
}
```

### Sending messages to the parent
To pass events to the parent, we need to first define the callbacks on the `Formula.Input` class.
```kotlin
data class ItemListInput(
  val onItemSelected: (itemId: String) -> Unit
)
```

Also, lets make sure that `Input` type is declared at the top of our `formula`.
```kotlin
class ItemListFormula() : Formula<ItemListInput, ..., ...>
```

Now, we can use the the Message API and the `input` passed to us in `Formula.evaluate` to communicate with the parent.
```kotlin
override fun evaluate(
  input: ItemListInput,
  state: ..,
  context: ..
): Evaluation<...> {
  return Evaluation(
    renderModel = state.items.map { item ->
      context.key(item.id) {
        ItemRow(
          name = item.name,
          onClick = context.callback {
            // sending a message to `input.onItemSelected` with parameter `item.id`
            message(input.onItemSelected, item.id)
          }
        )
      }
    }
  )
}
```

**Note:** instead of calling `input.onItemSelected(item.id)`, we call `message(input.onItemSelected, item.id)`. This
allows formula runtime to ensure that parent is in the right state to handle the message.


### Receiving asynchronous events
Formula uses RxJava to deal with event streams. You can either use `Observable` directly or wrap it in a `RxStream`.

Usually event stream dependencies will be passed/injected through the constructor.
```kotlin
class MyFormula(private val dataObservable: Observable<MyData>): Formula<....>
```

To listen to your data observable, you need to declare a binding within `Formula.evaluate` block.
```kotlin
Evaluation(
  renderModel = ...,
  // We declare the event streams within `updates` block
  updates = context.updates {
    events("data", dataObservable) { update: MyData ->
      // the listener is always scoped to the current `state` so you can update it as part of the transition
      state.copy(myData = update).noMessages()
    }
  }
)
```

*Note:* we use a unique identifier `"data"` to make sure that internal diffing mechanism can distinguish between
different streams.


### Formula retains callbacks
Callbacks retain equality across re-evaluation (such as state changes). By default, we persist the callback in a map
where each callback is identified by its class type. There a couple of cases when this is not sufficient and you need 
to explicitly provide a unique `key`. 


#### Case 1: Declaring callbacks within a loop
For example, if you are mapping list of items and creating a callback within the `map` function.
```kotlin
// This will not work unless your list of items never changes (removal of item or position change).
ItemListRenderModel(
  items = state.items.map { item ->
    ItemRenderModel(
      name = item.name,
      onSelected = context.callback {
        // perform a transition
      }
    )
  }
)
```

To fix it, you should wrap `ItemRenderModel` creation block in `context.key` where you pass it an `item id`.
```kotlin
context.key(item.id) {
  ItemRenderModel(
    name = item.name,
    onSelected = context.callback {
      // perform a transition
    }
  )
}
```

#### Case 2: Delegating to another function 
There is an issue with callbacks when passing `FormulaContext` to another function.
Let's say you have a function that takes FormulaContext and creates a ChildRenderModel.
```kotlin
fun createChildRenderModel(context: FormulaContext<...>): ChildRenderModel {
  return ChildRenderModel(
    onClick = context.callback {}
  )
} 
```

There is no problem calling it once, but there will be key collisions if you call it multiple times:
```kotlin
RenderModel(
  // First child is created with no problem
  first = createChildRenderModel(context),
  // Calling it again will crash 
  second = createChildRenderModel(context)
)
```

To fix it, wrap `createChildRenderModel` with `context.key` block.
```kotlin
RenderModel(
  first = context.key("first") { createChildRenderModel(context) },
  second = context.key("second") { createChildRenderModel(context) }
)
```



#### More info
For each unique key we have a persisted callback instance that is kept across multiple `Formula.evaluate` calls. The
instance is disabled and removed when your `Formula` is removed or if you don't create this callback in the current
`Formula.evaluate` call.
