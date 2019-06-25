package com.instacart.formula.timer

import com.instacart.formula.RxStream
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit

class Timer(private val scheduler: TestScheduler) : RxStream<Unit, Long> {

    override fun observable(input: Unit): Observable<Long> {
        return Observable.interval(1, 1, TimeUnit.SECONDS, scheduler)
    }
}
