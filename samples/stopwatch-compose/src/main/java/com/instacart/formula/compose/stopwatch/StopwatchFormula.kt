package com.instacart.formula.compose.stopwatch

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.rxjava3.RxAction
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class StopwatchFormula : Formula<Unit, StopwatchFormula.State, StopwatchRenderModel>() {

    data class State(
        val timePassedInMillis: Long,
        val isRunning: Boolean
    )

    override fun initialState(input: Unit): State = State(
        timePassedInMillis = 0,
        isRunning = false
    )

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<StopwatchRenderModel> {
        return Evaluation(
            output = StopwatchRenderModel(
                timePassed = formatTimePassed(state.timePassedInMillis),
                startStopButton = startStopButton(),
                resetButton = resetButton()
            ),
            actions = context.actions {
                if (state.isRunning) {
                    val incrementTimePassed = RxAction.fromObservable {
                        Observable.interval(1, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
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

    private fun Snapshot<*, State>.startStopButton(): ButtonRenderModel {
        return ButtonRenderModel(
            text = when {
                state.isRunning -> "Stop"
                state.timePassedInMillis > 0 -> "Resume"
                else -> "Start"
            },
            onSelected = context.onEvent {
                transition(state.copy(isRunning = !state.isRunning))
            }
        )
    }

    private fun Snapshot<*, State>.resetButton(): ButtonRenderModel {
        return ButtonRenderModel(
            text = "Reset",
            onSelected = context.onEvent {
                transition(state.copy(timePassedInMillis = 0, isRunning = false))
            }
        )
    }
}
