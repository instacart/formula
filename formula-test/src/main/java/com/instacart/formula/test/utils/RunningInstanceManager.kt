package com.instacart.formula.test.utils

internal class RunningInstanceManager<Input, Output>(
    private val formulaKeyFactory: (Input) -> Any?,
) {
    data class State<Input, Output>(
        val key: Any?,
        val inputs: List<Input>,
        val output: Output,
        val onNewOutput: (Output) -> Unit,
    )

    // Uses identifier as key that is generated within [initialState]
    private val runningInstanceStates = mutableMapOf<Long, State<Input, Output>>()

    fun onEvaluate(
        uniqueIdentifier: Long,
        input: Input,
        output: Output,
        onNewOutput: (Output) -> Unit,
    ) {
        val currentValue = runningInstanceStates[uniqueIdentifier]
        val newInputList = if (currentValue == null) {
            listOf(input)
        } else if (currentValue.inputs.last() == input) {
            currentValue.inputs
        } else {
            currentValue.inputs + input
        }

        runningInstanceStates[uniqueIdentifier] = State(
            key = formulaKeyFactory(input),
            inputs = newInputList,
            output = output,
            onNewOutput = onNewOutput,
        )
    }

    fun onTerminate(uniqueIdentifier: Long) {
        runningInstanceStates.remove(uniqueIdentifier)
    }

    fun mostRecentInstance(): State<Input, Output> {
        return requireNotNull(runningInstanceStates.values.lastOrNull()) {
            "Formula is not running"
        }
    }

    fun instanceByKey(key: Any?): State<Input, Output> {
        return requireNotNull(runningInstanceStates.entries.firstOrNull { it.value.key == key }?.value) {
            val existingKeys = runningInstanceStates.entries.map { it.value.key }
            "Formula for $key is not running, there are $existingKeys running"
        }
    }

    fun assertRunningCount(expected: Int) {
        val count = runningInstanceStates.size
        if (count != expected) {
            throw AssertionError("Expected $expected running formulas, but there were $count instead")
        }
    }
}