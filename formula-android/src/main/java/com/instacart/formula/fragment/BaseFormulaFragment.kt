package com.instacart.formula.fragment

interface BaseFormulaFragment<State> {

    fun getFragmentContract(): FragmentContract<State>

    fun currentState(): State?

    fun setState(state: State)
}
