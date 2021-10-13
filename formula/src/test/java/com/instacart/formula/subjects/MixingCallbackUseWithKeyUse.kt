package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener

class MixingCallbackUseWithKeyUse {

    class ParentOutput(
        val firstCallback: Listener<Unit>,
        val secondCallback: Listener<Unit>,
        val thirdCallback: Listener<Unit>,
    )

    class ParentFormula : Formula<Unit, Unit, ParentOutput>() {
        override fun initialState(input: Unit) = Unit

        override fun evaluate(
            input: Unit,
            state: Unit,
            context: FormulaContext<Unit>
        ): Evaluation<ParentOutput> {
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
