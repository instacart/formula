package com.instacart.formula.integration

import com.instacart.formula.android.Feature
import com.instacart.formula.fragment.FragmentKey

sealed class FeatureEvent {
    data class Init(override val key: FragmentKey, val feature: Feature<*>): FeatureEvent()
    data class Failure(override val key: FragmentKey, val error: Throwable): FeatureEvent()
    data class MissingBinding(override val key: FragmentKey): FeatureEvent()

    abstract val key: FragmentKey
}