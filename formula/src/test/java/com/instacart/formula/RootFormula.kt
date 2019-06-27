package com.instacart.formula

import com.instacart.formula.timer.TimerEffect
import com.instacart.formula.timer.TimerFormula
import com.instacart.formula.timer.TimerRenderModel

class RootFormula(
    private val timerFormula: TimerFormula
) : Formula<Unit, RootFormula.State, Unit, RootFormula.RenderModel> {

    data class State(val count: Int = 0, val showTimer: Boolean = true)

    class RenderModel(
        val timer: TimerRenderModel?,
        val openTimer: () -> Unit
    )

    override fun initialState(input: Unit): State = State()

    override fun evaluate(
        input: Unit,
        state: State,
        context: FormulaContext<State, Unit>
    ): Evaluation<RenderModel> {

        val timer = if (state.showTimer) {
            context.child(timerFormula, Unit, onEvent = {
                when (it) {
                    is TimerEffect.Exit -> transition(state.copy(showTimer = false))
                }
            })
        } else {
            null
        }

        return Evaluation(
            renderModel = RenderModel(
                timer = timer,
                openTimer = context.callback {
                    transition(state.copy(showTimer = true))
                }
            )
        )
    }
}
