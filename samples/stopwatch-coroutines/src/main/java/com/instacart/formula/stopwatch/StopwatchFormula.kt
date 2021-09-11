package com.instacart.formula.stopwatch

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.coroutines.FlowStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

class StopwatchFormula(private val scope: CoroutineScope = MainScope()) : Formula<Unit, StopwatchFormula.State, StopwatchRenderModel> {

    data class State(
        val timePassedInMillis: Long,
        val isRunning: Boolean
    )

    private val analytics = StopwatchAnalytics()

    override fun initialState(input: Unit): State = State(
        timePassedInMillis = 0,
        isRunning = true
    )

    override fun evaluate(
        input: Unit,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<StopwatchRenderModel> {
        return Evaluation(
            output = StopwatchRenderModel(
                timePassed = formatTimePassed(state.timePassedInMillis),
            ),
            updates = context.updates {
                if (state.isRunning) {
                    val incrementTimePassed = FlowStream.fromFlow(scope) {

                        flow {
                            while(true) {
                                emit(Unit)
                                delay(1)
                            }
                        }
                    }

                    incrementTimePassed.onEvent {
                        transition(state.copy(timePassedInMillis = state.timePassedInMillis + 1))
                    }
                }
            }
        )
    }

    private fun formatTimePassed(timePassedInMillis: Long): String {
        return buildString {
            val minutesPassed = TimeUnit.MILLISECONDS.toMinutes(timePassedInMillis)
            if (minutesPassed > 0) {
                append(minutesPassed)
                append('m')
                append(' ')
            }

            val secondsPassed = TimeUnit.MILLISECONDS.toSeconds(timePassedInMillis) % 60
            append(secondsPassed)
            append('s')
            append(' ')

            // Always show millis as two digits
            val millisPassed = (timePassedInMillis % 1000) / 10
            if (millisPassed < 10) {
                append('0')
            }
            append(millisPassed)
        }
    }

}
