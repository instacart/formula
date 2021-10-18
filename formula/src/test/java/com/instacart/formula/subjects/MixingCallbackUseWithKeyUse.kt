package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot

class MixingCallbackUseWithKeyUse {

    class ParentOutput(
        val firstCallback: Listener<Unit>,
        val secondCallback: Listener<Unit>,
        val thirdCallback: Listener<Unit>,
    )

    class ParentFormula : Formula<Unit, Unit, ParentOutput>() {
        override fun initialState(input: Unit) = Unit

        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<ParentOutput> {
            return Evaluation(
                output = ParentOutput(
                    firstCallback = context.onEvent { none() },
                    secondCallback = context.key("scoped") {
                        context.onEvent { none() }
                    },
                    thirdCallback = context.onEvent { none() }
                )
            )
        }
    }
}
