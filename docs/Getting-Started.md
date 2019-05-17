# Getting Started
A functional reactive framework for managing state and side effects based on RxJava. It enables building 
deterministic, composable, testable applications.

## Core concepts
### State 
State is a Kotlin data class that contains all the necessary information to render your view. 
```kotlin
data class MyScreenState(
  val userInfoRequest: Lce<UserInfo>,
  val isSaving: Boolean = false
)
```

Note: for info about `Lce`, please check [this article](https://tech.instacart.com/lce-modeling-data-loading-in-rxjava-b798ac98d80).  

### Render Model
Render Model is an immutable representation of your view. It will be used to update the Android views. Typically,
it will also contain callbacks that will be invoked when user interacts with the UI.
```kotlin
data class FooterButtonRenderModel(
  val title: String,
  val isEnabled: Boolean,
  val onClick: () -> Unit
)
```

### Render View
Render view is responsible for taking the Render Model and applying it to the Android views.

```kotlin
class FooterButtonRenderView(private val root: View) : RenderView<FooterButtonRenderModel> {
  private val footerButton: Button = root.findViewById(R.id.footer_button)
  
  override val renderer: Renderer<FooterButtonRenderModel> = Renderer.create { model ->
    footerButton.text = model.title
    footerButton.isEnabled = model.isEnabled
    footerButton.setOnClickListener {
      model.onClick()
    }
  } 
}
```

### Render Model Generator
Render Model Generator takes a State and creates a Render Model from it. 
```kotlin
class MyScreenRenderModelGenerator(
  private val onSaveUserInfoSelected: () -> Unit
) : RenderModelGenerator<MyScreenState, FooterButtonRenderModel> {
  override fun toRenderModel(state: MyScreenState): FooterButtonRenderModel {
    return FooterButtonRenderModel(
      title = "Save User Info",
      isEnabled = state.userInfoRequest.isData() && !state.isSaving,
      onClick = onSaveUserInfoSelected
    )
  }
}
```

### Reducers 
Reducers class defines all the possible State transformations. It defines methods that take an event object and 
return a transformation. Instead of of mutating properties when an event happens, we create a new version of the
State class. To accomplish that, we use data class `copy` method.

```kotlin
class MyScreenReducers : Reducers<MyScreenState, Unit>() {

  fun onUserInfoRequest(event: Lce<UserInfo>) = withoutEffects { state ->
    state.copy(userInfoRequest = event)
  }
    
  fun onSaveUserInfoRequest(event: Lce<SaveUserInfoResponse>) = withoutEffects { state ->
    state.copy(isSaving = event.isLoading())
  }
}
```

### Render Formula
Render Formula is responsible for state management. It combines various RxJava event streams and maps them to 
state transformations.
 
```kotlin
class MyScreenRenderFormula(
  private val userRepo: UserRepo
) : RenderFormula<Unit, MyScreenState, FooterButtonRenderModel, Unit> {

  override fun createRenderLoop(input: Unit): RenderLoop<MyScreenState, Unit, FooterButtonRenderModel> {
    val reducers = MyScreenReducers()
       
    val userInfoRequestChanges = userRepo.fetchUserInfo().map(reducers::onUserInfoRequest)
       
    // We use a RxRelay library to turn user events into an RxJava stream
    val saveUserInfoRelay = PublishRelay.create<Unit>()
    val saveUserInfoChanges = saveUserInfoRelay
      .switchMap { userRepo.saveUserInfo() }
      .map(reducers::onSaveUserInfoRequest)
        
    return RenderLoop(
      initialState = MyScreenState(
        userInfoRequest = Lce.loading()
      ),
      reducers = Observable.merge(
        userInfoRequestChanges,
        saveUserInfoChanges
      ),
      renderModelGenerator = MyScreeenRenderModelGenerator(
        onSaveUserInfoSelected = {
          saveUserInfoRelay.accept(Unit)
        }
      )
    )
  }
}
```

### Using Render Formula
Render formula is agnostic to other layers of abstraction. It can be used within activity or a fragment. Ideally, 
it would be placed within a surface that survives configuration changes such as Android Components ViewModel.

In this example, we keep the stream running until the view model is cleared.
```kotlin
class MyViewModel(private val formula: MyScreenRenderFormula) : ViewModel {
  private val disposables = CompositeDisposable()
    
  val renderModels = formula.state(Unit).replay(1).apply {
    connect { disposables.add(it) }
  }

  override fun onCleared() {
    super.onCleared()
    disposables.clear()
  }
}
```

In our activity, we then subscribe to the Render Model changes and pass them to the Render View.
```kotlin
class MyActivity : AppCompatActivity() {
  private val disposables = CompositeDisposable()

  override fun onCreate(state: Bundle?) {
    super.onCreate(state)
    setContentView(R.string.my_screen)
        
    val renderView = FooterButtonRenderView(findViewById(R.id.activity_content))
    val viewModel = ViewModelProviders.of(this).get(MyViewModel::class.java)
        
    disposables.add(viewModel.renderModels.subscribe(renderView.renderer::render))
  }
    
  override fun onDestroy() {
    disposables.clear()
    super.onDestroy()
  }
}
```

Formula also comes with a module that provides declarative API to connect state management to Android Fragments. To learn more, see our [Integration Guide](Integration.md).

### Input
Input is used to pass information when creating a state stream. Typically it will contain data necessary to initialize the state streams,
and callbacks for events that the parent should be aware of. 
```kotlin
class ItemDetailRenderFormula() : RenderFormula<Input, ..., ..., ...> {

  class Input(
    val itemId: String,
    val onItemDeleted: () -> Unit
  )

  override fun createRenderLoop(input: Input): RenderLoop<...> {
    // We can use the input here to fetch the item from the repo.
    // We can also notify the parent when the item is deleted using input.onItemDeleted()
  }
}
```

### Effects
Effects are message objects used to request the execution of impure code. Operations such as firing a network request,
reading / writing to disk, navigation or updating global state are considered side-effects. Instead of performing those
operations within the Reducers class, we return the Effect object and let the caller execute the side effect.
 
Typically, we define all possible side-effects as a sealed Kotlin class.
```kotlin
sealed class MyScreenEffect {
  data class ShowErrorModal(val errorMessage: String): MyScreenEffect()
  data class Exit(val savedUserInfo: Boolean): MyScreenEffect()
}
```

To emit effect 
```kotlin
class MyScreenReducers : Reducers<..., MyScreenEffect>() {

  fun onSaveUserInfoRequest(event: Lce<SaveUserInfoResponse>) = reduce {
    val updated = it.copy(isSaving = event.isLoading())
        
    // Check if there are side effects
    val effect = if (event.isData()) {
      // We want to close the screen when user info is saved.
      MyScreenEffect.Exit(savedUserInfo = true)
    } else if (event.isError()) {
      MyScreenEffect.ShowErrorModal(errorMessage = event.error.getMessage())
    } else {
      null
    }
        
    updated.withOptionalEffect(effect)
  }
}
```

Within the Render Formula, we can decide how to handle the effects.
```kotlin
class MyScreenRenderFormula : RenderFormula<..., ..., MyScreenEffect, ...> {
    
    override fun createRenderLoop(input: ...): RenderLoop<..., MyScreenEffect, ...> {
        return RenderLoop(
            ...,
            onEffect = { effect ->
                // We decide here how to execute the effect. We can
                // 1. bubble it to the parent using the callbacks passed by the Input object
                // 2. handle it internally by passing it to a RxRelay or RxJava Subject
                when(effect) {
                    is ShowErrorModal -> {
                    
                    }
                    is Exit -> {
                        
                    }
                }
            }
        )
    }
}
```

## Handling User UI Actions
To handle user UI actions, we set listeners on Android Views and delegate to the callbacks on the Render Model. 
```kotlin
data class MyRenderModel(
  // Defining a callback for a user action. 
  // Usually, callback will not take any parameters. 
  val onSaveButtonClicked: () -> Unit
)

class MyRenderView(...) : RenderView<MyRenderModel> {
  val saveButton: TextView = ...
    
  override val renderer: Renderer<MyRenderModel> = Renderer.create { model ->
    // We just set a click listener and delegate to the callback on the Render Model.
    saveButton.setOnClickListener {
      model.onSaveButtonClicked()
    }
  }  
}
```

The Render Model creation will be scoped to the current state object, so we can use it to decide how we should bubble it up.
```kotlin
class MyRenderModelGenerator(
  // We splitting save button click into two options
  private val showValidationError: (String) -> Unit,
  private val saveUserInfo: (UserInfo) -> Unit
) : RenderModelGenerator<State, MyRenderModel> {

  override fun toRenderModel(state: State): MyRenderModel {
    return RenderModel(
      onSaveButtonClicked = {
        // We use the current state to decide which callback to invoke
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

We will provide those callbacks in the Render Formula.
```kotlin
class MyRenderFormula : RenderFormula<Input, State, .., RenderModel> {

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
          // Let's bubble up this event to the parent of this formula using the Input class
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
  fun saveUserInfo(info: UserInfo): Observable<Lce<UserInfoResponse>>
} 

class MyRenderFormula(
  private val repo: SaveUserInfoRepo
) : RenderFormula<Input, State, .., RenderModel> {

  override fun createRenderLoop(input: Input): RenderLoop<...> {
    // We use a RxRelay library to turn user events into an RxJava stream. You could also use RxJava subjects.
    val saveUserInfoRelay = PublishRelay.create<UserInfo>()

    val saveUserInfoReducer = saveUserInfoRelay
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
      
    return RenderLoop(
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

## Composing Render models
Render Models are meant to be composable. You can build bigger Render Models from smaller Render Models.
```kotlin
data class CheckboxRenderModel(
  val text: String,
  val isChecked: Boolean,
  val onToggle: () -> Unit
)

data class NotificationSettingsRenderModel(
  val messagePushNotification: CheckboxRenderModel,
  val promotionalPushNotifications: CheckboxRenderModel,
  val marketingEmailNotifications: CheckboxRenderModel,
  val saveSettingsButton: FooterButtonRenderModel
)
```

You can also do the same in your Render View layer.
```kotlin
class CheckboxRenderView(private val root: View) : RenderView<CheckboxRenderModel> {
  private val checkbox: Checkbox = root.findViewById(R.id.checkbox)
  
  override val renderer: Renderer<CheckboxRenderModel> = Renderer.create { model ->
    checkbox.text = model.title
    checkbox.isChecked = model.isChecked
    checkbox.setOnCheckedListener {
      model.onToggle()
    }
  } 
}

class NotificationSettingsRenderView(private val root: View) : RenderView<NotificationSettingsRenderModel> {
  private val messagePushNotification = CheckboxRenderView(root.findViewById(R.id.message_push_checkbox))
  private val promotionalPushNotifications = CheckboxRenderView(root.findViewById(R.id.promotional_push_checkbox))
  private val marketingEmailNotifications = CheckboxRenderView(root.findViewById(R.id.marketing_email_checkbox))
  private val saveButton = FooterButtonRenderView(root.findViewById(R.id.save_button))
  
  override val renderer: Renderer<NotificationSettingsRenderModel> = Renderer.create { model ->
    messagePushNotification.renderer.render(model.messagePushNotification)
    promotionalPushNotifications.renderer.render(model.promotionalPushNotifications)
    marketingEmailNotifications.renderer.render(model.marketingEmailNotifications)
    saveButton.renderer.render(model.saveSettingsButton)
  } 
}
```

## Annotation processor
There is an optional annotation processor to remove some of the boiler plate code. It is primarily driven by
`@State` annotation placed on the State data class.
```kotlin
@State(reducers = MyScreenReducers::class)
data class MyScreenState(
  val userInfoRequest: Lce<UserInfo>,
  val isSaving: Boolean = false
)

class MyScreenReducers : Reducers<MyScreenState, Unit>() {

  fun onUserInfoRequest(event: Lce<UserInfo>) = withoutEffects { state ->
    state.copy(userInfoRequest = event)
  }
    
  fun onSaveUserInfoRequest(event: Lce<SaveUserInfoResponse>) = withoutEffects { state ->
    state.copy(isSaving = event.isLoading())
  }
}
```


This will generate an Events class that handles binding the RxJava streams to the appropriate event methods defined 
in the Reducers class.
```kotlin
@Generated
class MyScreenStateEvents(private val reducers: MyScreenReducers) {

  fun bind(
    onUserInfoRequest: Observable<Lce<UserInfo>>,
    onSaveUserInfoRequest: Observable<Lce<SaveUserInfoResponse>>
  ): Observable<...> {
    val list = ArrayList<Observable<...>>()
    list.add(onUserInfoRequest.map(reducers::onUserInfoRequest))
    list.add(onSaveUserInfoRequest.map(reducers::onSaveUserInfoRequest))
    return Observable.merge(list)
  }
}
``` 

You can use this Events class within the Render Formula.
```kotlin
class MyScreenRenderFormula(
  private val userRepo: UserRepo
) : RenderFormula<Unit, MyScreenState, FooterButtonRenderModel, Unit> {

  override fun createRenderLoop(input: Unit): RenderLoop<MyScreenState, Unit, FooterButtonRenderModel> {
    val events = MyScreenStateEvents(MyScreenReducers())
        
    return RenderLoop(
      ...,
      reducers = events.bind(
        onUserInfoRequest = userRepo.fetchUserInfo(),
        onSaveUserInfoRequest = ... 
      )
    )
  }
}
```

### Using @ExportedProperty
Exported Property annotation generates a transformation that updates a single property.
```kotlin
@State(reducers = MyScreenReducers::class)
data class MyScreenState(
  @ExportedProperty val userInfoRequest: Lce<UserInfo>,
  ...
)
```

The generated code is equivalent to this snippet, so we can delete it completely from MyScreenReducers.
```kotlin
fun onUserInfoRequest(event: Lce<UserInfo>) = withoutEffects { state ->
  state.copy(userInfoRequest = event)
}
```

### Using isDirectInput (@ExportedProperty)
Direct input generates a method on Events class that can directly cause a state transformation.
```kotlin
@State
data class NotificationSettingsState(
  @ExportedProperty(isDirectInput = true) val isPushNotificationsEnabled: Boolean
)
```

Now you can update this property through the NotificationSettingsStateEvents class.
```kotlin
class NotificationSettingsRenderModelGenerator(
  private val events: NotificationSettingsStateEvents
): RenderModelGenerator<NotificationSettingsState, CheckboxRenderModel> {

  override fun toRenderModel(state: NotificationSettingsState): CheckboxRenderModel {
    return CheckboxRenderModel(
      text = "Push notifications",
      isChecked = state.isPushNotificationsEnabled,
      onToggle = {
        // We are invoking a generated method.
        events.onIsPushNotificationsEnabledChanged(!state.isPushNotificationEnabled)
      }
    )
  }
}
```

### Using @DirectInput
Direct Input annotation works in similar manner as `@ExportedProperty(isDirectInput = true)` except you mark
transformation methods within the Reducers class instead of properties on State class.
```kotlin
class TaskListReducers : Reducers<TaskListState, Unit>() {

  @DirectInput fun onTaskCompleted(taskId: String) = withoutEffects { state ->
    state.tasks.map {
      if (it.id == taskId) {
        it.copy(completed = true)
      } else {
        it
      }
    } 
  }
}

@State(reducers = TaskListReducers::class)
data class TaskListState(
  val tasks: List<Task>
)
```

This will generated a method `TaskListStateEvents.onTaskCompleted`. You can now invoke this method on user input
and it will update the state.


## Navigation
TODO


