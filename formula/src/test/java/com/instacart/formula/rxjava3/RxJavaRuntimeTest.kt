package com.instacart.formula.rxjava3

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.Snapshot
import com.instacart.formula.test.CountingInspector
import com.instacart.formula.types.InputIdentityFormula
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class RxJavaRuntimeTest {

    @Test fun `toObservable with unit input and no config`() {
        val formula = InputIdentityFormula<Unit>()
        formula.toObservable().test().assertValues(Unit)
    }

    @Test fun `toObservable with unit input and config`() {
        val inspector = CountingInspector()
        val config = RuntimeConfig(inspector = inspector)
        val formula = InputIdentityFormula<Unit>()
        formula.toObservable(config).test().assertValues(Unit)
        inspector.assertEvaluationCount(1)
    }

    @Test fun `toObservable with integer input and no config`() {
        val formula = InputIdentityFormula<Int>()
        formula.toObservable(0).test().assertValues(0)
    }

    @Test fun `toObservable with observable input and no config`() {
        val formula = InputIdentityFormula<Int>()
        val input = Observable.just(0, 1, 2)
        val observer = formula.toObservable(input).test()
        assertThat(observer.values().last()).isEqualTo(2)
    }

    @Test fun `incorporate flow action with rxjava runtime`() {
        val formula = object : Formula<Unit, Int, Int>() {
            override fun initialState(input: Unit): Int = 0

            override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
                return Evaluation(
                    output = state,
                    actions = context.actions {
                        Action.fromFlow { flowOf(1, 2) }.onEvent {
                            transition(it)
                        }
                    }
                )
            }
        }

        val observer = formula.toObservable().test()
        observer.assertValues(2)
    }
}