package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
import com.instacart.formula.Snapshot
import com.instacart.formula.test.utils.RunningInstanceManager
import java.util.concurrent.atomic.AtomicLong

/**
 * Test formula is used to replace a real formula with a fake implementation. This allows you to:
 * - Verify that parent passes correct inputs. Take a look at [input].
 * - Verify that parent deals with output changes correctly. Take a look at [output]
 *
 *
 * ```kotlin
 * // To replace a real formula with a fake one, we need first define the interface
 * interface UserFormula : IFormula<Input, Output> {
 *     data class Input(val userId: String)
 *     data class Output(val user: User?)
 * }
 *
 * // Then, we can create a fake implementation
 * class FakeUserFormula : UserFormula {
 *     override val implementation = testFormula(
 *         initialOutput = Output(user = null)
 *     )
 * }
 *
 * // Then, in our test, we can do the following
 * @Test fun `ensure that account formula passes user id to user formula`() {
 *     val userFormula = FakeUserFormula()
 *     val accountFormula = RealAccountFormula(userFormula)
 *     accountFormula.test().input(AccountFormula.Input(userId = "my-user-id"))
 *
 *     userFormula.implementation.input {
 *       Truth.assertThat(this.userId).isEqualTo("my-user-id")
 *     }
 * }
 * ```
 */
class TestFormula<Input, Output> internal constructor(
    private val parentFormula: IFormula<Input, Output>,
    private val initialOutput: Output,
) : Formula<Input, TestFormula.State<Output>, Output>() {

    data class State<Output>(
        val uniqueIdentifier: Long,
        val output: Output
    )

    private val identifierGenerator = AtomicLong(0)
    private val runningInstanceManager = RunningInstanceManager<Input, Output>(
        formulaKeyFactory = { parentFormula.key(it) },
    )

    override fun initialState(input: Input): State<Output> {
        return State(
            uniqueIdentifier = identifierGenerator.getAndIncrement(),
            output = initialOutput,
        )
    }

    override fun Snapshot<Input, State<Output>>.evaluate(): Evaluation<Output> {
        runningInstanceManager.onEvaluate(
            uniqueIdentifier = state.uniqueIdentifier,
            input = input,
            output = state.output,
            onNewOutput = context.onEvent {
                val newState = state.copy(output = it)
                transition(newState)
            }
        )

        return Evaluation(
            output = state.output,
            actions = context.actions {
                Action.onTerminate().onEvent {
                    runningInstanceManager.onTerminate(state.uniqueIdentifier)
                    none()
                }
            }
        )
    }

    /**
     * Emits a new [Output].
     */
    fun output(output: Output) {
        val update = runningInstanceManager.mostRecentInstance().onNewOutput
        update(output)
    }

    fun output(key: Any?, output: Output) {
        val instance = runningInstanceManager.instanceByKey(key)
        instance.onNewOutput(output)
    }

    fun updateOutput(modify: Output.() -> Output) {
        val formulaValue = runningInstanceManager.mostRecentInstance()
        val newOutput = formulaValue.output.modify()
        formulaValue.onNewOutput(newOutput)
    }

    fun updateOutput(key: Any?, modify: Output.() -> Output) {
        val formulaValue = runningInstanceManager.instanceByKey(key)
        val newOutput = formulaValue.output.modify()
        formulaValue.onNewOutput(newOutput)
    }

    /**
     * Performs an interaction on the current [Input] passed by the parent.
     */
    fun input(interact: Input.() -> Unit) {
        mostRecentInput().interact()
    }

    /**
     * Performs an interaction on the current [Input] passed by the parent.
     */
    fun input(key: Any?, interact: Input.() -> Unit) {
        inputByKey(key).interact()
    }

    fun assertRunningCount(expected: Int) {
        runningInstanceManager.assertRunningCount(expected)
    }

    fun mostRecentInput(): Input {
        return runningInstanceManager.mostRecentInstance().inputs.last()
    }

    fun inputByKey(key: Any?): Input {
        return runningInstanceManager.instanceByKey(key).inputs.last()
    }

    fun mostRecentInputs(): List<Input> {
        return runningInstanceManager.mostRecentInstance().inputs
    }

    fun inputsByKey(key: Any): List<Input> {
        return runningInstanceManager.instanceByKey(key).inputs
    }
}