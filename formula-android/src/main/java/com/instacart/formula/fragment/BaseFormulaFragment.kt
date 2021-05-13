package com.instacart.formula.fragment

import com.instacart.formula.android.FragmentKey

interface BaseFormulaFragment<State> {

    fun getFragmentKey(): FragmentKey

    fun currentState(): State?

    fun setState(state: State)
}
