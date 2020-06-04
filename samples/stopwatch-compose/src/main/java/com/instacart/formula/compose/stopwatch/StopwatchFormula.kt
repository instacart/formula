package com.instacart.formula.compose.stopwatch

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.rxjava3.RxStream
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class StopwatchFormula : Formula<Unit, StopwatchFormula.State, StopwatchRenderModel> {

    data class State(
        val timePassedInMillis: Long,
        val isRunning: Boolean
    )

    override fun initialState(input: Unit): State = State(
        timePassedInMillis = 0,
        isRunning = false
    )

    override fun evaluate(
        input: Unit,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<StopwatchRenderModel> {
        return Evaluation(
            output = StopwatchRenderModel(
                timePassed = formatTimePassed(state.timePassedInMillis),
                startStopButton = startStopButton(state, context),
                resetButton = resetButton(state, context)
            ),
            updates = context.updates {
                if (state.isRunning) {
                    val incrementTimePassed = RxStream.fromObservable {
                        Observable.interval(1, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                    }

                    events(incrementTimePassed) {
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

    private fun startStopButton(state: State, context: FormulaContext<State>): ButtonRenderModel {
        return ButtonRenderModel(
            text = when {
                state.isRunning -> "Stop"
                state.timePassedInMillis > 0 -> "Resume"
                else -> "Start"
            },
            onSelected = context.callback {
                transition(state.copy(isRunning = !state.isRunning))
            }
        )
    }

    private fun resetButton(state: State, context: FormulaContext<State>): ButtonRenderModel {
        return ButtonRenderModel(
            text = "Reset",
            onSelected = context.callback {
                transition(state.copy(timePassedInMillis = 0, isRunning = false))
            }
        )
    }
}
