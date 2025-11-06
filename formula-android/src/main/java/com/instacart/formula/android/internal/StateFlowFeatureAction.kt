package com.instacart.formula.android.internal

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.RouteId
import com.instacart.formula.android.StateFlowFeature
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StateFlowFeatureAction internal constructor(
    private val asyncDispatcher: CoroutineDispatcher,
    private val routeEnvironment: RouteEnvironment,
    private val routeId: RouteId<*>,
    private val feature: StateFlowFeature,
) : Action<Any> {

    override fun key(): Any = routeId

    override fun start(scope: CoroutineScope, emitter: Action.Emitter<Any>): Cancelable {
        val job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                val featureScope = this
                val stateFlow = if (feature.initAsync) {
                    withContext(asyncDispatcher) {
                        feature.factory(featureScope)
                    }
                } else {
                    feature.factory(featureScope)
                }
                withContext(Dispatchers.Unconfined) {
                    stateFlow.collect { emitter.onEvent(it) }
                }
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    routeEnvironment.onScreenError(routeId.key, e)
                }
            }
        }

        return Cancelable(job::cancel)
    }
}