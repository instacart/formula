package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import java.util.concurrent.atomic.AtomicLong

/**
 * Test formula is used to provide a fake formula implementation. It allows you to [send][output]
 * output updates and [inspect/interact][input] with input.
 */
abstract class TestFormula<Input, Output> :
    Formula<Input, TestFormula.State<Output>, Output>() {

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

    data class State<Output>(
        val uniqueIdentifier: Long,
        val key: Any?,
        val output: Output
    )

    data class Value<Input, Output>(
        val key: Any?,
        val input: Input,
        val onNewOutput: (Output) -> Unit
    )

    private val identifierGenerator = AtomicLong(0)

    /**
     * Uses initial input as key (to be decided if its robust enough)
     */
    private val stateMap = mutableMapOf<Any?, Value<Input, Output>>()

    abstract fun initialOutput(): Output

    override fun initialState(input: Input): State<Output> {
        return State(
            uniqueIdentifier = identifierGenerator.getAndIncrement(),
            key = key(input),
            output = initialOutput(),
        )
    }

    override fun Snapshot<Input, State<Output>>.evaluate(): Evaluation<Output> {
        stateMap[state.uniqueIdentifier] = Value(
            key = state.key,
            input = input,
            onNewOutput = context.onEvent {
                transition(state.copy(output = it))
            }
        )

        return Evaluation(
            output = state.output,
            actions = context.actions {
                Action.onTerminate().onEvent {
                    stateMap.remove(state.uniqueIdentifier)
                    none()
                }
            }
        )
    }

    /**
     * Emits a new [Output].
     */
    fun output(output: Output) {
        val update = requireNotNull(stateMap.values.lastOrNull()?.onNewOutput) {
            "Formula is not running"
        }
        update(output)
    }

    fun output(key: Any?, output: Output) {
        val instance = getByKey(key)
        instance.onNewOutput(output)
    }

    /**
     * Performs an interaction on the current [Input] passed by the parent.
     */
    fun input(interact: Input.() -> Unit) {
        val input = requireNotNull(stateMap.values.lastOrNull()?.input) {
            "Formula is not running"
        }
        interact(input)
    }

    /**
     * Performs an interaction on the current [Input] passed by the parent.
     */
    fun input(key: Any?, interact: Input.() -> Unit) {
        val instance = getByKey(key)
        instance.input.interact()
    }

    fun assertRunningCount(expected: Int) {
        val count = stateMap.size
        if (count != expected) {
            throw AssertionError("Expected $expected running formulas, but there were $count instead")
        }
    }

    private fun getByKey(key: Any?): Value<Input, Output> {
        return requireNotNull(stateMap.entries.firstOrNull { it.value.key == key }?.value) {
            val existingKeys = stateMap.entries.map { it.value.key }
            "Formula for $key is not running, there are $existingKeys running"
        }
    }
}