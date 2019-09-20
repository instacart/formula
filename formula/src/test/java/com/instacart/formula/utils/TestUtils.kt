package com.instacart.formula.utils

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext

object TestUtils {
    class FormulaT<Input, State, RenderModel>(
        val initialState: (Input) -> State,
        val onInputChanged: (old: Input, new: Input, State) -> State = { _, _, state -> state },
        val evaluate: (Input, State, FormulaContext<State>) -> Evaluation<RenderModel>
    ) : Formula<Input, State, RenderModel> {

        override fun initialState(input: Input): State = initialState.invoke(input)
        override fun onInputChanged(oldInput: Input, input: Input, state: State): State = onInputChanged.invoke(oldInput, input, state)

        override fun evaluate(input: Input, state: State, context: FormulaContext<State>): Evaluation<RenderModel> {
            return evaluate.invoke(input, state, context)
        }
    }

    fun <RenderModel> stateless(
        evaluate: (FormulaContext<Unit>) -> Evaluation<RenderModel>
    ): Formula<Unit, Unit, RenderModel> {
        return FormulaT(
            initialState = { Unit },
            evaluate = { input, state, context ->
                evaluate(context)
            }
        )
    }

    fun <Input, RenderModel> stateless(
        evaluate: (Input, FormulaContext<Unit>) -> Evaluation<RenderModel>
    ): Formula<Input, Unit, RenderModel> {
        return FormulaT(
            initialState = { Unit },
            evaluate = { input, state, context ->
                evaluate(input, context)
            }
        )
    }

    fun <Input, State, RenderModel> create(
        initialState: State,
        evaluate: (Input, State, FormulaContext<State>) -> Evaluation<RenderModel>
    ): Formula<Input, State, RenderModel> = FormulaT(
        initialState = { initialState },
        evaluate = evaluate
    )

    fun <State, RenderModel> create(
        initialState: State,
        evaluate: (State, FormulaContext<State>) -> Evaluation<RenderModel>
    ): Formula<Unit, State, RenderModel> = FormulaT(
        initialState = { initialState },
        evaluate = { input, state, context ->
            evaluate(state, context)
        }
    )

    fun <Input, State, RenderModel> lazyState(
        initialState: (Input) -> State,
        onInputChanged: (old: Input, new: Input, State) -> State = { _, _, state -> state },
        evaluate: (Input, State, FormulaContext<State>) -> Evaluation<RenderModel>
    ) = FormulaT(
        initialState = initialState,
        onInputChanged = onInputChanged,
        evaluate = evaluate
    )

    fun <Input, State, RenderModel> lazyState(
        initialState: (Input) -> State,
        onInputChanged: (old: Input, new: Input, State) -> State = { _, _, state -> state },
        evaluate: (State, FormulaContext<State>) -> Evaluation<RenderModel>
    ) = FormulaT(
        initialState = initialState,
        onInputChanged = onInputChanged,
        evaluate = { input, state, context ->
            evaluate(state, context)
        }
    )
}
