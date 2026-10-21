## Formula Android

The Android module contains two independent APIs: activity lifecycle callbacks and `NavigationStore`.
They are documented separately so either section can move independently if the APIs are split into
separate libraries in the future.

## Activity lifecycle callbacks

`FormulaAndroid` and `ActivityStore` provide activity-specific setup, effects, and cleanup. This API
does not depend on `NavigationStore`.

### Configure and clear an activity store

Register an `ActivityStore` for the activity type. Its `configureActivity` callback runs from
`FormulaAndroid.onPreCreate`, before `super.onCreate`, and can inject the activity. Its `onCleared`
callback runs when the activity store is cleared and can release activity-scoped resources.

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val appComponent: AppComponent = ...
        FormulaAndroid.init(this) {
            activity(MyActivity::class) {
                // This component is retained across activity configuration changes.
                val activityComponent = appComponent.createMyActivityComponent()
                ActivityStore(
                    configureActivity = { activity -> activityComponent.inject(activity) },
                    onCleared = {
                        // Release resources owned by activityComponent.
                    },
                )
            }
        }
    }
}
```

Forward activity setup from the activity:

```kotlin
class MyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FormulaAndroid.onPreCreate(this, savedInstanceState)
        super.onCreate(savedInstanceState)
    }
}
```

`ActivityStoreContext` provides activity-bound effects and lifecycle state. Use `send { ... }` for
effects and `activityLifecycleState()` to observe lifecycle state.

## NavigationStore

`NavigationStore` manages feature state for routes and exposes route output through `stateFlow`. It is
independent of `FormulaAndroid` and `ActivityStore`; the host creates it, forwards route events,
renders its output, and disposes it when its owner no longer needs it.

### Define a route key and feature

A `RouteKey` identifies a route and selects its `FeatureFactory`. Route keys are `Parcelable` and can
carry arguments needed to initialize a feature. This example connects `CounterScreen` and
`CounterFormula` from the getting started [guide](index.md).

```kotlin
@Parcelize
data class CounterKey(
    override val tag: String = "counter",
) : RouteKey

class CounterFeatureFactory : FeatureFactory<Unit, CounterKey>() {
    override fun Params.initialize(): Feature {
        val counterFormula = CounterFormula()
        return Feature(CounterViewFactory()) { scope ->
            counterFormula.runAsStateFlow(scope, input = Unit)
        }
    }
}

class CounterViewFactory : ComposeViewFactory<CounterOutput>() {
    @Composable
    override fun Content(model: CounterOutput) {
        CounterScreen(model)
    }
}
```

### Create and drive a store

Bind feature factories when building the store. The host creates route IDs, forwards lifecycle and
visibility changes, and observes `stateFlow` to render route outputs. A feature is created when its
route is added and disposed when the route is removed.

```kotlin
val navigationStore = NavigationStore.Builder().build {
    bind(CounterFeatureFactory())
}

val routeId = RouteId("counter-instance", CounterKey())
navigationStore.onLifecycleEvent(RouteLifecycleEvent.Added(routeId))
navigationStore.onVisibilityChanged(routeId, visible = true)

// Collect navigationStore.stateFlow and render state.outputs[routeId]?.renderModel.

// When the host removes the route:
navigationStore.onLifecycleEvent(RouteLifecycleEvent.Removed(routeId))
navigationStore.dispose()
```

The host can use the feature's `ViewFactory` to render its model or provide its own rendering logic.

### Passing arguments as part of a route

Put route-specific arguments on the route key. `FeatureFactory.Params.key` exposes them when the
store initializes the feature.

```kotlin
@Parcelize
data class CounterKey(
    val initialCount: Int = 0,
    override val tag: String = "counter",
) : RouteKey

class CounterFeatureFactory : FeatureFactory<Unit, CounterKey>() {
    override fun Params.initialize(): Feature {
        val counterFormula = CounterFormula(key.initialCount)
        return Feature(CounterViewFactory()) { scope ->
            counterFormula.runAsStateFlow(scope, input = Unit)
        }
    }
}
```

### Route event handling

A feature can receive parent-provided dependencies to send events back to the host. For example, a
counter might show a toast at 10 and navigate to a victory route at 100.

```kotlin
data class CounterEventRouter(
    val onToastNotification: (String) -> Unit,
    val onVictoryReached: () -> Unit,
)

class CounterFeatureFactory : FeatureFactory<Dependencies, CounterKey>() {
    interface Dependencies {
        fun counterEventRouter(): CounterEventRouter
    }

    override fun Params.initialize(): Feature {
        val counterFormula = CounterFormula(dependencies.counterEventRouter())
        return Feature(CounterViewFactory()) { scope ->
            counterFormula.runAsStateFlow(scope, input = Unit)
        }
    }
}
```

The host supplies these dependencies when building the store. It can implement navigation and UI
effects using its own framework:

```kotlin
class CounterHostDependencies(
    private val showToast: (String) -> Unit,
    private val navigateTo: (RouteKey) -> Unit,
) : CounterFeatureFactory.Dependencies {
    override fun counterEventRouter() = CounterEventRouter(
        onToastNotification = showToast,
        onVictoryReached = { navigateTo(VictoryRouteKey()) },
    )
}

val dependencies = CounterHostDependencies(::showToast, ::navigateTo)
val navigationStore = NavigationStore.Builder().build(dependencies) {
    bind(CounterFeatureFactory())
}
```
