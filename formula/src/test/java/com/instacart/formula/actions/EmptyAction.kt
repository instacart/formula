package com.instacart.formula.actions

import com.instacart.formula.rxjava3.RxAction
import io.reactivex.rxjava3.core.Observable

object EmptyAction {
    fun init(key: Any = Unit) = RxAction.fromObservable(key) { Observable.empty<Unit>() }
}