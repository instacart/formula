package com.instacart.formula.streams

import com.instacart.formula.rxjava3.RxStream
import io.reactivex.rxjava3.core.Observable

object EmptyStream {
    fun init(key: Any = Unit) = RxStream.fromObservable(key) { Observable.empty<Unit>() }
}
