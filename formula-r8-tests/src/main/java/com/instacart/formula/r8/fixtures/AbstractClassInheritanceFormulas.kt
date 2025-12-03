package com.instacart.formula.r8.fixtures

import com.instacart.formula.Action
import com.instacart.formula.ActionFormula

object AbstractClassInheritanceFormulas {

    class One : ActionFormula<Unit, Int>() {
        override fun initialValue(input: Unit): Int = 1

        override fun action(input: Unit): Action<Int> {
            return Action.onData(1)
        }
    }


    class Two : ActionFormula<Unit, Int>() {
        override fun initialValue(input: Unit): Int = 2

        override fun action(input: Unit): Action<Int> {
            return Action.onData(2)
        }
    }
}