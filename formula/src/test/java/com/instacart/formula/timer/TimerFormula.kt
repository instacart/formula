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
                        transition(state.copy(time = state.time + 1))
                    })
                }
            },
            renderModel = TimerRenderModel(
                time = "Time: ${state.time}",
                onResetSelected = context.callback {
                    transition(state.copy(time = 0, runTimer = false))
                },
                onClose = context.callback {
                    output(TimerEffect.Exit)
                }
            )
        )
    }
}

sealed class TimerEffect {
    object Exit : TimerEffect()
}
