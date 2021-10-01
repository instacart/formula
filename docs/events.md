Event handling in Formula is based on event listeners. A listener is just a function
that is called when an event happens and could be described by a simple `(Event) -> Unit` 
function type. We pass listeners to other parts of the codebase such as the view layer by
adding the listener to the `Render Model`.

### UI Events
To handle UI events, declare a `Listener` on the `Render Model` for each type of UI event you care about.
```kotlin
data class FormRenderModel(
  // A listener where event contains no information (We use kotlin.Unit type).  
  val onSaveSelected: Listener<Unit>,

  // A listener where name string is passed as part of the event.
  val onNameChanged: Listener<String>,
)
```

To create a listener use `FormulaContext.onEvent`. Note: All listeners should be created within `Formula.evaluate` block.
```kotlin
override fun evaluate(input: Input, state: State, context: FormulaContext): ... {
  return Evaluation(
    output = FormRenderModel(
      onNameChanged = context.onEvent<String> { newName ->
        // Use "newName" to perform a transition
        transition(state.copy(name = newName))
      },
      onSaveSelected = context.onEvent<Unit> {
        // No state change, performing side-effects.
        transition {
          userService.updateName(state.name)  
          analytics.trackNameUpdated(state.name)
        }
      }
    )
  )
}
```

This example is dense, but it shows almost every kind of scenario. Let's go over it.

To create a listener, we pass a function that returns a `Transition<State>`. Formula
uses transitions to update internal state and/or perform side-effects to other components. 
Listeners are scoped to the current state. Any time we transition to a new state, evaluate
is called again and the listeners are recreated.

```kotlin
// Updating state
context.onEvent { newName: String ->
  // We use kotlin data class copy function
  // to create a new state with new name
  transition(state.copy(name = newName))
}

// Updating onSaveSelected to include validation
context.onEvent {
  if (state.name.isBlank()) {
    // A transition which performs a side-effect.
    transition {
      input.showNotification("Name cannot be empty!")
    }
  } else {
    // No state change, performing side-effects as part of the transition
    transition {
      userService.updateName(state.name)
      analytics.trackNameUpdated(state.name)
    }
  }
}
```

To ensure safe execution, all side-effects should be performed within `transition {}` block which
will be executed after the state change is performed.

### Sending messages to the parent
To pass events to the parent, first define the listener on the `Formula.Input` class.
```kotlin
data class ItemListInput(
  val onItemSelected: Listener<ItemId>,
)
```

Also, lets make sure that `Input` type is declared at the top of our `formula`.
```kotlin
class ItemListFormula() : Formula<ItemListInput, ..., ...>
```

Now, we can use the `input` passed to us in `Formula.evaluate` to communicate with the parent.
```kotlin
override fun evaluate(input: ItemListInput, state, context): ... {
  return Evaluation(
    output = state.items.map { item ->
      context.key(item.id) {
        ItemRow(
          name = item.name,
          onClick = context.onEvent {
            // Notifying parent that item was selected.
            transition {
              input.onItemSelected(item.id)
            }
          }
        )
      }
    }
  )
}
```

### Formula events
There are a few events that every formula can listen to and respond.

```kotlin
Evaluation(
  output = ...,
  updates = context.updates {
    // Performs a side effect when formula is initialized
    Stream.onInit().onEvent {
      transition { analytics.trackScreenOpen() }
    }

    // Performs a side effect when formula is terminated
    Stream.onTerminate().onEvent {
      transition { analytics.trackClose() }
    }

    // Performs a side-effect when data changes
    Stream.onData(state.itemId).onEvent {
      // This will call api.fetchItem for each unique itemId
      transition { api.fetchItem(state.itemId) }
    }
  }
)
```

### Formula retains listeners
Listeners retain equality across re-evaluation (such as state changes). The first time formula
requests a listener, we create it and persist it in the map. Subsequent calls will re-use this
instance. The instance is disabled and removed when your formula is removed or if you don't
request this listener within Formula.evaluate block.

By default, we generate a key for each listener based on the listener type. Usually, this
is an anonymous class which is associated with the position in code where it is called. There are
a couple of cases when this is not sufficient and you need to explicitly provide a unique `key`.

#### Case 1: Declaring listeners within a loop
For example, if you are mapping list of items and creating a listener within the `map` function.
```kotlin
// This will not work unless your list of items never changes (removal of item or position change).
ItemListRenderModel(
  items = state.items.map { item ->
    ItemRenderModel(
      name = item.name,
      onSelected = context.onEvent {
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
    onSelected = context.onEvent {
      // perform a transition
    }
  )
}
```

#### Case 2: Delegating to another function
There is an issue with listeners when passing `FormulaContext` to another function.
Let's say you have a function that takes FormulaContext and creates a ChildRenderModel.
```kotlin
fun createChildRenderModel(context: FormulaContext<...>): ChildRenderModel {
  return ChildRenderModel(
    onClick = context.onEvent {}
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
