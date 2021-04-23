package com.instacart.formula.integration

internal interface FeatureProvider {
    fun getFeature(id: FragmentId): FeatureEvent?
}