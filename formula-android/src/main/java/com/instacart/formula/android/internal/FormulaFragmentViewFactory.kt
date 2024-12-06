package com.instacart.formula.android.internal

import androidx.annotation.VisibleForTesting
import com.instacart.formula.android.FeatureView
import com.instacart.formula.android.ViewFactory
import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentId
import java.lang.IllegalStateException

internal class FormulaFragmentViewFactory(
    private val environment: FragmentEnvironment,
    private val fragmentId: FragmentId,
    private val featureProvider: FeatureProvider,
) : ViewFactory<Any> {

    private var factory: ViewFactory<Any>? = null

    override fun create(params: ViewFactory.Params): FeatureView<Any> {
        val viewFactory = viewFactory()
        val delegate = environment.fragmentDelegate
        return delegate.createView(fragmentId, viewFactory, params)
    }

    @VisibleForTesting
    internal fun viewFactory(): ViewFactory<Any> {
        return factory ?: findViewFactory().apply {
            factory = this
        }
    }

    private fun findViewFactory(): ViewFactory<Any> {
        val key = fragmentId.key
        val featureEvent = featureProvider.getFeature(fragmentId) ?: throw IllegalStateException("Could not find feature for $key.")
        return when (featureEvent) {
            is FeatureEvent.MissingBinding -> {
                throw IllegalStateException("Missing feature factory or integration for $key. Please check your FragmentStore configuration.")
            }
            is FeatureEvent.Failure -> {
                throw IllegalStateException("Feature failed to initialize: $key", featureEvent.error)
            }
            is FeatureEvent.Init -> {
                featureEvent.feature.viewFactory
            }
        }
    }
}