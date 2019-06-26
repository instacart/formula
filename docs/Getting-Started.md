## Getting Started
A functional reactive framework for managing state and side effects. It enables building 
deterministic, composable, testable applications.

## Core concepts
### State 
State is a Kotlin data class that contains all the necessary information to render your view. 
```kotlin
data class CounterState(val count: Int)
```

Given, this is a simple state, you could also use `Int` directly.

### Render Model
Render Model is an immutable representation of your view. It will be used to update the Android views. Typically,
it will also contain callbacks that will be invoked when user interacts with the UI.
```kotlin
data class CounterRenderModel(
  val title: String,
  val onDecrement: () -> Unit,
  val onIncrement: () -> Unit 
)
```

### Render View
Render view is responsible for taking the Render Model and applying it to the Android views.

```kotlin
class CounterRenderView(private val root: ViewGroup): RenderView<CounterRenderModel> {
    private val decrementButton: Button = root.findViewById(R.id.decrement_button)
    private val incrementButton: Button = root.findViewById(R.id.increment_button)
    private val countTextView: TextView = root.findViewById(R.id.count_text_view)

    override val renderer: Renderer<CounterRenderModel> = Renderer.create { model ->
        countTextView.setText(model.title)
        decrementButton.setOnClickListener {
            model.onDecrement()
        }
        incrementButton.setOnClickListener {
            model.onIncrement()
        }
    }
}
```

### Formula
Creating a counter widget that has increment/decrement buttons.

```kotlin
class CounterFormula : Formula<Unit, CounterState, Unit, CounterRenderModel> {

    override fun initialState(input: Unit): Int = CounterState(count = 0)

    override fun evaluate(
        input: Unit,
        state: CounterState,
        context: FormulaContext<Int, Unit>
    ): Evaluation<CounterRenderModel> {
        val count = state.count
        return Evaluation(
            renderModel = CounterRenderModel(
                count = "Count: $count",
                onDecrement = {
                    context.transition(state.copy(count = count - 1))
                },
                onIncrement = {
                    context.transition(state.copy(count = count + 1))
                }
            )
        )
    }
}
```

### Using Formula
Formula is agnostic to other layers of abstraction. It can be used within activity or a fragment. Ideally, 
it would be placed within a surface that survives configuration changes such as Android Components ViewModel.

In this example, I'll show how to connect Formula using Formula Android module. Let's first define our Activity.
```kotlin
class MyActivity : FormulaAppCompatActivity() {
  lateinit var counterRenderView: CounterRenderView

  override fun onCreate(state: Bundle?) {
    super.onCreate(state)
    setContentView(R.string.my_screen)
        
    counterRenderView = CounterRenderView(findViewById(R.id.counter))
  }
  
  fun render(model: CounterRenderView) {
    counterRenderView.renderer.render(model)
  }
}
```

Now, let's connect` MyScreenRenderFormula` to `MyActivity.render` function.
```kotlin
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        FormulaAndroid.init(this) {
            activity<MyActivity> {
                store(
                    streams = {
                        val formula = CounterFormula()
                        update(formula.state(Unit), MyActivity::render)
                    }
                )
            }
        }
    }
}
```

And that's it. To learn more, see our [Formula Android Guide](Integration.md).

### Using Formula with Android View Model
Defining `ViewModel` which runs `state` stream until `onCleared` is called.
```kotlin
class CounterViewModel(private val formula: CounterFormula) : ViewModel {
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
        
    val renderView = CounterRenderView(findViewById(R.id.counter))
    val viewModel = ViewModelProviders.of(this).get(CounterViewModel::class.java)
        
    disposables.add(viewModel.renderModels.subscribe(renderView.renderer::render))
  }
    
  override fun onDestroy() {
    disposables.clear()
    super.onDestroy()
  }
}
```

### Listening for events
We use RxJava for listening to events.
```kotlin
class MyFormula(
    private val someObservable: Observable<MyData>
) : Formula<...> {

    override fun evaluate(
        input: Unit,
        state: MyState,
        context: FormulaContext<MyState, ...>
    ): Evaluation<TimerRenderModel> {
        return Evaluation(
            // You can declaratively define what streams should run.
            updates = context.updates {
                // We use a key "data" to make sure that 
                // internal diffing mechanism can distinguish
                // between differen streams.
                events("data", someObservable) { update: MyData ->
                    // onEvent will always be scoped to the current `MyState` instance.
                    Transition(state.copy(myData = update))
                }
            },
            renderModel = ...
        )
    }
} 
```

### Side effects
It is very easy to define side-effects.
```kotlin
class UserProfileFormula(
    val userAnalyticsService: UserAnalyticsService
) : ProcessorFormula<...> {


    override fun evaluate(input: Unit, state: MyState, context: FormulaContext<...>): Evaluation<...> {
        return Evaluation(
            renderModel = state.name,
            streams = context.streams {
                // This will be invoked first time this formula runs.
                effect("view analytics") { 
                    userAnalyticsService.trackProfileView()
                }
            }
        )
    }
}
```

### Input
Input is used to pass information/data from the parent to the child. 
```kotlin
class ItemDetailFormula() : Formula<Input, ..., ..., ...> {

  data class Input(val itemId: String)

  override fun evaluate(
    input: Input,
    state: ..,
    context: ..
  ): Evaluation<...> {
    val itemId = input.itemId
    // We can use the input here to fetch the item from the repo.
  }
}
```

### Passing events to the parent / host
You can define a sealed class for outputs that the parent needs to handle.
```kotlin
sealed class ItemOutput {
    class ItemSelected(val itemId: String) : ItemOutput()
}
```

```kotlin
class ItemListFormula() : Formula<..., ..., ..., ItemOutput> {

  override fun evaluate(
    input: ..,
    state: ..,
    context: ..
  ): Evaluation<...> {
    return Evaluation(
        renderModel = state.items.map { item ->
            ItemRow(
                item = item,
                onItemSelected = {
                    // We send the `ItemSelected` event to the parent.
                    context.transition(state, ItemOutput.ItemSelected(item.id))   
                }  
            )
        }   
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

## Composing formulas
One of the primary goals when making `ProcessorFormula` was easy composability of features and widgets. Previously,
we had to listen to `Observable<ChildrenRenderModel>` and add it to our `State` and `RenderModel` classes. Also, we 
would have to use observables to pass information from parent to the child. 

```kotlin
class MainPageFormula(
    val headerFormula: HeaderFormula,
    val listFormula: ListFormula,
    val dialogFormula: DialogFormula
) : Formula<> {
    
    override fun evaluate(input: Unit, state: MyState, context: FormulaContext<...>): Evaluation<...> {
        // "context.child" returns a RenderModel 
        val listRenderModel = context.child(listFormula, createListInput(state)) { listEvent ->
            // We can perform state transition here.
        }
        
        val headerRenderModel = context.child(headerFormula, createHeaderInput(state)) { headerEvent ->
            // perform header events
        }
        
        // We can make decisions using the current `state` about 
        // what children to show
        val dialog = if (state.showDialog) {
            context.child(dialogFormula, Unit) { dialogEvent ->
                // perform dialog event
            }
        } else {
            null
        }
    
        return Evaluation(
            renderModel = MainRenderModel(
                header = headerRenderModel,
                list = listRenderModel,
                dialog = dialog
            )
        )
    }
}
```

### Diffing
Given that we recompute everything with each state change, there is an internal diffing mechanism with Formula. This
mechanism ensures that:
1. RxJava streams are only subscribed to once.
2. Side effects are only invoked once.
2. Children state is persisted across every processing pass.

### Testing
TODO:
