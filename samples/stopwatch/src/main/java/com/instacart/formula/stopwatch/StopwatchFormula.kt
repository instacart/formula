package com.instacart.formula.stopwatch

import android.os.SystemClock
import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

class StopwatchFormula(
    private val elapsedSinceBoot: () -> Long = { SystemClock.elapsedRealtime() },
) : Formula<Unit, StopwatchFormula.State, StopwatchRenderModel>() {

    data class State(
        val elapsedBeforeCurrentRunMillis: Long = 0,
        val startedAtElapsedRealtimeMillis: Long? = null,
        val currentElapsedRealtimeMillis: Long = 0,
    ) {
        val isRunning: Boolean
            get() = startedAtElapsedRealtimeMillis != null

        val elapsedTimeMillis: Long
            get() = elapsedBeforeCurrentRunMillis +
                (startedAtElapsedRealtimeMillis?.let { currentElapsedRealtimeMillis - it } ?: 0)
    }

    override fun initialState(input: Unit): State = State(
        currentElapsedRealtimeMillis = elapsedSinceBoot(),
    )

    override fun Snapshot<Unit, State>.evaluate(): Evaluation<StopwatchRenderModel> {
        return Evaluation(
            output = StopwatchRenderModel(
                timePassed = formatTimePassed(state.elapsedTimeMillis),
                startStopButton = startStopButton(),
                resetButton = resetButton(),
            ),
            actions = context.actions {
                if (state.isRunning) {
                    Action.fromFlow {
                        flow {
                            while (true) {
                                delay(1)
                                emit(elapsedSinceBoot())
                            }
                        }
                    }.onEvent { currentElapsedRealtimeMillis ->
                        transition(state.copy(currentElapsedRealtimeMillis = currentElapsedRealtimeMillis))
                    }
                }
            },
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
            val millis = (timePassedInMillis % 1000) / 10
            if (millis < 10) {
                append('0')
            }
            append(millis)
        }
    }

    private fun Snapshot<*, State>.startStopButton(): ButtonRenderModel {
        return ButtonRenderModel(
            text = when {
                state.isRunning -> "Stop"
                state.elapsedTimeMillis > 0 -> "Resume"
                else -> "Start"
            },
            onSelected = context.onEvent {
                val now = elapsedSinceBoot()
                val nextState = if (state.isRunning) {
                    state.copy(
                        elapsedBeforeCurrentRunMillis = state.elapsedBeforeCurrentRunMillis +
                            (now - checkNotNull(state.startedAtElapsedRealtimeMillis)),
                        startedAtElapsedRealtimeMillis = null,
                        currentElapsedRealtimeMillis = now,
                    )
                } else {
                    state.copy(
                        startedAtElapsedRealtimeMillis = now,
                        currentElapsedRealtimeMillis = now,
                    )
                }
                transition(nextState)
            },
        )
    }

    private fun Snapshot<*, State>.resetButton(): ButtonRenderModel {
        return ButtonRenderModel(
            text = "Reset",
            onSelected = context.onEvent {
                transition(
                    State(currentElapsedRealtimeMillis = elapsedSinceBoot()),
                )
            },
        )
    }
}
