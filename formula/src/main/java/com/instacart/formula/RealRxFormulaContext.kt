package com.instacart.formula

class RealRxFormulaContext<State, Effect>(
    private val delegate: Delegate<State, Effect>,
    private val onChange: (Transition<State, Effect>) -> Unit
) : FormulaContext<State, Effect> {

    var children = mutableMapOf<ProcessorManager.FormulaKey, List<Worker<*, *>>>()

    interface Delegate<State, Effect> {
        fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
            formula: ProcessorFormula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
            input: ChildInput,
            key: ProcessorManager.FormulaKey,
            onEffect: (ChildEffect) -> Transition<State, Effect>
        ): ProcessResult<ChildRenderModel>
    }

    override fun transition(state: State) {
        onChange(Transition(state))
    }

    override fun transition(state: State, effect: Effect?) {
        onChange(Transition(state, effect))
    }

    override fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
        formula: ProcessorFormula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
        input: ChildInput,
        tag: String,
        onEffect: (ChildEffect) -> Transition<State, Effect>
    ): ChildRenderModel {
        val key = ProcessorManager.FormulaKey(formula::class, tag)
        val result = delegate.child(formula, input, key, onEffect)
        children[key] = result.workers
        return result.renderModel
    }
}
