package com.instacart.formula.android.internal

import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.RouteKey

/**
 * Defines how a specific key should be bound to its [FeatureFactory]
 */
class FeatureBinding<in Dependencies, Key : RouteKey>(
    val type: Class<Key>,
    val feature: FeatureFactory<Dependencies, Key>,
)
