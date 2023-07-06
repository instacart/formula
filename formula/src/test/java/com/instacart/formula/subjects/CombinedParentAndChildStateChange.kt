package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.ActionBuilder
import com.instacart.formula.types.ActionDelegateFormula

object CombinedParentAndChildStateChange {

    fun formula() = run {
        val actionFormula = ActionDelegateFormula()
        HasChildFormula(actionFormula) {
            ActionDelegateFormula.Input(
                delegateAction = UpdateStateAction,
                onAction = callback {
                    transition(state + 1)
                }
            )
        }
    }

    private object UpdateStateAction : ActionDelegateFormula.DelegateAction {
        override fun ActionBuilder<ActionDelegateFormula.Input, Int>.runAction() {
            Action.onInit().onEvent {
                transition(state + 1) {
                    input.onAction()
                }
            }
        }
    }
}