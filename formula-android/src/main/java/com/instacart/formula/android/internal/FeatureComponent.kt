package com.instacart.formula.android.internal

import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.FragmentKey

class FeatureComponent<in Component>(
    private val component: Component,
    private val bindings: List<FeatureBinding<Component, *>>,
) {

    fun init(environment: FragmentEnvironment, fragmentId: FragmentId): FeatureEvent {
        val initialized = try {
            bindings.firstNotNullOfOrNull { binding ->
                if (binding.type.isInstance(fragmentId.key)) {
                    val featureFactory = binding.feature as FeatureFactory<Component, FragmentKey>
                    val feature = environment.fragmentDelegate.initializeFeature(
                        fragmentId = fragmentId,
                        factory = featureFactory,
                        dependencies = component,
                        key = fragmentId.key,
                    )
                    FeatureEvent.Init(fragmentId, feature)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            FeatureEvent.Failure(fragmentId, e)
        }

        return initialized ?: FeatureEvent.MissingBinding(fragmentId)
    }
}