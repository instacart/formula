package com.instacart.client.mvi

interface ICBaseMviFragment<State> {

    fun getMviContract(): ICMviFragmentContract<State>

    fun currentState(): State?

    fun setState(state: State)
}