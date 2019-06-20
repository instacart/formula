## Formula Android
The Android module provides a declarative API to connect reactive state management to Android Fragments. 
This module has been designed for gradual adoption. You can use as much or as little of it as you like.

Some of the goals for this module are:
- Use single RxJava stream to drive the UI.
- Separate state management from Android UI lifecycle.
- Ability to group multiple fragments into a flow and share state between them.
- Type-safe and scoped fragment event handling. (Avoid casting activity to a listener)

## Getting Started
For the getting started guide, we will build a timer which you can reset. This is a simple example, but 
it will be sufficient to display some of the concepts around this module.

### Defining the render model
When working with Formula, usually the first thing we define is what our UI will be rendering and what actions it will
perform. Render Model is a class that defines this.

```kotlin
class TimerRenderModel(
    val time: String,
    val onResetSelected: () -> Unit
)
```

### Let's apply this render model to android views
We define a fragment contract for how a render model is applied to Android views.
```kotlin
// Fragment contract has to provide Parcelable implementation because it is passed to the fragment as an argument.
// Read more about Parcelize: https://kotlinlang.org/docs/tutorials/android-plugin.html
@Parcelize
data class TimerContract(
    override val tag: String = "timer",
    override val layoutId: Int = R.layout.timer
) : FragmentContract<TimerRenderModel>() {

    // A layout is automatically inflated and the view is passed to this callback.
    override fun createComponent(view: View): FragmentComponent<TimerRenderModel> {
        val timerTextView = view.findViewById(R.id.timer_text_view)
        val resetButton = view.findViewById(R.id.timer_reset_button)
        
        return FragmentComponent.create { renderModel ->
            timerTextView.text = renderModel.time
            resetButton.setOnClickListener {
                renderModel.onResetSelected()
            }
        }
    }
}
```

### Register state management for this screen
Fragment contract is used as a navigation destination key. For each of the fragment contract types, we provide 
a state management factory. This factory has to return an `Observable<RenderModel>`. The factory will be invoked
when the user enters this destination.
```kotlin
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        FormulaAndroid.init(this) {
            activity(MyActivity::class) {
                store {
                    // Bind function provides type safety - given a TimerContract,
                    // it expects Observable<TimerRenderModel> from the factory
                    bind(TimerContract::class) { _, contract ->
                        val resetRelay = PublishRelay.create<Unit>()
                        
                        resetRelay.startWith(Unit).switchMap { 
                            Observable
                                .interval(0, 1, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                        }
                        .map {
                            TimerRenderModel(
                                time = "$it seconds passed.",
                                onResetSelected = {
                                    resetRelay.accept(Unit)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
```

For the sake of simplicity, I've inlined the state management logic into the `bind` function. In a real world example,
this logic would live within `Formula` or `RenderFormula` classes.

### The only thing left is navigating to this screen
```kotlin
class MyActivity : FormulaAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_activity)
        
        if (savedInstanceState == null) {
            val contract = TimerContract()
            val fragment = FormulaFragment.newInstance(contract)
            
            // Add the fragment using the fragment transaction API.
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, fragment, contract.tag)
                .commit()    
        }
    }
}
```

If your `Activity` has another base class, you can just copy logic from `FormulaAppCompatActivity` into your `Activity`.

### And that's it
Formula takes care of the rest. The RxJava state stream is instantiated and subscribed to when the user enters 
declared navigation destination. We dispose of the stream only when user exits the destination. 


## How to pass arguments such as item id to the fragment?
Arguments can be passed using the Fragment contract.
```kotlin
@Parcelize
data class ItemDetailContract(
    val itemId: Int,
    override val tag: String = "item detail ${itemId}",
    override val layoutId: Int = R.layout.item_detail
) : FragmentContract<RenderModelType>() {
  
    override fun createComponent(view: View): FragmentComponent<RenderModelType> {
        return TODO()
    }
}
```

The contract is passed to the function that instantiates the state management
```kotlin
val store = FragmentFlowStore.init {
    bind(ItemDetailContract::class) { _, key: ItemDetailContract ->
        // do something with the item id
        key.itemId
    }
}
```

### Fragment Event Handling
In fragments, a common pattern for passing events to the parent is casting Activity into a Listener
```kotlin
class MyFragment : Fragment() {
    override fun onAttach(context: Context) {
        listener = context as Listener
    }

    override fun onDetach(context: Context) {
        listener = null
    }
}
```

Instead of a listener with methods, we define a sealed class of possible actions that activity can perform.
```kotlin
sealed class MyActivityEffect {
    class ShowToast(val message: String): MyActivityEffect()
    class CloseFragment(val tag: String): MyActivityEffect()
}
```

```kotlin
class MyActivity : FragmentActivity() {

    fun onActivityEffect(effect: MyActivityEffect) {
        when (effect) {
            is ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_LONG).show();
            } 
            is CloseFragment -> {
                supportFragmentManager.popBackStack()
            }
        }
    }
}
```

We can then use a `ActivityProxy<MyActivity>` to trigger this effect.
```kotlin
activity(MyActivity::class) {
    store {
        bind(ItemDetailContract::class) { _, contract ->
            val input = ItemDetailFormula.Input(
                onItemFavorited = {
                    send {
                        onActivityEffect(MyActivityEffect.ShowToast("Item was added to your favorites."))
                    }
                },
                onItemDeleted = {
                    send {
                        onActivityEffect(MyActivityEffect.CloseFragment(contract.tag))
                    }
                }
            )
        
            val formula: ItemDetailFormula = component.createItemDetailFormula()
            formula.state(input)
        }
    }
}
```

## Navigation
To trigger navigation from one screen to another, we add a new type to the `MyActivityEffect` sealed class.
```kotlin
sealed class MyActivityEffect {
    ... 
    class NavigateToFragmentContract(val contract: FragmentContract<*>): MyActivityEffect()
}
```

Now, we can trigger it from event callback such as `onItemSelected`
```kotlin
activity(MyActivity::class) {
    store {
        bind(ItemListContract::class) { _, contract ->
            // Provide callbacks to item list feature events.
            val input = ItemListFormula.Input(
                onItemSelected = { item ->
                    val contract = ItemDetailContract(id = item.id)
                    send {
                        onActivityEffect(ActivityEffect.NavigateToFragmentContract(contract))
                    }
                }
            )
        
            // Hook up the formula state management
            val formula: ItemListFormula = ...
            formula.state(input)
        }
    }
}
```

In our activity, we can react to this effect and perform the navigation
```kotlin
class MyActivity : FormulaAppCompatActivity() {
     
     fun onActivityEffect(effect: MyActivityEffect) {
        when(effect) {
            is NavigateToFragmentContract -> {
                // Perform navigation using fragment transaction
                val fragment = FormulaFragment.newInstance(effect.contract)
                
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, effect.contract.tag)
                    .addToBackStack(null)
                    .commit()
            }
        }
     }
}
```


## Grouping multiple navigation destinations as part of a flow.
Flow is a combination of screens that are grouped together and can share a common component / state.

```kotlin
class MyFlowDeclaration : FlowDeclaration<MyFlowDeclaration.Component>() {
  // Define the shared component for the flow 
  class Component(
    val sharedService: MyFlowService,
    val onSomeEvent: () -> Unit
  )

  override fun createFlow(): Flow<Component> {
    return build {
      bind(Contract1::class) { component, contract ->
        TODO("return an RxJava state stream that drives the UI")
      }
      bind(Contract2::class) { component, contract ->
        TODO("return an RxJava state stream that drives the UI")
      }
    } 
  }
}
```

## Moving integration into a separate class
When you reach a certain number of fragment integrations, the store creation logic can become unwieldy. To keep it tidy,
you can place integration logic into separate class.
```kotlin
object TaskDetailIntegration : Integration<TaskAppComponent, TaskDetailContract, TaskDetailRenderModel>() {
    override fun create(component: TaskAppComponent, key: TaskDetailContract): Observable<TaskDetailRenderModel> {
        return component.taskRepo.findTask(taskId).map { task ->
            TaskDetailRenderModel(
                description = task.description,
                onDeleteSelected = {
                    component.taskRepo.delete(taskId)
                }
            )
        }
    }
}
```

Now we can update our store to use this integration.
```kotlin
val store = FragmentFlowStore.init(taskAppComponent) {
    bind(TaskDetailIntegration)
    
    // Other integrations.
    bind(Contract1Integration)
    bind(Contract2Integration)
}
```

## Handling back button events
To override how the back button works for a particular navigation destination, your render model needs to implement
`BackCallback` interface.

```kotlin
data class FormRenderModel(
    private val confirmBeforeExiting: Boolean,
    private val confirmUserWantsToExit: () -> Unit
): BackCallback {

    fun onBackPressed(): Boolean {
        // Check if we need to override back handling
        if (confirmBeforeExiting) {
            confirmUserWantsToExit()
            return true
        }
        
        // Use default behavior (which closes the screen)
        return false 
    }
}
```

Your `Activity` needs to call `FormulaAndroid.onBackPressed()`. It will check if your current screen
implements `BackCallback` and will invoke it.
```kotlin
class MyActivity : FragmentActivity() {

     override fun onBackPressed() {
         if (!FormulaAndroid.onBackPressed(this)) {
             super.onBackPressed()
         }
     }
}
```

## Activity state management
One of the goals of Formula is to make doing the right thing easy. As part of that we wanted to provide an easy
way for state streams to survive configuration changes by default. 

Let's define a basic activity that has a `renderTime` method.
```kotlin
class MyActivity : FormulaAppCompatActivity() {

    fun renderTime(time: String) {
        // implementation left to the reader
    }
}
```

To connect an RxJava Observable to `renderTime`, we define `streams` parameter which expects a `Disposable`
back. Within this method you can subscribe to any number of RxJava streams.    
```kotlin
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        FormulaAndroid.init(this) {
            activity<MyActivity> {
            
                store(
                    streams = {
                        // You can subscribe to your RxJava streams here.
                        val timerState =  Observable
                            .interval(0, 1, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .map { time ->
                                "$time seconds"
                            }
                            
                        // update ensures that if configuration changes happen that
                        // we send the last state to the new activity instance.    
                        update(timerState, MyActivity::renderTime)
                    }
                )
            }
        }
    }
}
```

You might be confused about the `update` function called there. It is provided within the context of `streams` function
using Kotlin receiver parameter `StreamConfigurator`. The `update` function ensures that state changes only arrive after
`Activity` has started and that last state is applied if `Activity` is re-created due to configuration changes. It returns
a `Disposable`.


## Managing dependencies
Managing dependencies in Formula is very easy. In the function that instantiates the `ActivityStore` for your activity, 
you can create your activity specific dependencies or Dagger components. These objects will survive configuration changes.
```kotlin
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        val appComponent: AppComponent = ...
        
        FormulaAndroid.init(this) {
            activity(MyActivity::class) {
                // This component will survive configuration changes.
                val activityComponent = appComponent.createMyActivityComponent()
                
                store { }
            }
        }
    }
}
```

To inject the activity or create activity dependencies that don't survive configuration changes such as ones that need direct
activity reference, you can use `configureActivity` callback.
```kotlin
val appComponent: AppComponent = ...

FormulaAndroid.init(this) {
    activity(MyActivity::class) {
        // This component will survive configuration changes.
        val activityComponent = appComponent.createMyActivityComponent()
        
        store(
            configureActivity = {
                // in this callback `this` is the instance of MyActivity
                // so we can use it to inject dependencies
                activityComponent.inject(this)
                
                // Or you can use setters to provide dependencies to your activity.
                // This dependency object won't survive configuration changes.
                val dependency = MyActivityDependency(activity = this)
                this.setDependency(dependency)
            }
        )
    }
}
```
