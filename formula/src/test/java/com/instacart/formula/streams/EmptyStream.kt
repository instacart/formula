package com.instacart.formula.streams

import com.instacart.formula.RxStream
import io.reactivex.Observable

object EmptyStream {
    fun init(key: Any = Unit) = RxStream.fromObservable(key) { Observable.empty<Unit>() }
}
