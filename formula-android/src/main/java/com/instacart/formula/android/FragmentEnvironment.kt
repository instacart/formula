package com.instacart.formula.android

import android.view.LayoutInflater
import android.view.ViewGroup

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
            fragmentId: FragmentId,
            factory: FeatureFactory<DependenciesT, KeyT>,
            dependencies: DependenciesT,
            key: KeyT,
        ): Feature<*> {
            return factory.initialize(dependencies, key)
        }

        /**
         * Called from [FormulaFragment.onCreateView] to instantiate the view.
         */
        open fun createView(
            fragmentId: FragmentId,
            viewFactory: ViewFactory<Any>,
            inflater: LayoutInflater,
            container: ViewGroup?,
        ): FeatureView<Any> {
            return viewFactory.create(inflater, container)
        }

        /**
         * Called when we are ready to apply [output] to the view.
         */
        open fun setOutput(fragmentId: FragmentId, output: Any, applyOutputToView: (Any) -> Unit) {
            applyOutputToView(output)
        }

        /**
         * Called after first render model is rendered. The [durationInMillis] starts
         * when formula fragment is initialized and ends after first render model is applied.
         */
        open fun onFirstModelRendered(fragmentId: FragmentId, durationInMillis: Long) = Unit
    }
}
