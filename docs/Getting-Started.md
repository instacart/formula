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
                title = "Count: $count",
                onDecrement = context.callback("decrement") {
                    transition(state.copy(count = count - 1))
                },
                onIncrement = context.callback("increment") {
                    transition(state.copy(count = count + 1))
                }
            )
        )
    }
}
```

### Using Formula
Formula is agnostic to other layers of abstraction. It can be used within activity or a fragment. Ideally, 
it would be placed within a surface that survives configuration changes such as Android Components ViewModel.

In this example, I'll show how to connect Formula using Formula Android module. For using Formula with AndroidX ViewModel 
take a look at [AndroidX Guide](Using-Android-View-Model.md).
 
Let's first define our Activity.
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
            // You can declaratively define what event streams should run.
            updates = context.updates {
                // We use a key "data" to make sure that 
                // internal diffing mechanism can distinguish
                // between differen streams.
                events("data", someObservable) { update: MyData ->
                    // onEvent will always be scoped to the current `MyState` instance.
                    transition(state.copy(myData = update))
                }
            },
            renderModel = ...
        )
    }
} 
```

### Side effects
It is very easy to emit side-effects.
```kotlin
class UserProfileFormula(
    val userAnalyticsService: UserAnalyticsService
) : ProcessorFormula<...> {

    override fun evaluate(input: Unit, state: MyState, context: FormulaContext<...>): Evaluation<...> {
        return Evaluation(
            renderModel = UserProfileRenderModel(
                onSaveSelected = context.callback("save selected") {
                    sideEffect("save selected analytics") {
                        userAnalyticsService.trackSaveSelected()
                    }
                }
            )
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
                onItemSelected = context.callback("item selected: ${item.id}") {
                    // We send the `ItemSelected` event to the parent.
                    output(ItemOutput.ItemSelected(item.id))   
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

### Callbacks
To create a UI event callback use `context.callback` or `context.eventCallback` within `Formula.evaluate` method.
```kotlin
CounterRenderModel(
    onIncrement = context.callback("increment") {
        transition(state + 1)
    }
)
```

For each callback you must provide a key such as `"increment"` here that is unique within this `Formula`. This means
that if you have a list of items and you want to have a click listener for each item, you need to make sure that each
callback key is unique by using an item id or something similar.

```kotlin
ItemListRenderModel(
    items = state.items.map { item ->
        ItemRenderModel(
            name = item.name,
            onSelected = context.callback("item selection: ${item.id}") {
                // perform a transition
            }
    }
)
```

For each unique key we have a persisted callback instance that is kept across multiple `Formula.evaluate` calls. The
instance is disabled and removed when your `Formula` is removed or if you don't create this callback in the current
`Formula.evaluate` call.


### Testing
To simplify testing your Formulas, you can use `formula-test` module.
```
testImplementation 'com.github.instacart:formula-test:{latest_version}'
```

Testing render model output
```kotlin
val subject = MyFormula().test().renderModel {
    assertThat(this.name).isEqualTo("my name")
}
```

If your Formula has children, you can replace their render model output
```kotlin
val subject = MyFormula().test {
    // Note: we are using mockito to mock ChildRenderModel, you could also manually create it.
    child(MyChildFormula::class, mock<ChildRenderModel>())
}
```

You can now emit children output to your Formula
```kotlin
subject.output(MyChildFormula::class, MyChildFormula.Output("property"))
```

To inspect the input that was passed to the child
```kotlin
subject.childInput(MyChildFormula::class) {
    assertThat(this.property).isEqualTo("property")
}
```

## FAQ

### Threading
The state management should be initialized on the main thread and all the transitions should also happen on the main thread. You 
will get the following exception if that is not the case.
```
Caused by: java.lang.IllegalStateException: Only thread that created it can trigger transitions. Expected: main, Was: Network 1
```

### Transition already happened.
After each transition, formula is re-evaluated and new event listeners are created. If you use an old listener
you will see the following exception.
```
Caused by: java.lang.IllegalStateException: Transition already happened. This is using old transition callback: $it.
```
