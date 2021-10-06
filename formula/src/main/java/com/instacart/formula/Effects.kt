package com.instacart.formula

/**
 * Effects is a function returned within [Transition.Result] which will be executed by
 * [FormulaRuntime] usually after [Formula] state changes are applied. This function is
 * the place to call listeners, log events, trigger network requests or database writes,
 * and etc. Instead of executing effects within [Transition.toResult], deferring execution
 * allows us to ensure that [Formula] is always in the correct state in case effects
 * trigger a new event.
 */
typealias Effects = () -> Unit
