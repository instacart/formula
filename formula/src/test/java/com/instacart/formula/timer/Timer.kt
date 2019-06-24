package com.instacart.formula.timer

import com.instacart.formula.Stream
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit

class Timer(
    private val scheduler: TestScheduler
) : Stream<Unit, Long> {
    override fun subscribe(input: Unit, onEvent: (Long) -> Unit): Disposable {
        return Observable.interval(1, 1, TimeUnit.SECONDS, scheduler).subscribe(onEvent)
    }
}
