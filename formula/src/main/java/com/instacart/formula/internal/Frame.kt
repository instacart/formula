package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Update

/**
 * A frame is a representation of state after a process round. After each processing round
 * we need to look at what children and updates exist and do a diff.
 */
internal class Frame<Input, State, RenderModel>(
    val input: Input,
    val state: State,
    val evaluation: Evaluation<RenderModel>,
    val transitionCallbackWrapper: TransitionCallbackWrapper<State, *>,
    val children: Map<FormulaKey, List<Update>>
) {
    private var stateValid: Boolean = true
    private var childrenValid: Boolean = true

    fun updateStateValidity(state: State) {
        if (stateValid && this.state != state) {
            stateValid = false
        }
    }

    fun childInvalidated() {
        childrenValid = false
    }

    fun isValid() = stateValid && childrenValid


    fun isValid(input: Input): Boolean {
        return stateValid && childrenValid && this.input == input
    }
}
