Event handling in Formula is based on simple callback functions. Callbacks can have zero or 
one parameter to pass data as part of the event.

### UI Events
To handle UI events, declare a function on the `Render Model` for each type of UI event you care about.
```kotlin
data class FormRenderModel(
  // A callback with no parameters
  val onSaveSelected: () -> Unit,

  // A callback where data is passed as part of the event.
  val onNameChanged: (newName: String) -> Unit
)
```

All callbacks should be created within `Formula.evaluate` block.
```kotlin
override fun evaluate(input: Input, state: State, context: FormulaContext): ... {
  return Evaluation(
    renderModel = FormRenderModel(
      // Use FormulaContext.eventCallback for callbacks that have a parameter.
      onNameChanged = context.eventCallback { newName ->
        // Use "newName" to perform a transition
        transition(state.copy(name = newName))
      },

      // Use FormulaContext.callback for callbacks with no parameters.
      onSaveSelected = context.callback {
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

To create a callback, we pass a function that returns a `Transition<State>`. Formula
uses transitions to update internal state and/or perform side-effects to other components. 
Callbacks are scoped to the current state. Any time we transition to a new state, evaluate
is called again and the callbacks are recreated.

```kotlin
// Updating state
context.eventCallback { newName: String ->
  // We use kotlin data class copy function
  // to create a new state with new name
  transition(state.copy(name = newName))
}

// Updating onSaveSelected to include validation
context.callback {
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
To pass events to the parent, first define the callbacks on the `Formula.Input` class.
```kotlin
data class ItemListInput(
  val onItemSelected: (itemId: String) -> Unit
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
    renderModel = state.items.map { item ->
      context.key(item.id) {
        ItemRow(
          name = item.name,
          onClick = context.callback {
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
  renderModel = ...,
  updates = context.updates {
    // Performs a side effect when formula is initialized
    events(Stream.onInit()) {
      transition { analytics.trackScreenOpen() }
    }

    // Performs a side effect when formula is terminated
    events(Stream.onTerminate()) {
      transition { analytics.trackClose() }
    }

    // Performs a side-effect when data changes
    events(Stream.onData(), state.itemId) {
      // This will call api.fetchItem for each unique itemId
      transition { api.fetchItem(state.itemId) }
    }
  }
)
```

### Formula retains callbacks
Callbacks retain equality across re-evaluation (such as state changes). The first time formula
requests a callback, we create it and persist it in the map. Subsequent calls will re-use this
instance. The instance is disabled and removed when your formula is removed or if you don't
request this callback within Formula.evaluate block.

By default, we generate a key for each callback based on the position in code where it is called.
There are a couple of cases when this is not sufficient and you need to explicitly provide a unique `key`.

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
