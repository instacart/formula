package com.instacart.formula.android.internal

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.ViewGroup
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

    @Suppress("UNCHECKED_CAST")
    override fun create(inflater: LayoutInflater, container: ViewGroup?): FeatureView<Any> {
        val start = SystemClock.uptimeMillis()
        val key = fragmentId.key
        val featureEvent = featureProvider.getFeature(fragmentId) ?: throw IllegalStateException("Could not find feature for $key.")
        val viewFactory = factory ?: when (featureEvent) {
            is FeatureEvent.MissingBinding -> {
                throw IllegalStateException("Missing feature factory or integration for $key. Please check your FragmentFlowStore configuration.")
            }
            is FeatureEvent.Failure -> {
                throw IllegalStateException("Feature failed to initialize: $key", featureEvent.error)
            }
            is FeatureEvent.Init -> {
                featureEvent.feature.viewFactory as ViewFactory<Any>
            }
        }
        this.factory = viewFactory
        val view = viewFactory.create(inflater, container)
        val endTime = SystemClock.uptimeMillis()
        environment.eventListener?.onViewInflated(fragmentId, endTime - start)
        return view
    }
}