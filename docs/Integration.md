## Formula Integration
The integration module provides declarative API to connect reactive state management to Android Fragments. 
This module has been designed for gradual adoption. You can use as much or as little of it as you like.

Benefits of using it:
1. Can be added easily to an app that already uses Fragments.
2. Supports incremental migration / usage. Not all fragments need to use Formula state management.
3. State management survives configuration changes.
4. Supports modularization
5. Works naturally with Dagger 2
6. Supports single activity architecture.

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
        
        // This function needs to return Flowable<TaskDetailRenderModel>
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
        Flowable.error(Throwable("not implemented yet."))
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
    val state: Flowable<FragmentFlowState> =  store.state().replay(1).apply {
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
    override val tag: String = "item detail ${taskId}",
    override val layoutId: Int = ...
) : FragmentContract<RenderModelType>() {
  
    override fun createComponent(view: View): FragmentComponent<RenderModelType> {
        return ...
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

## Moving integration into a separate class
When you reach a certain number of fragment integrations, the store creation logic can become unwieldy. To keep it tidy,
you can place integration logic into separate class.
```kotlin
object TaskDetailIntegration : Integration<TaskAppComponent, TaskDetailContract, TaskDetailRenderModel>() {
    override fun create(component: TaskAppComponent, key: TaskDetailContract): Flowable<TaskDetailRenderModel> {
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

## Grouping multiple fragments as part of a flow.
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
      bind(Contract1::class) { component, key ->
        // create contract 1 state stream
      }
      bind(Contract2::class) { component, key ->
        // create contract 2 state stream
      }
    } 
  }
}
```


## How does the fragment flow store works?
The basic mechanism:
- `FragmentFlowRenderView` listens to fragment add & remove events and passes those events to `FragmentFlowStore`
- Based on those events, `FragmentFlowStore` instantiates or disposes of the appropriate render model stream.
- We then listen to `FragmentFlowStore.state()` and pass those render model changes to the `FragmentFlowRenderView.renderer.render` 
- `FragmentFlowRenderView` finds `FormulaFragment` and passes the state to the `RenderView` which then applies the change to Android views.  

Key things:
- An instance of `FragmentFlowStore` should be created for each `Activity`. This instance should survive the `Activity` lifecycle (e.g. using `ViewModel`) and should be disposed of when no longer in use.
- `FragmentFlowRenderView` needs to be created before `Activity.super.onCreate()`, so we do not miss fragment events on Activity restoration.
- Only when fragment is completely removed, do we dispose of the state management. This means that if you are going 
forward from `A` to `B` fragment, both A & B state management streams will be running. 


## How to navigate from one fragment to another?

## Using with dagger (TODO)

