# Formula Android

The Android module provides activity lifecycle helpers and a route-oriented `NavigationStore`.
It does not provide a fragment implementation or render views for you; the host application owns
presentation and forwards route lifecycle events to the store.

## Define a route feature

A `RouteKey` identifies a route. A `FeatureFactory` creates the feature state flow and its
`ViewFactory` for that route.

```kotlin
@Parcelize
data class CounterKey(
    override val tag: String = "counter",
) : RouteKey

class CounterFeatureFactory : FeatureFactory<Unit, CounterKey>() {
    override fun Params.initialize(): Feature {
        val formula = CounterFormula()
        return Feature(CounterViewFactory()) { scope ->
            formula.runAsStateFlow(scope, Unit)
        }
    }
}
```

## Build and drive a NavigationStore

Bind feature factories when building the store. The host creates route IDs, sends lifecycle and
visibility events, and observes `stateFlow` to render route outputs.

```kotlin
val navigationStore = NavigationStore.Builder().build {
    bind(CounterFeatureFactory())
}

val routeId = RouteId("counter-instance", CounterKey())
navigationStore.onLifecycleEvent(RouteLifecycleEvent.Added(routeId))

// Collect navigationStore.stateFlow and render state.outputs[routeId]?.renderModel.

navigationStore.onVisibilityChanged(routeId, visible = true)
// When the route leaves the navigation stack:
navigationStore.onLifecycleEvent(RouteLifecycleEvent.Removed(routeId))
navigationStore.dispose()
```

A route's `Feature` remains active until its `RouteLifecycleEvent.Removed` event. Hosts can use the
feature's `ViewFactory` to choose how to render its model, or supply their own rendering logic.

## Activity integration

`ActivityStore` and `FormulaAndroid` provide activity lifecycle, activity-result, and activity
injection callbacks. They do not observe or render `NavigationStore` state. The host retains the
store and forwards route events itself.

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FormulaAndroid.init(this) {
            activity(MyActivity::class) {
                ActivityStore()
            }
        }
    }
}
```

Call `FormulaAndroid.onPreCreate(this, savedInstanceState)` before `super.onCreate()` and forward
activity results to `FormulaAndroid.onActivityResult` after calling the superclass implementation.
Use `ActivityStoreContext.send { ... }` for activity-bound effects and `activityResults()` or
`activityLifecycleState()` to observe activity events.
