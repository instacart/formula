package com.instacart.formula.coroutines

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.instacart.formula.Evaluation
import com.instacart.formula.FormulaPlugins
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.plugin.Plugin
import com.instacart.formula.test.CountingInspector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FlowRuntimeTest {



    @Test
    fun `toFlow with unit input and no config`() {
        val formula = object : StatelessFormula<Unit, String>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<String> {
                return Evaluation("output")
            }
        }

        runTest {
            formula.toFlow().test {
                Truth.assertThat(awaitItem()).isEqualTo("output")
            }
        }
    }

    @Test
    fun `toFlow with unit input and config`() {
        val formula = object : StatelessFormula<Unit, String>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<String> {
                return Evaluation("output")
            }
        }

        val inspector = CountingInspector()
        runTest {
            formula.toFlow(config = RuntimeConfig(inspector = inspector)).test {
                Truth.assertThat(awaitItem()).isEqualTo("output")
            }
        }
        inspector.assertEvaluationCount(1)
    }

    @Test
    fun `toFlow with input value and no config`() {
        val formula = object : StatelessFormula<String, String>() {
            override fun Snapshot<String, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(input)
            }
        }

        runTest {
            formula.toFlow("output").test {
                Truth.assertThat(awaitItem()).isEqualTo("output")
            }
        }
    }

    @Test
    fun `toFlow with input value and config`() {
        val formula = object : StatelessFormula<String, String>() {
            override fun Snapshot<String, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(input)
            }
        }

        val inspector = CountingInspector()
        runTest {
            formula.toFlow("output", RuntimeConfig(inspector = inspector)).test {
                Truth.assertThat(awaitItem()).isEqualTo("output")
            }
        }
        inspector.assertEvaluationCount(1)
    }

    @Test
    fun `toFlow with input flow and no config`() {
        val formula = object : StatelessFormula<String, String>() {
            override fun Snapshot<String, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(input)
            }
        }

        runTest {
            formula.toFlow(flowOf("output", "output-2")).test {
                Truth.assertThat(awaitItem()).isEqualTo("output")
                Truth.assertThat(awaitItem()).isEqualTo("output-2")
            }
        }
    }

    @Test
    fun `toFlow with input flow and config`() {
        val formula = object : StatelessFormula<String, String>() {
            override fun Snapshot<String, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(input)
            }
        }

        val inspector = CountingInspector()
        runTest {
            formula.toFlow(
                input = flowOf("output", "output-2"),
                config = RuntimeConfig(inspector = inspector),
            ).test {
                Truth.assertThat(awaitItem()).isEqualTo("output")
                Truth.assertThat(awaitItem()).isEqualTo("output-2")
            }
        }
        inspector.assertEvaluationCount(2)
    }

    @Test
    fun `toFlow with input flow with duplicate values`() {
        val formula = object : StatelessFormula<String, String>() {
            override fun Snapshot<String, Unit>.evaluate(): Evaluation<String> {
                return Evaluation(input)
            }
        }

        val inspector = CountingInspector()
        runTest {
            formula.toFlow(
                input = flowOf("output", "output", "output"),
                config = RuntimeConfig(inspector = inspector),
            ).test {
                Truth.assertThat(awaitItem()).isEqualTo("output")
            }
        }
        inspector.assertEvaluationCount(1)
    }

    @Test fun `flow action throws error`() {
        val formula = object : StatelessFormula<Unit, Unit>() {
            override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
                return Evaluation(
                    output = Unit,
                    actions = context.actions {
                        FlowAction.fromFlow<Unit> {
                            flow { throw IllegalStateException("crashed") }
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

            runTest {
                formula.toFlow(Unit).test {
                    awaitItem() // Initial event
                }

                Truth.assertThat(errors).hasSize(1)
            }
        } finally {
            FormulaPlugins.setPlugin(null)
        }
    }
}