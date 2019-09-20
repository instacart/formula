package com.instacart.formula

object MultipleChildEvents {
    fun formula() = HasChildFormula.create(MessageFormula()) {
        MessageInput(messageHandler = eventCallback {
            transition(it)
        })
    }
}
