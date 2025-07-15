package com.instacart.formula.android

data class FragmentEnvironment(
    val logger: (String) -> Unit = {},
    val onScreenError: (FragmentKey, Throwable) -> Unit = { _, it -> throw it },
    val fragmentDelegate: FragmentDelegate = FragmentDelegate(),
) {

    /**
     * Introspection API to track various formula fragment events and their performance.
     */
    open class FragmentDelegate {

        /**
         * Instantiates the feature.
         */
        open fun <DependenciesT, KeyT: FragmentKey> initializeFeature(
            fragmentId: FragmentId<KeyT>,
            factory: FeatureFactory<DependenciesT, KeyT>,
            dependencies: DependenciesT,
        ): Feature {
            return factory.initialize(dependencies, fragmentId)
        }

        /**
         * Called from [FormulaFragment.onCreateView] to instantiate the view.
         */
        open fun createView(
            fragmentId: FragmentId<*>,
            viewFactory: ViewFactory<Any>,
            params: ViewFactory.Params,
        ): FeatureView<Any> {
            return viewFactory.create(params)
        }

        /**
         * Called when we are ready to apply [output] to the view.
         */
        open fun setOutput(fragmentId: FragmentId<*>, output: Any, applyOutputToView: (Any) -> Unit) {
            applyOutputToView(output)
        }
    }
}
