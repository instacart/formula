package com.instacart.formula.android.internal

import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentKey

/**
 * Defines how a specific key should be bound to its [FeatureFactory]
 */
class FeatureBinding<in Dependencies, Key : FragmentKey>(
    val type: Class<Key>,
    val feature: FeatureFactory<Dependencies, Key>,
)
