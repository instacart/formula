package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
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
    private val listeners: ScopedListeners,
    private val transitionListener: TransitionListener
) : SnapshotImpl.Delegate, FormulaManager<Input, Output> {

    constructor(
        formula: Formula<Input, State, Output>,
        input: Input,
        transitionListener: TransitionListener
    ): this(formula, input, ScopedListeners(formula), transitionListener)

    private val actionManager = ActionManager()

    private var children: SingleRequestMap<Any, FormulaManager<*, *>>? = null
    private var frame: Frame<Input, State, Output>? = null
    var terminated = false

    private var state: State = formula.initialState(initialInput)
    private var pendingRemoval: MutableList<FormulaManager<*, *>>? = null

    private var childTransitionListener: TransitionListener? = null

    private fun handleTransitionResult(result: Transition.Result<State>) {
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
        lastFrame.transitionDispatcher.transitionId = transitionId

        children?.forEachValue { it.updateTransitionId(transitionId) }
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

        listeners.evaluationStarted()
        val transitionDispatcher = TransitionDispatcher(input, state, this::handleTransitionResult, transitionId)
        val snapshot = SnapshotImpl(transitionId, listeners, this, transitionDispatcher)
        val result = snapshot.run { formula.run { evaluate() } }
        val frame = Frame(input, state, result, transitionDispatcher)
        actionManager.updateEventListeners(frame.evaluation.actions)
        this.frame = frame

        listeners.evaluationFinished()

        children?.clearUnrequested {
            pendingRemoval = pendingRemoval ?: mutableListOf()
            it.markAsTerminated()
            pendingRemoval?.add(it)
        }

        transitionDispatcher.running = true
        return result
    }

    override fun terminateDetachedChildren(transitionId: TransitionId): Boolean {
        val local = pendingRemoval
        pendingRemoval = null
        local?.forEach { it.performTerminationSideEffects() }
        if (transitionId.hasTransitioned()) {
            return true
        }

        return children?.any { it.value.value.terminateDetachedChildren(transitionId) } ?: false
    }

    // TODO: should probably terminate children streams, then self.
    override fun terminateOldUpdates(transitionId: TransitionId): Boolean {
        val newFrame = frame ?: throw IllegalStateException("call evaluate before calling nextFrame()")

        if (actionManager.terminateOld(newFrame.evaluation.actions, transitionId)) {
            return true
        }

        // Step through children frames
        children?.forEachValue {
            if (it.terminateOldUpdates(transitionId)) {
                return true
            }
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
        children?.forEachValue {
            if (it.startNewUpdates(transitionId)) {
                return true
            }
        }

        return false
    }

    override fun <ChildInput, ChildOutput> child(
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
        transitionId: TransitionId
    ): ChildOutput {
        @Suppress("UNCHECKED_CAST")
        val children = children ?: run {
            val initialized: SingleRequestMap<Any, FormulaManager<*, *>> = mutableMapOf()
            this.children = initialized
            initialized
        }

        val compositeKey = constructKey(formula, input)
        val manager = children
            .findOrInit(compositeKey) {
                val childTransitionListener = getOrInitChildTransitionListener()
                val implementation = formula.implementation()
                FormulaManagerImpl(implementation, input, childTransitionListener)
            }
            .requestAccess {
                throw IllegalStateException("There already is a child with same key: $compositeKey. Override [Formula.key] function.")
            } as FormulaManager<ChildInput, ChildOutput>

        return manager.evaluate(input, transitionId).output
    }

    override fun markAsTerminated() {
        terminated = true
        frame?.transitionDispatcher?.terminated = true
        children?.forEachValue { it.markAsTerminated() }
    }

    override fun performTerminationSideEffects() {
        children?.forEachValue { it.performTerminationSideEffects() }
        actionManager.terminate()
        listeners.disableAll()
    }

    private fun <ChildInput, ChildOutput> constructKey(
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput
    ): Any {
        return FormulaKey(
            type = formula.type(),
            key = formula.key(input)
        )
    }

    private fun getOrInitChildTransitionListener(): TransitionListener {
        return childTransitionListener ?: run {
            TransitionListener { result, isChildValid ->
                val frame = this.frame
                if (!isChildValid) {
                    frame?.childInvalidated()
                }
                val isValid = frame != null && frame.isValid()
                transitionListener.onTransitionResult(result, isValid)
            }.apply {
                childTransitionListener = this
            }
        }
    }
}
