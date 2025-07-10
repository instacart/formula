package com.instacart.formula.android.internal

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.StateFlowFeature
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StateFlowFeatureAction internal constructor(
    private val asyncDispatcher: CoroutineDispatcher,
    private val fragmentEnvironment: FragmentEnvironment,
    private val fragmentId: FragmentId,
    private val feature: StateFlowFeature,
) : Action<Any> {

    override fun key(): Any = fragmentId

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
                    fragmentEnvironment.onScreenError(fragmentId.key, e)
                }
            }
        }

        return Cancelable(job::cancel)
    }
}