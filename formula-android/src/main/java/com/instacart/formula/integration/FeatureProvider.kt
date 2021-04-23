package com.instacart.formula.integration

internal interface FeatureProvider {
    fun getFeature(key: ActiveFragment): FeatureEvent?
}