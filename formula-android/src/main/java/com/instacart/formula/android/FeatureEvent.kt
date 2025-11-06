package com.instacart.formula.android

sealed class FeatureEvent {
    data class Init(override val id: RouteId<*>, val feature: Feature): FeatureEvent()
    data class Failure(override val id: RouteId<*>, val error: Throwable): FeatureEvent()
    data class MissingBinding(override val id: RouteId<*>): FeatureEvent()

    abstract val id: RouteId<*>
}