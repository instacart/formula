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
class CounterFormula : Formula<Unit, CounterState, CounterRenderModel> {

    override fun initialState(input: Unit): Int = CounterState(count = 0)

    override fun evaluate(
        input: Unit,
        state: CounterState,
        context: FormulaContext<Int>
    ): Evaluation<CounterRenderModel> {
        val count = state.count
        return Evaluation(
            renderModel = CounterRenderModel(
                title = "Count: $count",
                onDecrement = context.callback {
                    state.copy(count = count - 1).transition()
                },
                onIncrement = context.callback {
                    state.copy(count = count + 1).transition()
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
Formula uses RxJava to deal with event streams. You can either use `Observable` directly or wrap it in a `RxStream`.

Usually event stream dependencies will be passed/injected through the constructor.
```kotlin
class MyFormula(private val dataObservable: Observable<MyData>): Formula<....>
```

To listen to your data observable, you need to declare a binding within `Formula.evaluate` block.
```kotlin
Evaluation(
    renderModel ...,
    // We declare the event streams within `updates` block
    updates = context.updates {
        events("data", dataObservable) { update: MyData ->
            // the listener is always scoped to the current `state` so you can update it as part of the transition
            state.copy(myData = update).transition()
        }
    }
)
```

*Note:* we use a unique identifier `"data"` to make sure that internal diffing mechanism can distinguish between
different streams.

### Messages
Messages are objects used to request execution of impure code. Use messages to execute operations such as
logging, database updates, firing network requests, notifying a parent and etc.

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

### Input
Input is used to pass information/data from the parent to the child. 
```kotlin
class ItemDetailFormula() : Formula<Input, ..., ...> {

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

### Passing events to the parent
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
You can pass other formulas through the constructor
```kotlin
class MainPageFormula(
    val headerFormula: HeaderFormula,
    val listFormula: ListFormula,
    val dialogFormula: DialogFormula
) : Formula<> 
```

Use `FormulaContext.child` within `Formula.evaluate` to hook them up.
```kotlin
val listRenderModel = context
    .child(listFormula)
    .input {
        ListInput(
            items = state.items,
            onItemSelected = context.eventCallback {
                // you can respond to child event
            }
    }
```
 
Here is a more complete example:
```kotlin
class MainPageFormula(
    val headerFormula: HeaderFormula,
    val listFormula: ListFormula,
    val dialogFormula: DialogFormula
) : Formula<> {
    
    override fun evaluate(input: Unit, state: MyState, context: FormulaContext<...>): Evaluation<...> {
        // "context.child" returns a RenderModel 
        val listRenderModel = context
            .child(listFormula)
            .input { createListInput(state) }

        val headerRenderModel = context
            .child(headerFormula)
            .input { createHeaderInput(state) }

        // We can make decisions using the current `state` about 
        // what children to show
        val dialog = if (state.showDialog) {
            context
                .child(dialogFormula)
                .input(Unit)
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
2. Children state is persisted across every processing pass.

### Callbacks
To create a UI event callback use `context.callback` or `context.eventCallback` within `Formula.evaluate` method.
```kotlin
CounterRenderModel(
    onIncrement = context.callback {
        transition(state + 1)
    }
)
```

Callbacks retain equality across re-evaluation (such as state changes). By default, we persist the callback in a map
where each callback is identified by an incremented integer id. In some cases incremented id is not sufficient and
you will need to explicitly provide a unique `key`. There a two common situations: when callback is declared in a conditional
statement or when it is declared in a list iteration.

Conditional callback issue:
```kotlin
// THIS WILL NOT WORK
val optionalRenderModel = if (state.showChild) {
    ChildRenderModel(
        // Incremented integer id doesn't work in this case
        onClick = context.callback {
            transition()
        }
    )
} else {
    null
}
```

To fix it, you just need to add a unique key
```kotlin
onClick = context.callback("child on click") {

}
```

Creating callbacks within a list:
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

For each unique key we have a persisted callback instance that is kept across multiple `Formula.evaluate` calls. The
instance is disabled and removed when your `Formula` is removed or if you don't create this callback in the current
`Formula.evaluate` call.


### Testing
To simplify testing your Formulas, you can use `formula-test` module.
```
testImplementation 'com.github.instacart:formula-test:{latest_version}'
```

Testing the last render model emission
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

To inspect the input that was passed to the child
```kotlin
subject.childInput(MyChildFormula::class) {
    assertThat(this.property).isEqualTo("property")
}
```

You can fake child events
```kotlin
subject.childInput(MyChildFormula::class) {
    this.onEvent("fake data")
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

### Dynamic callback registrations detected.
By default, formula uses positional index for callback uniqueness. This exception indicates that some of your callbacks
need to have an explicit key likely because some callback is defined within `if` block or a `list loop`. For more info
take a look at the [callbacks section](#callbacks).
