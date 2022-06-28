package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
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
    private val actionManager: ActionManager = ActionManager(),
) : FormulaManager<Input, Output> {

    private var state: State = formula.initialState(initialInput)
    private var frame: Frame<Input, State, Output>? = null
    private var childrenManager: ChildrenManager? = null

    var terminated = false

    fun handleTransitionResult(result: Transition.Result<State>) {
        if (terminated) {
            // State transitions are ignored, only side effects are passed up to be executed.
            transitionListener.onTransitionResult(result, true)
            return
        }

        if (result is Transition.Result.Stateful) {
            state = result.state
        }
        val frame = this.frame
        frame?.updateStateValidity(state)
        val isValid = frame != null && frame.isValid()
        transitionListener.onTransitionResult(result, isValid)
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
        if (lastFrame != null && lastFrame.isValid(input)) {
            updateTransitionId(transitionId)
            return lastFrame.evaluation
        }

        val prevInput = frame?.input
        if (prevInput != null && prevInput != input) {
            state = formula.onInputChanged(prevInput, input, state)
        }

        val snapshot = SnapshotImpl(input, state, transitionId, listeners, this)
        val result = formula.evaluate(snapshot)
        val frame = Frame(snapshot, result)
        actionManager.updateEventListeners(frame.evaluation.actions)
        this.frame = frame

        listeners.evaluationFinished()
        childrenManager?.evaluationFinished()

        snapshot.running = true
        return result
    }

    override fun terminateDetachedChildren(transitionId: TransitionId): Boolean {
        return childrenManager?.terminateDetachedChildren(transitionId) == true
    }

    // TODO: should probably terminate children streams, then self.
    override fun terminateOldUpdates(transitionId: TransitionId): Boolean {
        val newFrame = frame ?: throw IllegalStateException("call evaluate before calling nextFrame()")

        if (actionManager.terminateOld(newFrame.evaluation.actions, transitionId)) {
            return true
        }

        // Step through children frames
        if (childrenManager?.terminateOldUpdates(transitionId) == true) {
            return true
        }

        return false
    }

    override fun startNewUpdates(transitionId: TransitionId): Boolean {
        val newFrame = frame ?: throw IllegalStateException("call evaluate before calling nextFrame()")

        // Update parent workers so they are ready to handle events
        if (actionManager.startNew(newFrame.evaluation.actions, transitionId)) {
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
    }

    private fun getOrInitChildrenManager(): ChildrenManager {
        return childrenManager ?: run {
            val listener = TransitionListener { result, isChildValid ->
                val frame = this.frame
                if (!isChildValid) {
                    frame?.childInvalidated()
                }
                val isValid = frame != null && frame.isValid()
                transitionListener.onTransitionResult(result, isValid)
            }

            val value = ChildrenManager(listener)
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
