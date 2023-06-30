package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
import com.instacart.formula.Inspector
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import kotlin.reflect.KClass

/**
 * Handles formula and its children state processing.
 *
 * Order of processing:
 * 1. Evaluate
 * 2. Disable old event listeners
 * 3. Terminate removed children
 * 4. Prepare parent and alive children for updates.
 */
internal class FormulaManagerImpl<Input, State, Output>(
    private val delegate: ManagerDelegate,
    private val formula: Formula<Input, State, Output>,
    initialInput: Input,
    private val listeners: Listeners = Listeners(),
    private val inspector: Inspector?,
) : FormulaManager<Input, Output>, ManagerDelegate {

    private val type = formula.type()
    private var state: State = formula.initialState(initialInput)
    private var frame: Frame<Input, State, Output>? = null
    private var childrenManager: ChildrenManager? = null
    private var isValidationEnabled: Boolean = false

    private val actionManager: ActionManager = ActionManager(
        manager = this,
        formulaType = type,
        inspector = inspector,
    )

    var transitionID: Long = 0
    var terminated = false

    fun hasTransitioned(id: Long): Boolean {
        return transitionID != id
    }

    fun handleTransitionResult(result: Transition.Result<State>) {
        if (terminated) {
            // State transitions are ignored, only side effects are passed up to be executed.
            delegate.onTransition(type, result, evaluate = false)
            return
        }

        val frame = this.frame
        if (result is Transition.Result.Stateful) {
            if (state != result.state) {
                state = result.state

                transitionID += 1
            }
        }

        val evaluate = frame == null || frame.transitionID != transitionID
        if (evaluate || result.effects != null) {
            delegate.onTransition(type, result, evaluate)
        }
    }

    override fun setValidationRun(isValidationEnabled: Boolean) {
        this.isValidationEnabled = isValidationEnabled
    }

    /**
     * Creates the current [Output] and prepares the next frame that will need to be processed.
     */
    override fun evaluate(input: Input): Evaluation<Output> {
        // TODO: assert main thread.
        val transitionID = transitionID
        val lastFrame = frame
        if (lastFrame == null && isValidationEnabled) {
            throw ValidationException("Formula should already have run at least once before the validation mode.")
        }

        if (lastFrame == null) {
            inspector?.onFormulaStarted(type)
        }

        if (!isValidationEnabled) {
            inspector?.onEvaluateStarted(type, state)
        }

        if (lastFrame != null) {
            val prevInput = lastFrame.input
            val hasInputChanged = prevInput != input
            if (!isValidationEnabled && lastFrame.transitionID == transitionID && !hasInputChanged) {
                val evaluation = lastFrame.evaluation
                inspector?.onEvaluateFinished(type, evaluation.output, evaluated = false)
                return evaluation
            }

            if (hasInputChanged) {
                if (isValidationEnabled) {
                    throw ValidationException("$type - input changed during identical re-evaluation - old: $prevInput, new: $input")
                }
                state = formula.onInputChanged(prevInput, input, state)

                inspector?.onInputChanged(type, prevInput, input)
            }
        }

        val snapshot = SnapshotImpl(input, state, transitionID, listeners, this)
        val result = formula.evaluate(snapshot)

        if (isValidationEnabled) {
            val oldOutput = lastFrame?.evaluation?.output
            if (oldOutput != result.output) {
                throw ValidationException("$type - output changed during identical re-evaluation - old: $oldOutput, new: ${result.output}")
            }

            val lastActionKeys = lastFrame?.evaluation?.actions?.map { it.key }
            val currentActionKeys = result.actions.map { it.key }
            if (lastActionKeys != currentActionKeys) {
                throw ValidationException("$type - action keys changed during identical re-evaluation - old: $lastActionKeys, new: $currentActionKeys")
            }
        }

        val frame = Frame(snapshot, result, transitionID)
        actionManager.onNewFrame(frame.evaluation.actions)
        this.frame = frame

        listeners.evaluationFinished()
        childrenManager?.evaluationFinished()

        snapshot.running = true
        if (!isValidationEnabled) {
            inspector?.onEvaluateFinished(type, frame.evaluation.output, evaluated = true)
        }
        return result
    }

    override fun executeUpdates(): Boolean {
        val transitionID = transitionID
        if (childrenManager?.executeChildUpdates(transitionID) == true) {
            return true
        }

        if (childrenManager?.terminateChildren(transitionID) == true) {
            return true
        }

        if (actionManager.terminateOld(transitionID)) {
            return true
        }

        return actionManager.startNew(transitionID)
    }

    fun <ChildInput, ChildOutput> child(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): ChildOutput {
        val childrenManager = getOrInitChildrenManager()
        val manager = childrenManager.findOrInitChild(key, formula, input)
        manager.setValidationRun(isValidationEnabled)
        return manager.evaluate(input).output
    }

    override fun markAsTerminated() {
        terminated = true
        frame?.snapshot?.terminated = true
        childrenManager?.markAsTerminated()
    }

    override fun performTerminationSideEffects() {
        childrenManager?.performTerminationSideEffects()
        actionManager.terminate()
        listeners.disableAll()

        inspector?.onFormulaFinished(type)
    }

    override fun onTransition(formulaType: KClass<*>, result: Transition.Result<*>, evaluate: Boolean) {
        if (evaluate) {
            transitionID += 1
        }
        delegate.onTransition(formulaType, result, evaluate)
    }

    private fun getOrInitChildrenManager(): ChildrenManager {
        return childrenManager ?: run {
            val value = ChildrenManager(this, inspector)
            childrenManager = value
            value
        }
    }

    private fun Formula<Input, State, Output>.evaluate(
        snapshot: Snapshot<Input, State>
    ): Evaluation<Output> {
        return snapshot.run { evaluate() }
    }
}
