## Formula Android
The Android module provides a declarative API to connect reactive state management to Android Fragments. 
This module has been designed for gradual adoption. You can use as much or as little of it as you like.

Some of the goals for this module are:

    - Use a single RxJava stream to drive the UI.
    - Separate state management from Android UI lifecycle.
    - Ability to group multiple fragments into a flow and share state between them.
    - Type-safe and scoped fragment event handling. (Avoid casting activity to a listener)

## Using Fragments
This module provides an API to connect state management and view rendering logic to Android 
fragments. For this example, we will connect `CounterRenderView` and `CounterFormula` from the 
main getting started [guide](index.md). 

### Define a fragment key
Fragment key is used to instantiate `FormulaFragment` and to identify which `FeatureFactory` to 
use. You can also use it to add arguments that the fragment instance needs. 

```kotlin
/**
 * Fragment key has to provide Parcelable implementation because it is passed 
 * to the fragment as an argument. 
 *   
 * Read more about Parcelize: https://kotlinlang.org/docs/tutorials/android-plugin.html
 */
@Parcelize
data class CounterKey(
    override val tag: String = "counter"
) : FragmentKey
```

### Define a feature factory
A feature factory creates the state observable and a view factory for a fragment. To continue our 
example, we define a `CounterFeatureFactory` which will handle `CounterKey` fragments.

```kotlin
class CounterFeatureFactory : FeatureFactory<Any, CounterKey> {
    override fun initialize(dependencies: Any, key: CounterKey): Feature {
        val counterFormula = CounterFormula()        
        return Feature(
            state = counterFormula.toObservable(),
            viewFactory = CounterViewFactory()
        )   
    }
}

// View factory which uses XML layout resource.
class CounterViewFactory : LayoutViewFactory<CounterRenderModel>(R.layout.counter) {
    override fun ViewInstance.create(): FeatureView<CounterRenderModel> {
        // We use [ViewInstance.view] to access the inflated view
        val counterView = CounterRenderView(view)

        // We create a [FeatureView] by passing a [RenderView]
        return featureView(counterView)
    }
}
```

We now need to register our feature factory with the activity in which the counter will be shown.
```kotlin
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        FormulaAndroid.init(this) {
            activity(MyActivity::class) {
                store {
                    bind(CounterFeatureFactory())
                }
            }
        }
    }
}
```

### Use formula fragment
The only thing left is navigating to this screen. We create `FormulaFragment` instance using
our `CounterKey` and use fragment transactions to add it.
```kotlin
class MyActivity : FormulaAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_activity)
        
        if (savedInstanceState == null) {
            val key = CounterKey()
            val fragment = FormulaFragment.newInstance(key)
            
            // Add the fragment using the fragment transaction API.
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_content, fragment, key.tag)            
                .commit()    
        }
    }
}
```

If your `Activity` has another base class, you can just copy logic from `FormulaAppCompatActivity` into your `Activity`.

### And that's it
Formula takes care of the rest. This is how the state observable works:

- When `FormulaFragment` is added, we instantiate and subscribe to the state observable.
- When `FormulaFragment` is removed, we destroy the state observable.

The state management observable continues to run during configuration changes or if you navigate
to another fragment.

## Passing arguments to a fragment
Arguments can be passed using fragment key class. For example, we want to pass initial count 
value to the `CounterFormula` used in the previous examples. To accomplish that, let's update the
`CounterKey`.

```kotlin
@Parcelize
data class CounterKey(
    val initialCount: Int = 0,    
    override val tag: String = "counter"
) : FragmentKey
```

You can access the `CounterKey` within `CounterFeatureFactory`
```kotlin
class CounterFeatureFactory : FeatureFactory<Any, CounterKey> {
    override fun initialize(dependencies: Any, key: CounterKey): Feature {
        val initialCount = key.initialCount    
        val counterFormula = CounterFormula(initialCount)        
        ...                
    }
}
```

## Fragment Event Handling
Very frequently we need to pass events from a fragment to the parent/activity which trigger 
things like navigation.
 
Let's say we want to add the following behaviors to the previous counter example:

- show a toast notification when user increments to 10 
- navigate to a new "victory" screen when user increments to 100. 

First, let's define a class that defines our events.
```kotlin
data class CounterEventRouter(
  val onToastNotification: (String) -> Unit, 
  val onVictoryReached: () -> Unit
)
```

We can now request this dependency within our feature factory 
```kotlin
class CounterFeatureFactory : FeatureFactory<Dependencies, CounterKey> {

    // We can ask for dependencies from the parent using an interface.
    interface Dependencies {
        fun counterEventRouter(): CounterEventRouter
    }

    override fun initialize(dependencies: Dependencies, key: CounterKey): Feature {
        val counterEventRouter = dependencies.counterEventRouter()
        // We can pass the event router to the counter formula.
        val counterFormula = CounterFormula(counterEventRouter)        
        return ...
    }
}
```

To provide dependencies, the parent component needs to extend `CounterFeatureFactory.Dependencies`
```kotlin
class MyActivityComponent(
    private val store: ActivityStoreContext<MyActivity>
) : CounterFeatureFactory.Dependencies { 

    override fun counterEventRouter(): CounterEventRouter {
        return CounterEventRouter(
            onToastNotification = this::showToast,    
            onVictoryReached = {
                // VictoryFragmentKey implementation is left to readers imagination.
                val key = VictoryFragmentKey()
                navigateTo(key)
            }
        )
    }

    private fun showToast(message: String) {
        store.send {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }        
    }    

    private fun navigateTo(key: FragmentKey) {
       store.send {
           // Sample fragment transaction 
           val fragment = FormulaFragment.newInstance(key)
           supportFragmentManager.beginTransaction()
               .add(R.id.activity_content, fragment, key.tag)
               .addToBackStack(null)
               .commit()
       }  
    }    
}
```

To pass this component to feature factories, we need to update the configuration that lives 
within our `Application`.

```kotlin
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        FormulaAndroid.init(this) {
            activity(MyActivity::class) {
                ActivityStore(
                    fragmentStore = FragmentStore.init(MyActivityComponent(this)) {
                        bind(CounterFeatureFactory())
                    }
                )
            }
        }
    }
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
This is already in place for you if you use `FormulaAppCompatActivity`.

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
        
        ActivityStore(
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
