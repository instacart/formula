package com.instacart.formula

class MixingCallbackUseWithKeyUse {

    class ParentRenderModel(
        val firstCallback: () -> Unit,
        val secondCallback: () -> Unit,
        val thirdCallback: () -> Unit
    )

    class ParentFormula : Formula<Unit, Unit, ParentRenderModel> {
        override fun initialState(input: Unit) = Unit

        override fun evaluate(
            input: Unit,
            state: Unit,
            context: FormulaContext<Unit>
        ): Evaluation<ParentRenderModel> {


            return Evaluation(
                renderModel = ParentRenderModel(
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
