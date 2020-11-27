package com.instacart.formula


inline fun <Input, Output> Formula.Companion.stateless(
    crossinline output: (Input, FormulaContext<Unit>) -> Evaluation<Output>
): IFormula<Input, Output> = DelegateFormula<Input, Unit, Output>(
    initialState = { Unit },
    evaluate = { input, state, context ->
        output(input, context)
    }
)

inline fun <Output> Formula.Companion.stateless(
    crossinline output: (FormulaContext<Unit>) -> Evaluation<Output>
): IFormula<Unit, Output> = DelegateFormula<Unit, Unit, Output>(
    initialState = { Unit },
    evaluate = { input, state, context ->
        output(context)
    }
)

fun <Input, State: Any, Output> Formula.Companion.create(
    initialState: State,
    evaluate: (Input, State, FormulaContext<State>) -> Evaluation<Output>
): IFormula<Input, Output> = DelegateFormula(
    initialState = { initialState },
    evaluate = evaluate
)

inline fun <State: Any, Output> Formula.Companion.create(
    initialState: State,
    crossinline evaluate: (State, FormulaContext<State>) -> Evaluation<Output>
): IFormula<Unit, Output> = DelegateFormula<Unit, State, Output>(
    initialState = { initialState },
    evaluate = { _, state, context ->
        evaluate(state, context)
    }
)

@PublishedApi
internal class DelegateFormula<Input, State: Any, Output>(
    private val initialState: (Input) -> State,
    private val onInputChanged: (Input, Input, State) -> State = { _, _, state -> state },
    private val evaluate: (Input, State, FormulaContext<State>) -> Evaluation<Output>,
    private val key: ((Input) -> Any?)? = null
): Formula<Input, State, Output> {
    override fun initialState(input: Input): State = initialState.invoke(input)

    override fun onInputChanged(oldInput: Input, input: Input, state: State): State {
        return onInputChanged.invoke(oldInput, input, state)
    }

    override fun evaluate(input: Input, state: State, context: FormulaContext<State>): Evaluation<Output> {
        return evaluate.invoke(input, state, context)
    }

    override fun key(input: Input): Any? {
        return key?.invoke(input) ?: super.key(input)
    }
}