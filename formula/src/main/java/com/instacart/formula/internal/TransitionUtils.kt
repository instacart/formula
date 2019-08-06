package com.instacart.formula.internal

import com.instacart.formula.EventMessage
import com.instacart.formula.Message
import com.instacart.formula.Transition
import com.instacart.formula.UnitMessage

internal object TransitionUtils {

    fun isEmpty(transition: Transition<*>): Boolean {
        return transition.state == null
            && transition.messages.isEmpty()
    }

    fun isMessageTransitional(message: Message): Boolean {
        return when(message) {
            is UnitMessage -> message.invoke is Callback
            is EventMessage<*> -> message.invoke is EventCallback<*>
        }
    }
}
