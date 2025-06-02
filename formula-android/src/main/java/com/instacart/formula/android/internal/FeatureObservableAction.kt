package com.instacart.formula.android.internal

import com.instacart.formula.Action
import com.instacart.formula.Cancelable
import com.instacart.formula.android.Feature
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentId
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope

class FeatureObservableAction(
    private val fragmentEnvironment: FragmentEnvironment,
    private val fragmentId: FragmentId,
    private val feature: Feature,
) : Action<Any> {

    override fun key(): Any = fragmentId

    override fun start(scope: CoroutineScope, send: (Any) -> Unit): Cancelable {
        val observable = feature.stateObservable.onErrorResumeNext {
            fragmentEnvironment.onScreenError(fragmentId.key, it)
            Observable.empty()
        }

        val disposable = observable.subscribe(send)
        return Cancelable(disposable::dispose)
    }
}