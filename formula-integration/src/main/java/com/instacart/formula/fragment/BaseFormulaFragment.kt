package com.instacart.formula.fragment

interface BaseFormulaFragment<State> {

    fun getMviContract(): FragmentContract<State>

    fun currentState(): State?

    fun setState(state: State)
}
