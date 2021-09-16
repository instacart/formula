package com.instacart.formula.stopwatch

import android.util.Log
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Stream
import com.instacart.formula.coroutines.FlowStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

class StopwatchFormula : Formula<Unit, StopwatchFormula.State, StopwatchRenderModel> {

    data class State(
        val timePassedInMillis: Long,
        val isRunning: Boolean
    )

    override fun initialState(input: Unit): State = State(
        timePassedInMillis = 0,
        isRunning = true
    )

    @ExperimentalStdlibApi
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
                    val incrementTimePassed = FlowStream.fromFlow {
                        ticker()
                    }

                    incrementTimePassed.onEvent {
                        transition(state.copy(timePassedInMillis = state.timePassedInMillis + 1))
                    }
                }

                Stream.onTerminate().onEvent {
                    transition {
                        Log.d("Formula", "Exit")
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

fun ticker() = flow {
    while (true) {
        emit(Unit)
        delay(1)
    }
}