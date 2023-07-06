package com.instacart.formula.subjects

import com.instacart.formula.types.OnEventFormula

object CombinedParentAndChildStateChangeOnEvent {

    fun formula() = run {
        val outputEventFormula = OnEventFormula<OnEventInput>(
            eventDelegate = {
                context.callback {
                    transition(state + 1) {
                        input.onEvent()
                    }
                }
            }
        )

        HasChildFormula(outputEventFormula) {
            OnEventInput(
                onEvent = callback {
                    transition(state + 1)
                }
            )
        }
    }

    data class OnEventInput(
        val onEvent: () -> Unit,
    )
}