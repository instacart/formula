package com.instacart.formula.internal

import com.instacart.formula.plugin.Dispatcher

class TestDispatcher : Dispatcher {
    private var dispatches = mutableListOf<() -> Unit>()

    override fun isDispatchNeeded(): Boolean {
        return true
    }

    override fun dispatch(executable: () -> Unit) {
        dispatches.add(executable)
    }

    fun executeAndClear() {
        val local = dispatches
        dispatches = mutableListOf()
        local.forEach { it.invoke() }
    }
}