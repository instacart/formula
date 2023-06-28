package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
import com.instacart.formula.Inspector
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition

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
    private val formula: Formula<Input, State, Output>,
    initialInput: Input,
    private val transitionListener: TransitionListener,
    private val listeners: Listeners = Listeners(),
    private val inspector: Inspector?,
) : FormulaManager<Input, Output> {

    private val type = formula.type()
    private var state: State = formula.initialState(initialInput)
    private var frame: Frame<Input, State, Output>? = null
    private var childrenManager: ChildrenManager? = null
    private var isValidationEnabled: Boolean = false

    private val actionManager: ActionManager = ActionManager(
        formulaType = type,
        inspector = inspector,
    )

    var terminated = false

    fun handleTransitionResult(result: Transition.Result<State>) {
        if (terminated) {
            // State transitions are ignored, only side effects are passed up to be executed.
            transitionListener.onTransitionResult(type, result, true)
            return
        }

        if (result is Transition.Result.Stateful) {
            state = result.state
        }
        val frame = this.frame
        frame?.updateStateValidity(state)
        val isValid = frame != null && frame.isValid()
        transitionListener.onTransitionResult(type, result, isValid)
    }

    override fun setValidationRun(isValidationEnabled: Boolean) {
        this.isValidationEnabled = isValidationEnabled
    }

    override fun updateTransitionId(transitionId: TransitionId) {
        val lastFrame = checkNotNull(frame) { "missing frame means this is called before initial evaluate" }
        lastFrame.snapshot.transitionId = transitionId

        childrenManager?.updateTransitionId(transitionId)
    }

    /**
     * Creates the current [Output] and prepares the next frame that will need to be processed.
     */
    override fun evaluate(
        input: Input,
        transitionId: TransitionId
    ): Evaluation<Output> {
        // TODO: assert main thread.
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
            if (!isValidationEnabled && lastFrame.isValid() && !hasInputChanged) {
                updateTransitionId(transitionId)
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

        val snapshot = SnapshotImpl(input, state, transitionId, listeners, this)
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

        val frame = Frame(snapshot, result)
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

    override fun terminateDetachedChildren(transitionId: TransitionId): Boolean {
        return childrenManager?.terminateDetachedChildren(transitionId) == true
    }

    // TODO: should probably terminate children streams, then self.
    override fun terminateOldUpdates(transitionId: TransitionId): Boolean {
        if (actionManager.terminateOld(transitionId)) {
            return true
        }

        // Step through children frames
        if (childrenManager?.terminateOldUpdates(transitionId) == true) {
            return true
        }

        return false
    }

    override fun startNewUpdates(transitionId: TransitionId): Boolean {
        // Update parent workers so they are ready to handle events
        if (actionManager.startNew(transitionId)) {
            return true
        }

        // Step through children frames
        if (childrenManager?.startNewUpdates(transitionId) == true) {
            return true
        }

        return false
    }

    fun <ChildInput, ChildOutput> child(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
        transitionId: TransitionId
    ): ChildOutput {
        val childrenManager = getOrInitChildrenManager()
        val manager = childrenManager.findOrInitChild(key, formula, input)
        manager.setValidationRun(isValidationEnabled)
        return manager.evaluate(input, transitionId).output
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

    private fun getOrInitChildrenManager(): ChildrenManager {
        return childrenManager ?: run {
            val listener = TransitionListener { type, result, isChildValid ->
                val frame = this.frame
                if (!isChildValid) {
                    frame?.childInvalidated()
                }
                val isValid = frame != null && frame.isValid()
                transitionListener.onTransitionResult(type, result, isValid)
            }

            val value = ChildrenManager(listener, inspector)
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
