package com.instacart.formula.integration

import com.instacart.formula.android.Feature

sealed class FeatureEvent {
    data class Init(override val key: ActiveFragment, val feature: Feature<*>): FeatureEvent()
    data class Failure(override val key: ActiveFragment, val error: Throwable): FeatureEvent()
    data class MissingBinding(override val key: ActiveFragment): FeatureEvent()

    abstract val key: ActiveFragment
}