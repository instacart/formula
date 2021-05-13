package com.instacart.formula.android

interface BaseFormulaFragment<State> {

    fun getFragmentKey(): FragmentKey

    fun currentState(): State?

    fun setState(state: State)
}
