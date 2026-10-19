package com.instacart.formula.android.internal

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.FeatureEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StateFlowFeatureAction internal constructor(
    private val routeEnvironment: RouteEnvironment,
    private val init: FeatureEvent.Init,
) : Action<Any> {

    override fun key(): Any = init.id

    override fun start(scope: CoroutineScope, emitter: Action.Emitter<Any>): Cancelable {
        val job = scope.launchFeature(init, routeEnvironment, emitter::onEvent)
        return Cancelable(job::cancel)
    }
}

internal fun CoroutineScope.launchFeature(init: FeatureEvent.Init, environment: RouteEnvironment, onEvent: (Any) -> Unit): Job {
    val feature = init.feature
    return launch(start = CoroutineStart.UNDISPATCHED) {
        try {
            val featureScope = this
            val stateFlow = if (feature.initAsync) {
                withContext(environment.asyncCoroutineDispatcher) {
                    feature.factory(featureScope)
                }
            } else {
                feature.factory(featureScope)
            }
            withContext(Dispatchers.Unconfined) {
                stateFlow.collect(onEvent)
            }
        } catch (e: Throwable) {
            if (e !is CancellationException) {
                environment.onScreenError(init.id.key, e)
            }
        }
    }
}