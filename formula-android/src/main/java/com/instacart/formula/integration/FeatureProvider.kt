package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentKey

internal interface FeatureProvider {
    fun getFeature(key: FragmentKey): FeatureEvent?
}