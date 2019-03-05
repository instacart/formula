package com.instacart.formula

/**
 * Simple typealias for [Next], denoted as a reducer
 */
typealias NextReducer<State, Effect> = (State) -> Next<State, Effect>
