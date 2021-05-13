package com.instacart.formula.android

sealed class FeatureEvent {
    data class Init(override val id: FragmentId, val feature: Feature<*>): FeatureEvent()
    data class Failure(override val id: FragmentId, val error: Throwable): FeatureEvent()
    data class MissingBinding(override val id: FragmentId): FeatureEvent()

    abstract val id: FragmentId
}