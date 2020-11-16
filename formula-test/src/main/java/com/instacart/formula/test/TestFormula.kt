package com.instacart.formula.test

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Stream
import java.lang.IllegalStateException

/**
 * Test formula is used to provide a fake formula implementation. It allows you to [send][output]
 * output updates and [inspect/interact][input] with input.
 */
abstract class TestFormula<Input, Output> :
    Formula<Input, TestFormula.State<Input, Output>, Output> {

    companion object {
        /**
         * Initializes [TestFormula] instance with [initialOutput].
         */
        operator fun <Input, Output> invoke(
            initialOutput: Output,
            key: (Input) -> Any? = { null }
        ): TestFormula<Input, Output> {
            return object : TestFormula<Input, Output>() {
                override fun initialOutput(): Output = initialOutput

                override fun key(input: Input): Any? = key(input)
            }
        }
    }

    data class State<Input, Output>(
        val initialInput: Input,
        val currentInput: Input,
        val output: Output
    )

    data class Value<Input, Output>(
        val input: Input,
        val onNewOutput: (Output) -> Unit
    )

    /**
     * Uses initial input as key (to be decided if its robust enough)
     */
    private val stateMap = mutableMapOf<Input, Value<Input, Output>>()

    abstract fun initialOutput(): Output

    override fun initialState(input: Input): State<Input, Output> {
        return State(input, input, initialOutput())
    }

    override fun onInputChanged(
        oldInput: Input,
        input: Input,
        state: State<Input, Output>
    ): State<Input, Output> {
        return state.copy(currentInput = input)
    }

    override fun evaluate(
        input: Input,
        state: State<Input, Output>,
        context: FormulaContext<State<Input, Output>>
    ): Evaluation<Output> {
        stateMap[state.initialInput] = Value(
            input = input,
            onNewOutput = context.eventCallback {
                transition(state.copy(output = it))
            }
        )

        return Evaluation(
            output = state.output,
            updates = context.updates {
                Stream.onTerminate().onEvent {
                    transition {
                        stateMap.remove(state.initialInput)
                    }
                }
            }
        )
    }

    /**
     * Emits a new [Output].
     */
    fun output(output: Output) {
        val update = stateMap.values.lastOrNull()?.onNewOutput ?: throw IllegalStateException("Formula is not running")
        update(output)
    }

    /**
     * Performs an interaction on the current [Input] passed by the parent.
     */
    fun input(interact: Input.() -> Unit) {
        val input = stateMap.values.lastOrNull()?.input ?: throw IllegalStateException("Formula is not running")
        interact(input)
    }
}