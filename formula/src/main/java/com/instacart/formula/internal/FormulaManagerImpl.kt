package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Message
import com.instacart.formula.Transition

/**
 * Handles formula and its children state processing.
 *
 * Order of processing:
 * 1. Evaluate
 * 2. Disable old callbacks
 * 3. Terminate removed children
 * 4. Prepare parent and alive children for updates.
 */
internal class FormulaManagerImpl<Input, State, RenderModel>(
    state: State,
    private val callbacks: ScopedCallbacks,
    private val transitionLock: TransitionLock,
    private val childManagerFactory: FormulaManagerFactory
) : FormulaContextImpl.Delegate<State>, FormulaManager<Input, State, RenderModel> {

    private val updateManager = UpdateManager(transitionLock)

    internal val children: SingleRequestMap<Any, FormulaManager<*, *, *>> = mutableMapOf()
    internal var frame: Frame<Input, State, RenderModel>? = null
    private var terminated = false

    private var state: State = state
    private var onTransition: ((List<Message>, isValid: Boolean) -> Unit)? = null

    private fun handleTransition(transition: Transition<State>, wasChildInvalidated: Boolean) {
        this.state = transition.state ?: this.state
        val frame = this.frame
        frame?.updateStateValidity(state)
        if (wasChildInvalidated) {
            frame?.childInvalidated()
        }

        if (!terminated) {
            val isValid = frame != null && frame.isValid()
            onTransition?.invoke(transition.messages, isValid)
        }
    }

    override fun setTransitionListener(listener: (List<Message>, isValid: Boolean) -> Unit) {
        this.onTransition = listener
    }

    override fun updateTransitionNumber(number: Long) {
        val lastFrame = checkNotNull(frame) { "missing frame means this is called before initial evaluate" }
        lastFrame.transitionCallbackWrapper.transitionId = number

        children.forEachValue {
            it.updateTransitionNumber(number)
        }
    }

    /**
     * Creates the current [RenderModel] and prepares the next frame that will need to be processed.
     */
    override fun evaluate(
        formula: Formula<Input, State, RenderModel>,
        input: Input,
        transitionId: Long
    ): Evaluation<RenderModel> {
        // TODO: assert main thread.

        val lastFrame = frame
        if (lastFrame != null && lastFrame.isValid(input)) {
            updateTransitionNumber(transitionId)
            return lastFrame.evaluation
        }

        val transitionCallback = TransitionCallbackWrapper(transitionLock, this::handleTransition, transitionId)
        val context = FormulaContextImpl(transitionId, callbacks, this, transitionCallback)

        val prevInput = frame?.input
        if (prevInput != null && prevInput != input) {
            state = formula.onInputChanged(prevInput, input, state)
        }

        callbacks.evaluationStarted()
        val result = formula.evaluate(input, state, context)
        val frame = Frame(input, state, result, transitionCallback)
        updateManager.updateEventListeners(frame.evaluation.updates)
        this.frame = frame

        callbacks.evaluationFinished()

        // Set pending removal of children.
        children.clearUnrequested {
            it.markAsTerminated()
        }

        transitionCallback.running = true
        return result
    }

    override fun terminateOldUpdates(currentTransition: Long): Boolean {
        val newFrame = frame ?: throw IllegalStateException("call evaluate before calling nextFrame()")

        if (updateManager.terminateOld(newFrame.evaluation.updates, currentTransition)) {
            return true
        }

        // Step through children frames
        children.forEachValue {
            if (it.terminateOldUpdates(currentTransition)) {
                return true
            }
        }

        return false
    }

    override fun startNewUpdates(currentTransition: Long): Boolean {
        val newFrame = frame ?: throw IllegalStateException("call evaluate before calling nextFrame()")

        // Update parent workers so they are ready to handle events
        if (updateManager.startNew(newFrame.evaluation.updates, currentTransition)) {
            return true
        }

        // Step through children frames
        children.forEachValue {
            if (it.startNewUpdates(currentTransition)) {
                return true
            }
        }

        return false
    }

    /**
     * Returns true if has transition while moving to next frame.
     */
    fun nextFrame(currentTransition: Long): Boolean {
        if (terminateOldUpdates(currentTransition)) {
            return true
        }

        if (startNewUpdates(currentTransition)) {
            return true
        }

        return transitionLock.hasTransitioned(currentTransition)
    }

    override fun <ChildInput, ChildState, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, ChildRenderModel>,
        input: ChildInput,
        key: Any,
        processingPass: Long
    ): ChildRenderModel {
        @Suppress("UNCHECKED_CAST")
        val manager = children
            .findOrInit(key) {
                childManagerFactory.createChildManager(formula, input, transitionLock)
            }
            .requestAccess {
                throw java.lang.IllegalStateException("There already is a child with same key: $key. Use [key: String] parameter.")
            } as FormulaManager<ChildInput, ChildState, ChildRenderModel>

        manager.setTransitionListener { messages, isValid ->
            handleTransition(Transition(messages = messages), !isValid)
        }

        return manager.evaluate(formula, input, processingPass).renderModel
    }

    override fun markAsTerminated() {
        terminated = true

        // Clear callbacks
        callbacks.disableAll()

        // Terminate updates so no transitions happen
        updateManager.terminate()

        children.forEachValue {
            it.markAsTerminated()
        }
    }


    fun terminate() {
        markAsTerminated()
    }
}
