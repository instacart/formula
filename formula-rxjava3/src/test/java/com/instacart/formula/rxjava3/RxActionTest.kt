package com.instacart.formula.rxjava3

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RxActionTest {

    @Test fun `fromObservable emits multiple events`() = runTest {
        RxAction.fromObservable { Observable.just("a", "b") }.test {
            assertValues("a", "b")
            cancel()
        }
    }

    @Test fun `fromObservable cancellation stops emissions`() = runTest {
        val relay = PublishRelay.create<String>()
        RxAction.fromObservable { relay }.test {
            assertValues()

            relay.accept("a")
            assertValues("a")

            cancel()

            relay.accept("b")
            assertValues("a")
        }
    }

    @Test fun `fromObservable emits error`() = runTest {
        val error = RuntimeException("Test exception")
        val result = kotlin.runCatching {
            RxAction.fromObservable { Observable.error<String>(error) }.test {}
        }
        assertThat(result.exceptionOrNull()).hasMessageThat().isEqualTo(
            "Expected no errors, but got: [java.lang.RuntimeException: Test exception]"
        )
    }
}