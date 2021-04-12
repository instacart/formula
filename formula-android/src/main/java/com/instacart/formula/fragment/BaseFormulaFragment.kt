package com.instacart.formula.fragment

interface BaseFormulaFragment<State> {

    fun getFragmentKey(): FragmentKey

    fun currentState(): State?

    fun setState(state: State)
}
