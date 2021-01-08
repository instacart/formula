package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.rxjava3.ObservableFormula
import com.instacart.formula.test.test
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class ObservableFormulaTest {

    @Test
    fun `initial value`() {
        SubjectFormula()
            .test("initial")
            .apply {
                Truth.assertThat(values()).containsExactly(0).inOrder()
            }
    }

    @Test
    fun `initial value and subsequent events from relay`() {
        SubjectFormula()
            .test("initial")
            .apply {
                formula.relay.accept(1)
                formula.relay.accept(2)
                formula.relay.accept(3)
            }
            .apply {
                Truth.assertThat(values()).containsExactly(0, 1, 2, 3).inOrder()
            }
    }

    @Test
    fun `new input restarts formula`() {
        val inputRelay = PublishRelay.create<String>()
        SubjectFormula()
            .test(inputRelay)
            .apply { inputRelay.accept("initial") }
            .apply { formula.relay.accept(1) }
            .apply { inputRelay.accept("reset") }
            .apply { formula.relay.accept(1) }
            .apply {
                Truth.assertThat(values()).containsExactly(0, 1, 0, 1).inOrder()
            }
    }

    internal class SubjectFormula : ObservableFormula<String, Int>() {
        val relay: PublishRelay<Int> = PublishRelay.create()

        override fun initialValue(input: String): Int = 0

        override fun observable(input: String): Observable<Int> {
            return relay
        }
    }
}