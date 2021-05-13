package com.instacart.formula.android.internal

import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FragmentId

internal interface FeatureProvider {
    fun getFeature(id: FragmentId): FeatureEvent?
}