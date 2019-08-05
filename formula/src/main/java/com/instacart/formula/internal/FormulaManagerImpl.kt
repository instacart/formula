package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.SideEffect
import com.instacart.formula.Transition

/**
 * Handles state processing.
 *
 * Order of processing:
 * 1. Mark removed children as terminated.
 * 2. Prepare parent and alive children for updates.
 * 3. Process removed children side effects.
 * 4. Perform children side effects
 * 5. Perform parent side effects.
 */
class FormulaManagerImpl<Input, State, Output, RenderModel>(
    state: State,
    private val transitionLock: TransitionLock,
    private val childManagerFactory: FormulaManagerFactory
) : FormulaContextImpl.Delegate<State, Output>, FormulaManager<Input, State, Output, RenderModel> {

    private val updateManager = UpdateManager(transitionLock)

    internal val children: MutableMap<FormulaKey, FormulaManager<*, *, *, *>> = mutableMapOf()
    internal val pendingTermination = mutableListOf<FormulaManager<*, *, *, *>>()
    internal var frame: Frame<Input, State, RenderModel>? = null
    private var terminated = false

    private var state: State = state

    private var pendingSideEffects = mutableListOf<SideEffect>()

    class Holder<T>(val value: T) {
        var requested: Boolean = false

        inline fun requestAccess(errorMessage: () -> String): T {
            if (requested) {
                throw IllegalStateException(errorMessage())
            }

            requested = true
            return value
        }
    }

    private var onTransition: ((Output?, Boolean) -> Unit)? = null
    private val callbacks: MutableMap<Any, Holder<Callback>> = mutableMapOf()
    private val eventCallbacks: MutableMap<Any, Holder<EventCallback<*>>> = mutableMapOf()

    private fun handleTransition(transition: Transition<State, Output>, wasChildInvalidated: Boolean) {
        pendingSideEffects.addAll(transition.sideEffects)
        this.state = transition.state ?: this.state

        val frame = this.frame
        frame?.updateStateValidity(state)
        if (wasChildInvalidated) {
            frame?.childInvalidated()
        }

        if (!terminated) {
            val isValid = frame != null && frame.isValid()
            onTransition?.invoke(transition.output, isValid)
        }
    }

    override fun setTransitionListener(listener: (Output?, Boolean) -> Unit) {
        onTransition = listener
    }

    override fun updateTransitionNumber(number: Long) {
        val lastFrame = checkNotNull(frame) { "missing frame means this is called before initial evaluate" }
        lastFrame.transitionCallbackWrapper.transitionNumber = number

        children.forEach {
            it.value.updateTransitionNumber(number)
        }
    }

    override fun initOrFindCallback(key: Any): Callback {
        return callbacks
            .getOrPut(key) { Holder(Callback(key)) }
            .requestAccess {
               "Callback $key is already defined. Make sure your key is unique."
            }
    }

    override fun <UIEvent> initOrFindEventCallback(key: Any): EventCallback<UIEvent> {
        @Suppress("UNCHECKED_CAST")
        return eventCallbacks
            .getOrPut(key) { Holder(EventCallback<UIEvent>(key)) }
            .requestAccess {
                "Event callback $key is already defined. Make sure your key is unique."
            } as EventCallback<UIEvent>
    }

    /**
     * Creates the current [RenderModel] and prepares the next frame that will need to be processed.
     */
    override fun evaluate(
        formula: Formula<Input, State, Output, RenderModel>,
        input: Input,
        currentTransition: Long
    ): Evaluation<RenderModel> {
        // TODO: assert main thread.

        val lastFrame = frame
        if (lastFrame != null && lastFrame.isValid(input)) {
            updateTransitionNumber(currentTransition)
            return lastFrame.evaluation
        }

        val transitionCallback = TransitionCallbackWrapper(transitionLock, this::handleTransition, currentTransition)
        val context = FormulaContextImpl(currentTransition, this, transitionCallback)

        val prevInput = frame?.input
        if (prevInput != null && prevInput != input) {
            state = formula.onInputChanged(prevInput, input, state)
        }

        val result = formula.evaluate(input, state, context)
        val frame = Frame(input, state, result, transitionCallback, context.children, context.callbackCount)
        updateManager.updateEventListeners(frame.evaluation.updates)
        this.frame = frame

        if (lastFrame != null && lastFrame.callbackCount != frame.callbackCount) {
            val message = buildString {
                append("Dynamic callback registrations detected in ${formula::class}. ")
                append("Expected: ${lastFrame.callbackCount}, was: ${frame.callbackCount}.")
                append("Take a look at https://github.com/instacart/formula/blob/master/docs/Getting-Started.md#callbacks")
            }
            throw IllegalStateException(message)
        }

        disableOldCallbacks()

        // Set pending removal of children.
        val childIterator = children.iterator()
        while (childIterator.hasNext()) {
            val child = childIterator.next()
            if (!frame.children.contains(child.key)) {
                val processor = child.value
                processor.markAsTerminated()
                pendingTermination.add(processor)
                childIterator.remove()
            }
        }

        transitionCallback.running = true
        return result
    }

    private fun disableOldCallbacks() {
        val callbackIterator = callbacks.iterator()
        while (callbackIterator.hasNext()) {
            val callback = callbackIterator.next()
            if (!callback.value.requested) {
                callback.value.value.callback = {
                    // TODO log that disabled callback was invoked.
                }
                callbackIterator.remove()
            } else {
                callback.value.requested = false
            }
        }

        val eventCallbackIterator = eventCallbacks.iterator()
        while (eventCallbackIterator.hasNext()) {
            val entry = eventCallbackIterator.next()
            if (!entry.value.requested) {
                entry.value.value.callback = {
                    // TODO log that disabled callback was invoked.
                }

                eventCallbackIterator.remove()
            } else {
                entry.value.requested = false
            }
        }
    }

    override fun terminateOldUpdates(currentTransition: Long): Boolean {
        val newFrame = frame ?: throw IllegalStateException("call evaluate before calling nextFrame()")

        if (updateManager.terminateOld(newFrame.evaluation.updates, currentTransition)) {
            return true
        }

        // Step through children frames
        children.forEach {
            if (it.value.terminateOldUpdates(currentTransition)) {
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
        children.forEach {
            if (it.value.startNewUpdates(currentTransition)) {
                return true
            }
        }

        return false
    }

    private fun clearRemovedChildren(currentTransition: Long): Boolean {
        // Tear down old children
        val pendingTerminationIterator = pendingTermination.iterator()
        while (pendingTerminationIterator.hasNext()) {
            val child = pendingTerminationIterator.next()
            pendingTerminationIterator.remove()
            child.clearSideEffects()

            if (transitionLock.hasTransitioned(currentTransition)) {
                return true
            }
        }

        return false
    }

    override fun processSideEffects(currentTransition: Long): Boolean {
        children.forEach { child ->
            if (child.value.processSideEffects(currentTransition)) {
                return true
            }
        }

        // Perform pending side-effects
        val sideEffectIterator = pendingSideEffects.iterator()
        while (sideEffectIterator.hasNext()) {
            val sideEffect = sideEffectIterator.next()
            sideEffectIterator.remove()
            sideEffect.effect()

            if (transitionLock.hasTransitioned(currentTransition)) {
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

        if (clearRemovedChildren(currentTransition)) {
            return true
        }

        if (processSideEffects(currentTransition)) {
            return true
        }

        return transitionLock.hasTransitioned(currentTransition)
    }

    override fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
        input: ChildInput,
        key: FormulaKey,
        onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Output>,
        processingPass: Long
    ): Evaluation<ChildRenderModel> {
        @Suppress("UNCHECKED_CAST")
        val processorManager = (children[key] ?: run {
            val new = childManagerFactory.createChildManager(formula, input, transitionLock)
            children[key] = new
            new
        }) as FormulaManager<ChildInput, ChildState, ChildOutput, ChildRenderModel>

        processorManager.setTransitionListener { output, isValid ->
            val transition = output?.let { onEvent(Transition.Factory, it) } ?: Transition.Factory.none()
            handleTransition(transition, !isValid)
        }

        return processorManager.evaluate(formula, input, processingPass)
    }

    override fun markAsTerminated() {
        terminated = true

        // Clear callbacks
        callbacks.forEach { entry ->
            entry.value.value.callback = {
                // TODO log that event is invalid because child was removed
            }
        }
        callbacks.clear()

        eventCallbacks.forEach { entry ->
            entry.value.value.callback = {
                // TODO log that event is invalid because child was removed
            }
        }
        eventCallbacks.clear()

        // Terminate updates so no transitions happen
        updateManager.terminate()

        children.forEach {
            it.value.markAsTerminated()
        }
    }

    override fun clearSideEffects() {
        // Terminate children
        val childIterator = children.iterator()
        while (childIterator.hasNext()) {
            val child = childIterator.next()
            childIterator.remove()
            child.value.clearSideEffects()
        }

        // Perform pending side effects
        processSideEffects()
    }

    fun terminate() {
        markAsTerminated()
        clearSideEffects()
    }

    private fun processSideEffects() {
        // Clear side-effect queue
        val sideEffectIterator = pendingSideEffects.iterator()
        while (sideEffectIterator.hasNext()) {
            val sideEffect = sideEffectIterator.next()
            sideEffectIterator.remove()
            sideEffect.effect()
        }
    }
}
