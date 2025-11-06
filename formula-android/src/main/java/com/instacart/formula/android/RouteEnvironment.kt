package com.instacart.formula.android

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
        open fun <DependenciesT, KeyT: RouteKey> initializeFeature(
            routeId: RouteId<KeyT>,
            factory: FeatureFactory<DependenciesT, KeyT>,
            dependencies: DependenciesT,
        ): Feature {
            return factory.initialize(dependencies, routeId)
        }

        /**
         * Called from [FormulaFragment.onCreateView] to instantiate the view.
         */
        open fun createView(
            routeId: RouteId<*>,
            viewFactory: ViewFactory<Any>,
            params: ViewFactory.Params,
        ): FeatureView<Any> {
            return viewFactory.create(params)
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
