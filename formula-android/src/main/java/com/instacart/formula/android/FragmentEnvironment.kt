package com.instacart.formula.android

data class FragmentEnvironment(
    val logger: (String) -> Unit = {},
    val onScreenError: (FragmentKey, Throwable) -> Unit = { _, it -> throw it },
    val eventListener: EventListener? = null,
) {

    /**
     * Introspection API to track various formula fragment events and their performance.
     */
    interface EventListener {

        /**
         * Called after [FeatureFactory.initialize] is called.
         */
        fun onFeatureInitialized(fragmentId: FragmentId, durationInMillis: Long)

        /**
         * Called when [FormulaFragment] view is inflated.
         */
        fun onViewInflated(fragmentId: FragmentId, durationInMillis: Long)

        /**
         * Called after render model was applied to the [FeatureView].
         */
        fun onRendered(fragmentId: FragmentId, durationInMillis: Long)

        /**
         * Called after first render model is rendered. The [durationInMillis] starts
         * when formula fragment is initialized and ends after first render model is applied.
         */
        fun onFirstModelRendered(fragmentId: FragmentId, durationInMillis: Long)
    }
}
