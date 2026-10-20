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
    private val event: FeatureEvent.Init,
) : Action<Any> {

    override fun key(): Any = event.id

    override fun start(scope: CoroutineScope, emitter: Action.Emitter<Any>): Cancelable {
        val job = scope.launchFeature(event, routeEnvironment, emitter::onEvent)
        return Cancelable(job::cancel)
    }
}

internal fun CoroutineScope.launchFeature(event: FeatureEvent.Init, environment: RouteEnvironment, onEvent: (Any) -> Unit): Job {
    val feature = event.feature
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
                environment.onScreenError(event.id.key, e)
            }
        }
    }
}