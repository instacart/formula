## Formula Android

The Android module connects Formula features to a host-managed route and rendering system. It provides feature registration, route state management, activity-scoped dependencies, and Compose view factories; it does not create or manage screen containers.

## Define a route key

A route key identifies a screen type and carries its arguments. Implement `Parcelable` so the navigation host can persist it as needed.

```kotlin
@Parcelize
data class CounterKey(
    val initialCount: Int = 0,
    override val tag: String = "counter",
) : RouteKey
```

The navigation host supplies a unique instance ID for each back-stack entry. Together, the key and ID form a `RouteId`:

```kotlin
val routeId = RouteId(instanceId = entry.instanceId, key = entry.key)
```

## Define a feature factory

A feature factory builds the Formula state flow and its view factory for a route.

```kotlin
class CounterFeatureFactory : FeatureFactory<CounterFeatureFactory.Dependencies, CounterKey>() {
    interface Dependencies {
        fun counterInput(key: CounterKey): CounterFormula.Input
    }

    override fun Params.initialize(): Feature {
        val formula = CounterFormula()
        return Feature(CounterViewFactory()) { scope ->
            formula.runAsStateFlow(scope, dependencies.counterInput(key))
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

Bind factories when building the `NavigationStore`:

```kotlin
val store = NavigationStore.Builder().build(dependencies) {
    bind(CounterFeatureFactory())
}
```

## Navigation host responsibilities

The host owns back-stack identity, persistence, lifecycle events, and rendering. It reports route additions and removals to the store and marks routes visible as appropriate:

```kotlin
store.onLifecycleEvent(RouteLifecycleEvent.Added(routeId))
store.onVisibilityChanged(routeId, visible = true)

// When the route is permanently removed:
store.onVisibilityChanged(routeId, visible = false)
store.onLifecycleEvent(RouteLifecycleEvent.Removed(routeId, lastState = lastRenderModel))
```

Supply `lastState` when consumers need the final render model during removal. The store exposes navigation state through the activity-store context. A host can render a feature through its configured route delegate:

```kotlin
routeEnvironment.routeDelegate.Content(routeId, viewFactory, renderModel)
```

`RouteEnvironment.RouteDelegate` can be overridden to instrument feature initialization, output application, or route content.

## Activity integration

`FormulaAndroid` and `ActivityStore` provide activity-scoped setup, activity lifecycle state, activity results, and access to navigation state. Configure an activity with an `ActivityStore` containing its `NavigationStore`; the navigation host remains responsible for driving route lifecycle and rendering.
