## Formula Integration
The integration module provides a declarative API to connect reactive state management to Android Fragments. 
This module has been designed for gradual adoption. You can use as much or as little of it as you like.

Some of the goals for this module are:
- Use single RxJava stream to drive the UI.
- Separate state management from Android UI lifecycle.
- Ability to group multiple fragments into a flow and share state between them.
- Type-safe and scoped fragment event handling. (Avoid casting activity to a listener)

### Declarative API
This module provides a declarative API where you define state management for each of 
your navigation destinations (we call them contracts). You define a store and bind
individual contract types to the state management.

```kotlin
val store = FragmentFlowStore.init(...) {
    bind(LoginContract::class) { _, contract ->
        TODO("return an RxJava state stream that drives the UI")
    }

    bind(ItemListContract::class) { _, contract ->
        TODO("return an RxJava state stream that drives the UI")
    } 
    
    bind(ItemDetailContract::class) { _, contract ->
        TODO("return an RxJava state stream that drives the UI")
    }
}
```

### Lifecycle of individual state streams is managed for you
The RxJava state stream is instantiated and subscribed to when the user enters declared navigation destination. We 
dispose of the stream only when user exits the destination. As long as the `FragmentFlowStore.state()` is subscribed to 
within a surface that survives configuration changes such as Android Components ViewModel, all of the state streams will
survive configuration changes.

```kotlin
class MyActivityViewModel : ViewModel() {
    private val store: FragmentFlowStore = ... 

    private val disposables = CompositeDisposable()

    // We use replay + connect so this stream survives configuration changes.
    val state: Observable<FragmentFlowState> =  store.state().replay(1).apply {
        connect { disposables.add(it) }
    }

    // The activity will pass fragment lifecycle events so we 
    // could figure out what state management needs to run.
    fun onLifecycleEvent(event: LifecycleEvent<FragmentContract<*>>) {
        store.onLifecycleEffect(event)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
```


## Defining the first fragment contract
FragmentContract defines how a fragment should bind a specific type of render model to Android views. It is also
used as a key to instantiate the state management. 

```kotlin
// Fragment contract has to provide Parcelable implementation because it is passed to the fragment as an argument.
// Read more about Parcelize: https://kotlinlang.org/docs/tutorials/android-plugin.html
@Parcelize 
data class TaskDetailContract(
    val taskId: Int,
    override val tag: String = "task ${taskId}",
    override val layoutId: Int = R.layout.task_detail
) : FragmentContract<TaskDetailRenderModel>() {

    override fun createComponent(view: View): FragmentComponent<TaskDetailRenderModel> {
        val taskDescriptionView = view.findViewById(R.id.task_description_view)
        val deleteButton = view.findViewById(R.id.task_delete_button)
        
        return FragmentComponent.create { renderModel ->
            taskDescriptionView.text = renderModel.taskDescription
            deleteButton.setOnClickListener {
                renderModel.onDeleteSelected()
            }
        }
    }
}
```

When we want to navigate to task detail, we create `FormulaFragment` using this contract.
```kotlin
val contract = TaskDetailContract(taskId = 1)
val fragment = FormulaFragment.newInstance(contract)

// Add the fragment using the fragment transaction API.
supportFragmentManager.beginTransaction()
    .add(R.id.activity_content, fragment, contract.tag)
    .commit()

```

Now let's bind the fragment contract to the state management. We create a `FragmentFlowStore` that
enables us to bind various fragment contracts to their state management.
```kotlin
val component: TaskAppComponent = ...

// Declaring fragment store with all the fragment contracts we handle.
val store = FragmentFlowStore.init(component) {
    bind(TaskDetailContract::class) { component: TaskAppComponent, key: TaskDetailContract ->
        // When a fragment is added as part of a fragment transaction,
        // this lambda function is called to instantiate the state management 
        // for that fragment. The specific fragment contract is passed to this
        // function from which we we can get the task id.
        val taskId = key.taskId
        
        // This function needs to return Observable<TaskDetailRenderModel>
        component.taskRepo.findTask(taskId).map { task ->
            TaskDetailRenderModel(
                description = task.description,
                onDeleteSelected = {
                    component.taskRepo.delete(taskId)
                }
            )
        }
    }
    
    bind(AnotherFragmentContract::class) { component, key ->
        Observable.error(Throwable("not implemented yet."))
    }
}
``` 
 
The store should live outside of the activity so that it would survive configuration changes. One option is to place it 
inside of the android architecture component ViewModel.
```kotlin
class MyActivityViewModel : ViewModel() {
    private val store = /* see previous example how to create a store */

    private val disposables = CompositeDisposable()

    // We use replay + connect so this stream survives configuration changes.
    val state: Observable<FragmentFlowState> =  store.state().replay(1).apply {
        connect { disposables.add(it) }
    }

    // The activity will pass fragment lifecycle events so we 
    // could figure out what state management needs to run.
    fun onLifecycleEvent(event: LifecycleEvent<FragmentContract<*>>) {
        store.onLifecycleEffect(event)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
```

Finally, we need to wire everything up in our activity 
```kotlin
class MyActivity : FragmentActivity() {
     private lateinit var fragmentRenderView: FragmentFlowRenderView
 
     val disposables = CompositeDisposable()
 
     override fun onCreate(savedInstanceState: Bundle?) {
         val viewModel = ViewModelProviders.of(this).get(MyActivityViewModel::class.java)
         // We need to initialize this before Activity.super.onCreate(). We want to listen to all
         // fragment lifecycle events and pass them to the FragmentFlowStore.
         fragmentRenderView = FragmentFlowRenderView(this, onLifecycleEvent = viewModel::onLifecycleEvent)
 
         super.onCreate(savedInstanceState)
         setContentView(R.layout.my_activity)
         
         // We listen for fragment state changes and pass them to the FragmentFlowRenderView to render. 
         disposables.add(viewModel.state.subscribe(fragmentRenderView.renderer::render))
     }
 
     override fun onDestroy() {
         disposables.clear()
         fragmentRenderView.dispose()
         super.onDestroy()
     }
 
     override fun onBackPressed() {
         if (!fragmentRenderView.onBackPressed()) {
             super.onBackPressed()
         }
     }
}
```

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
sealed class ActivityEffect {
    class ShowToast(val message: String): ActivityEffect()
    class CloseFragment(val tag: String): ActivityEffect()
}
```

We then create a PublishRelay that we use for pub-sub messaging with the Activity.
```kotlin
class MyActivityViewModel : ViewModel() {
    private val effectRelay: PublishRelay<ActivityEffect> = PublishRelay.create()
    
    private val store = FragmentFlowStore.init(...) {
        bind(ItemDetailContract::class) { component, contract ->
            val input = ItemDetailFormula.Input(
                onItemFavorited = {
                    effectRelay.accept(ActivityEffect.ShowToast("Item was added to your favorites."))
                },
                onItemDeleted = {
                    effectRelay.accept(ActivityEffect.CloseFragment(contract.tag))        
                }
            )
        
            val formula: ItemDetailFormula = component.createItemDetailFormula()
            formula.state(input)
        }
    }    
    
    private val disposables = CompositeDisposable()

    // We use replay + connect so this stream survives configuration changes.
    val state: Observable<FragmentFlowState> =  store.state().replay(1).apply {
        connect { disposables.add(it) }
    }

    // Expose effects to the activity
    val effects: Observable<ActivityEffect> = effectRelay.hide()
}
```

In the activity, we then listen to `effects` stream and do a pattern match
```kotlin
class MyActivity : FragmentActivity() {
     val disposables = CompositeDisposable()
 
     override fun onCreate(savedInstanceState: Bundle?) {
         val viewModel = ViewModelProviders.of(this).get(MyActivityViewModel::class.java)
 
         super.onCreate(savedInstanceState)
         setContentView(R.layout.my_activity)
         
         disposables.add(viewModel.effects.subscribe { effect ->
            when (effect) {
                is ShowToast -> {
                    Toast.makeText(this, effect.message, Toast.LENGTH_LONG).show();
                } 
                is CloseFragment -> {
                    supportFragmentManager.popBackStack()
                }
            }
         })
     }
 
     override fun onDestroy() {
         disposables.clear()
         super.onDestroy()
     }
}
```

## Navigation
To trigger navigation from one screen to another, we add a new type to the `ActivityEffect` sealed class.
```kotlin
sealed class ActivityEffect {
    ... 
    class NavigateToFragmentContract(val contract: FragmentContract<*>): ActivityEffect()
}
```

Now, we can trigger it from event callback such as `onItemSelected`
```kotlin
class MyActivityViewModel : ViewModel() {

    private val effectRelay: PublishRelay<ActivityEffect> = PublishRelay.create()
    
    private val store = FragmentFlowStore.init(...) {
        bind(ItemListContract::class) { component, contract ->
            // Provide callbacks to item list feature events.
            val input = ItemListFormula.Input(
                onItemSelected = { item ->
                    val contract = ItemDetailContract(id = item.id)
                    effectRelay.accept(ActivityEffect.NavigateToFragmentContract(contract))        
                }
            )
        
            // Hook up the formula state management
            val formula: ItemListFormula = ...
            formula.state(input)
        }
    }    
}
```

In our activity, we can re-act to this effect and perform the navigation
```kotlin
class MyActivity : FragmentActivity() {
     
     private fun handleActivityEffect(effect: ActivityEffect) {
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
    private val confirmUserWantsToExit: () -> Unit
): BackCallback {

    fun onBackPressed() {
        confirmUserWantsToExit()
    }
}
```

Your `Activity` needs to call `FragmentFlowRenderView.onBackPressed()`. It will check if your current screen
implements `BackCallback` and will invoke it.
```kotlin
class MyActivity : FragmentActivity() {
     private lateinit var fragmentRenderView: FragmentFlowRenderView
 
     override fun onBackPressed() {
         if (!fragmentRenderView.onBackPressed()) {
             super.onBackPressed()
         }
     }
}
```


## Using with dagger (TODO)

