package com.instacart.formula.android.internal

import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FeatureFactory
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.RouteId
import com.instacart.formula.android.RouteKey

class FeatureComponent<in Component>(
    private val component: Component,
    private val bindings: List<FeatureBinding<Component, *>>,
) {

    fun init(environment: RouteEnvironment, routeId: RouteId<*>): FeatureEvent {
        val initialized = try {
            bindings.firstNotNullOfOrNull { binding ->
                if (binding.type.isInstance(routeId.key)) {
                    val featureFactory = binding.feature as FeatureFactory<Component, RouteKey>
                    val feature = environment.routeDelegate.initializeFeature(
                        routeId = routeId,
                        factory = featureFactory,
                        dependencies = component,
                    )
                    FeatureEvent.Init(routeId, feature)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            FeatureEvent.Failure(routeId, e)
        }

        return initialized ?: FeatureEvent.MissingBinding(routeId)
    }
}