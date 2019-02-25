package com.instacart.formula

typealias NextReducer<State, Effect> = (State) -> Next<State, Effect>
