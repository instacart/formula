package com.instacart.formula.rxjava3

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ObservableFormulaTest {

    @Test fun `initial value`() {
        val formula = object : ObservableFormula<String, Int>() {
            override fun initialValue(input: String): Int = 0

            override fun observable(input: String): Observable<Int> {
                return Observable.empty()
            }
        }

        formula.test()
            .input("initial")
            .apply {
                assertThat(values()).containsExactly(0).inOrder()
            }
    }

    @Test fun `initial value and then subsequent events`() {
        val relay = PublishRelay.create<Int>()
        val formula = object : ObservableFormula<String, Int>() {
            override fun initialValue(input: String): Int = 0

            override fun observable(input: String): Observable<Int> {
                return relay
            }
        }

        formula.test()
            .input("initial")
            .apply {
                relay.accept(1)
                relay.accept(2)
                relay.accept(3)
            }
            .apply {
                assertThat(values()).containsExactly(0, 1, 2, 3).inOrder()
            }
    }

    @Test fun `resets state when input changes`() {
        val incrementingOutput = AtomicInteger(0)
        val formula = object : ObservableFormula<String, Int>() {
            override fun initialValue(input: String): Int = 0

            override fun observable(input: String): Observable<Int> {
                return Observable.fromCallable {
                    incrementingOutput.getAndIncrement()
                }
            }
        }

        formula.test()
            .input("initial")
            .input("reset")
            .apply {
                assertThat(values()).containsExactly(0, 1).inOrder()
            }
    }
}