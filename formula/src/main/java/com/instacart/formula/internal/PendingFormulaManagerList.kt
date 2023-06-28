package com.instacart.formula.internal

internal class PendingFormulaManagerList(
    private val action: (FormulaManager<*, *>, TransitionId) -> Boolean,
) {
    var invalidated = false
    var pending: MutableList<FormulaManager<*, *>>? = null


    fun evaluationFinished() {
        pending?.clear()
        invalidated = true
    }

    fun iterate(
        children: SingleRequestMap<Any, FormulaManager<*, *>>?,
        transitionId: TransitionId,
    ): Boolean {
        prepareList(children)

        if (pending.isNullOrEmpty()) {
            return false
        }

        val iterator = pending?.iterator()
        while (iterator?.hasNext() == true) {
            val childFormulaManager = iterator.next()
            if (action(childFormulaManager, transitionId)) {
                return true
            }

            iterator.remove()
        }

        return false
    }


    private fun prepareList(
        children: SingleRequestMap<Any, FormulaManager<*, *>>?
    ) {
        if (!invalidated) {
            return
        }
        invalidated = false

        if (!children.isNullOrEmpty()) {
            children.forEachValue {
                if (pending == null) {
                    pending = mutableListOf()
                }

                pending?.add(it)
            }
        }
    }
}