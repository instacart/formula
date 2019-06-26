package com.instacart.formula.timer

import com.instacart.formula.FormulaContext
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Transition

class TimerFormula(
    private val timer: Timer
) : Formula<Unit, TimerState, TimerEffect, TimerRenderModel> {

    override fun initialState(input: Unit): TimerState = TimerState()

    override fun evaluate(
        input: Unit,
        state: TimerState,
        context: FormulaContext<TimerState, TimerEffect>
    ): Evaluation<TimerRenderModel> {
        return Evaluation(
            updates = context.updates {
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
