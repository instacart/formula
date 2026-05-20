package com.instacart.formula.android

import androidx.compose.runtime.Composable

data class RouteEnvironment(
    val logger: (String) -> Unit = {},
    val onScreenError: (RouteKey, Throwable) -> Unit = { _, it -> throw it },
    val routeDelegate: RouteDelegate = RouteDelegate(),
) {

    /**
     * Introspection API to track various formula fragment events and their performance.
     */
    open class RouteDelegate {

        /**
         * Instantiates the feature.
         */
        open fun <DependenciesT, KeyT : RouteKey> initializeFeature(
            routeId: RouteId<KeyT>,
            factory: FeatureFactory<DependenciesT, KeyT>,
            dependencies: DependenciesT,
        ): Feature {
            return factory.initialize(dependencies, routeId)
        }

        /**
         * Renders the route's content. The default implementation dispatches over the
         * sealed [ViewFactory] hierarchy. Hosts may override to wrap rendering with
         * telemetry, [androidx.compose.runtime.CompositionLocalProvider] for side-band
         * data (e.g. page keys), or error boundaries.
         */
        @Composable
        open fun Content(routeId: RouteId<*>, viewFactory: ViewFactory<Any>, model: Any) {
            when (viewFactory) {
                is ComposeViewFactory -> viewFactory.Content(model)
            }
        }

        /**
         * Called when we are ready to apply [output] to the view.
         */
        open fun setOutput(routeId: RouteId<*>, output: Any, applyOutputToView: (Any) -> Unit) {
            applyOutputToView(output)
        }
    }
}

@Deprecated(
    message = "FragmentEnvironment has been renamed to RouteEnvironment",
    replaceWith = ReplaceWith("RouteEnvironment", "com.instacart.formula.android.RouteEnvironment")
)
typealias FragmentEnvironment = RouteEnvironment
