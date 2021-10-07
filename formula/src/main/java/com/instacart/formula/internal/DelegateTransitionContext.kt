package com.instacart.formula.internal

import com.instacart.formula.TransitionContext

internal class DelegateTransitionContext<State>(override val state: State): TransitionContext<State>