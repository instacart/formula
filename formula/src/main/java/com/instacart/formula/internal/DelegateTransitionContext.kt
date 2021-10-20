package com.instacart.formula.internal

import com.instacart.formula.TransitionContext

internal class DelegateTransitionContext<Input, State>(
    override val input: Input,
    override val state: State,
): TransitionContext<Input, State>