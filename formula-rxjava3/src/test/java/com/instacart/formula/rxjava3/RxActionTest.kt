package com.instacart.formula.rxjava3

import com.instacart.formula.test.test
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class RxActionTest {

    @Test fun `fromObservable emits multiple events`() {
        val observer = RxAction
            .fromObservable { Observable.just("a", "b") }
            .test()

        observer.assertValues("a", "b")
        observer.cancel()
    }

    @Test fun `fromObservable cancellation stops emissions`() {
        val relay = PublishRelay.create<String>()
        val observer = RxAction
            .fromObservable { relay }
            .test()

        observer.assertValues()
        relay.accept("a")
        observer.assertValues("a")

        observer.cancel()
        relay.accept("b")
        observer.assertValues("a")
    }
}