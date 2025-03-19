package com.instacart.formula.rxjava3

import com.google.common.truth.Truth
import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaPlugins
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.coroutines.FlowAction
import com.instacart.formula.plugin.Plugin
import com.instacart.formula.test.CountingInspector
import com.instacart.formula.test.test
import com.instacart.formula.types.InputIdentityFormula
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.flow
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
        formula.toObservable(Observable.just(0, 1, 2)).test().assertValues(0, 1, 2)
    }

    @Test fun `rx action throws an error`() {
        val formula = object : StatelessFormula<Unit, Unit>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
                return Evaluation(
                    output = Unit,
                    actions = context.actions {
                        RxAction.fromObservable<Unit> {
                            Observable.error(IllegalStateException("crashed"))
                        }.onEvent {
                            none()
                        }
                    }
                )
            }
        }

        try {
            val errors = mutableListOf<Throwable>()
            val plugin = object : Plugin {
                override fun onUnhandledActionError(formulaType: Class<*>, error: Throwable) {
                    errors.add(error)
                }
            }

            FormulaPlugins.setPlugin(plugin)

            formula.test().input(Unit)

            Truth.assertThat(errors).hasSize(1)
        } finally {
            FormulaPlugins.setPlugin(null)
        }
    }
}