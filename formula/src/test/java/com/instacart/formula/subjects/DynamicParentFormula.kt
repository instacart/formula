package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener

class DynamicParentFormula(
    private val childFormula: KeyFormula = KeyFormula()
) : Formula<Unit, DynamicParentFormula.State, DynamicParentFormula.Output>() {

    data class State(
        val childKeys: List<TestKey> = emptyList()
    )

    data class Output(
        val children: List<TestOutput> = emptyList(),
        val addChild: (TestKey) -> Unit,
        val removeChild: (TestKey) -> Unit,
        val removeAllChildren: Listener<Unit>,
    )

    override fun initialState(input: Unit): State = State()

    override fun evaluate(
        input: Unit,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<Output> {
        return Evaluation(
            output = Output(
                children = state.childKeys.map {
                    context.child(childFormula, it)
                },
                addChild = context.onEvent<TestKey> { key ->
                    transition(state.copy(childKeys = state.childKeys.plus(key)))
                },
                removeChild = context.onEvent<TestKey> { key ->
                    transition(state.copy(childKeys = state.childKeys.minus(key)))
                },
                removeAllChildren = context.onEvent {
                    transition(state.copy(childKeys = emptyList()))
                }
            )
        )
    }
}