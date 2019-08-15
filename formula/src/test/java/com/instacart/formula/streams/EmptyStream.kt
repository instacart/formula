package com.instacart.formula.streams

import com.instacart.formula.RxStream
import io.reactivex.Observable

class EmptyStream : RxStream<Unit, Unit> {
    override fun observable(parameter: Unit): Observable<Unit> {
        return Observable.empty()
    }
}
