package com.instacart.formula.android.internal

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.android.RouteEnvironment
import com.instacart.formula.android.RouteId
import com.instacart.formula.android.RxJavaFeature
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope

class FeatureObservableAction internal constructor(
    private val routeEnvironment: RouteEnvironment,
    private val routeId: RouteId<*>,
    private val feature: RxJavaFeature,
) : Action<Any> {

    override fun key(): Any = routeId

    override fun start(scope: CoroutineScope, emitter: Action.Emitter<Any>): Cancelable {
        val observable = feature.stateObservable.onErrorResumeNext {
            routeEnvironment.onScreenError(routeId.key, it)
            Observable.empty()
        }

        val disposable = observable.subscribe(emitter::onEvent, emitter::onError)
        return Cancelable(disposable::dispose)
    }
}