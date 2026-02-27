package com.instacart.formula.action

import com.instacart.formula.Action
import com.instacart.formula.DelegateAction

internal class OverrideKeyAction<Event>(
    private val key: Any,
    delegateAction: Action<Event>,
) : DelegateAction<Event>(delegateAction) {

    override fun key(): Any = key
}