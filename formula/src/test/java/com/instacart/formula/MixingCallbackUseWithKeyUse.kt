package com.instacart.formula

class MixingCallbackUseWithKeyUse {

    class ParentOutput(
        val firstCallback: () -> Unit,
        val secondCallback: () -> Unit,
        val thirdCallback: () -> Unit
    )

    class ParentFormula : Formula<Unit, Unit, ParentOutput> {
        override fun initialState(input: Unit) = Unit

        override fun evaluate(
            input: Unit,
            state: Unit,
            context: FormulaContext<Unit>
        ): Evaluation<ParentOutput> {
            return Evaluation(
                output = ParentOutput(
                    firstCallback = context.callback { none() },
                    secondCallback = context.key("scoped") {
                        context.callback { none() }
                    },
                    thirdCallback = context.callback { none() }
                )
            )
        }
    }
}
