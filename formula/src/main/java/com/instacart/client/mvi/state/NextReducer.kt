package com.instacart.client.mvi.state

typealias NextReducer<State, Effect> = (State) -> ICNext<State, Effect>
