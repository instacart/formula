# Getting Started




## Creating render loop




## Handling User UI Actions

In our architecture, the UI doesn't have direct access to the ViewModel. The only thing that UI has access to is a render model. So to pass a user action up to our state management layer, we need to add a callback to the render model.

Example:
```kotlin
data class RenderModel(
  // Defining a callback for a user action. 
  // Usually, callback will not take any parameters. 
  val onSaveButtonClicked: () -> Unit
)

// UI can simply use this callback when setting a listener.
fun render(model: RenderModel) {
  saveButton.setOnClickListener {
    // When user clicks the button, we invoke the callback.
    model.onSaveButtonClicked()
  }
}
```

Now, the place that creates the render model will be responsible for handling the user action. Since render model creation lives in the RenderModelGenerator, that's where the callback will be created.
```kotlin
class MyRenderModelGenerator(
  // We split button click into two options:
  // 1. We want to show validation error
  // 2. We want to save user info
  val showValidationError: (String) -> Unit,
  val saveUserInfo: (UserInfo) -> Unit
) : RenderModelGenerator<State, RenderModel> {

  override fun toRenderModel(state: State): RenderModel {
    return RenderModel(
      onSaveButtonClicked = {
        // Here we have access to the current state, so we can use that
        // when escalating the action up.
        if (state.isValid) {
          saveUserInfo(state.userInfo)
        } else {
          showValidationError("Email field is empty.")
        }        
      }
    )
  }
}
```

Now, we need to hook this render model generator to our state management.
```kotlin
class MyRenderLoopFormula : RenderLoopFormula<Input, State, .., RenderModel> {

  class Input(
    val showToast: (String) -> Unit
  )

  override fun createRenderLoop(input: Input): RenderLoop<...> {
    return RenderLoop(
      renderModelGenerator = MyRenderModelGenerator(
        // There are many options here what to do with a user action. You can:
        // 1. Escalate it up to the parent by adding a callback to Input 
        // 2. Delegate to another class that was injected through the constructor
        // 3. Handle it internally by passing the event to a PublishRelay
        showValidationError = { error ->
          // Let's ask the parent to show a toast.
          input.showToast(error)
        },
        saveUserInfo = { info ->

        }
      )
    )
  }  
}
```

## Handling User Action Internally

This is continuation on the above section. We want to trigger save user info request and update the UI according to request state. 

```kotlin
// Let's define the repository abstraction for saving user info
class SaveUserInfoRepo {
  fun saveUserInfo(info: UserInfo): Flowable<ICLce<UserInfoResponse>>
} 

class MyRenderLoopFormula(
  val repo: SaveUserInfoRepo
) : RenderLoopFormula<Input, State, .., RenderModel> {

  override fun createRenderLoop(input: Input): RenderLoop<...> {
    // We create a relay here. This allows us to combine callbacks with RxJava.
    val saveUserInfoRelay = PublishRelay.create<UserInfo>()

    val saveUserInfoReducer = saveUserInfoRelay
      // We need to convert relay to flowable
      .toFlowable(BackpressureStrategy.LATEST)
      // We use switch map to cancel previous computation.
      // This means if new save info action happens, we 
      // cancel the previous request and create a new one. 
      .switchMap { info ->
        repo.saveUserInfo(info)
      }
      .map { responseEvent ->
        // We should create a reduce a.k.a. state transformation function here.
        // How to accomplish that will be shown in a different example.
      }
      
    return ICRenderLoop(
      // Since we only have a single reducer here, we pass it directly.
      reducers = saveUserInfoReducer,
      renderModelGenerator = MyRenderModelGenerator(
        saveUserInfo = { info ->
          // We pass the action to the relay
          saveUserInfoRelay.accept(info)
        }
      )
    )
  }  
}
```

## Reducers (AKA State transformations)

We use immutable data classes to represent state. Instead of mutating properties when an action happens,
we create a new version of the state class. Let's use a a simple list of tasks as an example to display how this actually looks in code. Imagine next to each task there is a button that user can click to delete that task.

Let's define our data classes:
```kotlin
data class Task(val id: String, val text: String)

data class State(val tasks: List<Task>)
```


We need to define a transformation for the delete action. Transformation is a function that takes the current state as a parameter and returns new state. This is also called a reduce function. 
```kotlin
// Note the signature of deleteTask function: this function itself is 
// not the transformation. This function only creates the transformation
// function. You then need to pass the state object to execute it. 
fun deleteTask(taskId: String): (State) -> State {
  return { state ->
    // Remove the task from the list
    val updatedTaskList = state.tasks.filter { task ->
      task.id != taskId
    }
    
    // Create a new state without the deleted task 
    state.copy(tasks = updatedTaskList)
  }
}

// To manually invoke this function
deleteTask("task-id")(currentState)
```

## What are Effects

Effects are message objects used to request the execution of impure code. For example, we want to notify the backend that the user has deleted a task. Performing this inside of a reducer function would introduce unpredictability and make the function impure. Operations such as network requests, reading / writing to disk and updating global state are known as side-effects. Instead of performing impure code here, we emit a message and let the caller decide how to execute it. 

Usually we define effects as a sealed Kotlin class. 
```kotlin
sealed class Effect {
  data class SendDeleteTaskRequest(val taskId: String): Effect()
}
```


To emit effects as part of the reducer function, we have to introduce a new type
```kotlin
data class Next<State, Effect>(
  val state: State,
  // We allow multiple effect emissions.
  val effects: Set<Effect>
)
```

We also have to change the reducer functions signature

```kotlin
// The reducer function now returns Next<State, Effect> instead of just State
fun deleteTask(taskId: String): (State) -> Next<State, Effect> {
  return { state ->
    // Remove the task from the list
    val updatedTaskList = state.tasks.filter { task ->
      task.id != taskId
    }
        
    Next(
      // Create a new state without the deleted task 
      state = state.copy(tasks = updatedTaskList),
      // Emit a message to send a delete task request.
      effects = setOf(Effect.SendDeleteTaskRequest(taskId))
    )
  }
}
```

## Keeping all reducers together

For better testability, we keep all reducers inside of a class that extends NextReducers. This also provides us with utility methods to construct the reducer.
```kotlin
class MyStateReducers : NextReducers<State, Effect> {

    // We don't have to specify the return type of this method.
    fun deleteTask(taskId: String) = reduce { state ->
      // toNextWithEffects() helps construct Next<State, Effect> for us
      state.copy(/* perform update*/).toNextWithEffects(
        Effect.SendDeleteTaskRequest(taskId)
      )
    }

    fun insertTask(task: Task) = withoutEfects { state ->
      // withoutEffects allows you to just emit state.
      state
    }
}

```


Note: if you don't have any effects, you can use Kotlin Unit type to indicate that. 

## Combining reducers into a State Loop

The reason why we have a function that creates a function is to have a common type that all transformations fulfill. This enables us to combine different state reducers into a single collection. In our case, we want to a combine them into single RxJava stream.
```kotlin
// User action streams
val deleteTaskActions: Flowable<String> = ...
val insertTaskActions: Flowable<Task> = ...

// Initialize the class containing all state reducers
val reducers = MyStateReducers()

// Combine all transformations into a single stream
val transformations: Flowable<(State) -> Next<State, Effect>> = Flowable.merge(
  deleteTaskActions.map { taskId ->
    // Create a delete reducer 
    reducers.deleteTask(taskId)
  },
  insertTaskActions.map { task ->
    // Create an insert task reducer
    reducers.insertTask(task)
  }
)

// To execute all these transformations, we will use RxJava scan operator
// Note: no transformations will be executed until something subscribes to
// this stream 
val initialEmission = Next(
  state = State(tasks = emptyList()),
  effects = emptyList() // no initial effects
)

val stateChanges: Flowable<State> = 
  transformations.scan(initialEmission) { current, transformation ->
    // When a new transformation is emitted, we take the current state
    // and transform it. The resulting state is emitted to the subscribers
    // of this state change stream. 
    transformation(current.state)
  }
  .doOnNext { event ->
    // Let's handle effects here 
    event.effects.forEach {
      // we do a pattern match here
      when (it) {
        is SendDeleteTaskRequest -> // trigger network request?
      }
    }
  }
  // Only emit state
  .map { event ->
    event.state
  }
  // We avoid emitting duplicate state updates.
  .distinctUntilChanged()
```

There is a lot of integration logic here that can be reused here. To have a better API and remove code duplication, we have a StateLoop class.
```kotlin
// We only pass the important properties here
// while removing all the integration noise.
val loop = ICStateLoop(
  initialState = State(tasks = emptyList()),
  reducers = transformations,
  onEffect = { effect ->
    // we do a pattern match here
    when (effect) {
      is SendDeleteTaskRequest -> // trigger network request?
    }
  }
)

val stateChanges: Flowable<State> = loop.createLoop()
stateChanges.subscribe()
```

## When to use Render Loop

If you understand StateLoop, you practically know RenderLoop. The only difference is that RenderLoop expects a RenderModel and a RenderModelGenerator. So, if you have a view that needs to be updated based on state changes, use RenderLoop. For non UI state management, use StateLoop.
```kotlin
val loop = RenderLoop(
  initialState = State(tasks = emptyList()),
  reducers = transformations,
  onEffect = { effect ->
    // we do a pattern match here
    when (effect) {
      is SendDeleteTaskRequest -> // trigger network request?
    }
  },
  renderModelGenerator = MyRenderModelGenerator()
)

val renderModelChanges = loop.createRenderModelStream()
renderModelChanges.subscribe()
```

## Composing Render models

TODO

## Annotation processor

TODO




