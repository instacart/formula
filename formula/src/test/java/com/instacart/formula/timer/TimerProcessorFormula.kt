package com.instacart.formula.timer

import com.instacart.formula.FormulaContext
import com.instacart.formula.ProcessResult
import com.instacart.formula.ProcessorFormula
import com.instacart.formula.Transition

class TimerProcessorFormula(
    private val timer: Timer
) : ProcessorFormula<Unit, TimerState, TimerEffect, TimerRenderModel> {

    override fun initialState(input: Unit): TimerState = TimerState()

    override fun process(
        input: Unit,
        state: TimerState,
        context: FormulaContext<TimerState, TimerEffect>
    ): ProcessResult<TimerRenderModel> {
        return ProcessResult(
            streams = context.streams {
                if (state.runTimer) {
                    events(timer, onEvent = {
                        Transition(state.copy(time = state.time + 1))
                    })
                }
            },
            renderModel = TimerRenderModel(
                time = "Time: ${state.time}",
                onResetSelected = {
                    context.transition(state.copy(time = 0, runTimer = false))
                },
                onStart = {
                    if (!state.runTimer) {
                        context.transition(state.copy(runTimer = true))
                    }
                },
                onClose = {
                    context.transition(state, TimerEffect.Exit)
                }
            )
        )
    }
}

sealed class TimerEffect {
    object Exit : TimerEffect()
}
