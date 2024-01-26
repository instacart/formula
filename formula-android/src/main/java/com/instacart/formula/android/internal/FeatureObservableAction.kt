package com.instacart.formula.android.internal

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentId
import io.reactivex.rxjava3.core.Observable

class FeatureObservableAction(
    private val fragmentEnvironment: FragmentEnvironment,
    private val fragmentId: FragmentId,
    private val feature: Feature<*>,
) : Action<Any> {

    override fun key(): Any = fragmentId

    override fun start(send: (Any) -> Unit): Cancelable {
        val observable = feature.state.onErrorResumeNext {
            fragmentEnvironment.onScreenError(fragmentId.key, it)
            Observable.empty()
        }

        // We ensure all feature state updates come on the main thread.
        val androidUpdateScheduler = AndroidUpdateScheduler(send)
        val disposable = observable.subscribe(androidUpdateScheduler::emitUpdate)
        return Cancelable(disposable::dispose)
    }
}