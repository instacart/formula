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
class ProcessorManager<Input, State, Output, RenderModel>(
    state: State,
    private val transitionLock: TransitionLock,
    private val childManagerFactory: FormulaManagerFactory
) : FormulaContextImpl.Delegate<State, Output>, FormulaManager<Input, State, Output, RenderModel> {

    private val updateManager = UpdateManager(transitionLock)

    internal val children: MutableMap<FormulaKey, FormulaManager<*, *, *, *>> = mutableMapOf()
    internal val pendingTermination = mutableListOf<FormulaManager<*, *, *, *>>()
    internal var frame: Frame? = null
    private var terminated = false

    private var state: State = state
    private var lastInput: Input? = null

    private var pendingSideEffects = mutableListOf<SideEffect>()

    private var onTransition: ((Output?) -> Unit)? = null

    private fun handleTransition(transition: Transition<State, Output>) {
        pendingSideEffects.addAll(transition.sideEffects)
        this.state = transition.state ?: this.state

        if (!terminated) {
            onTransition?.invoke(transition.output)
        }
    }

    override fun setTransitionListener(listener: (Output?) -> Unit) {
        onTransition = listener
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

        var canRun = false

        val context = FormulaContextImpl(currentTransition, this, transitionCallback = {
            if (!canRun) {
                throw IllegalStateException("Transitions are not allowed during evaluation")
            }

            if (TransitionUtils.isEmpty(it)) {
                return@FormulaContextImpl
            }

            if (transitionLock.hasTransitioned(currentTransition)) {
                // We have already transitioned, this should not happen.
                throw IllegalStateException("Transition already happened. This is using old transition callback: $it.")
            }

            handleTransition(it)
        })

        val prevInput = lastInput
        if (prevInput != null && prevInput != input) {
            state = formula.onInputChanged(prevInput, input, state)
        }

        val result = formula.evaluate(input, state, context)
        val frame = Frame(result.updates, context.children)
        updateManager.updateEventListeners(frame.updates)
        this.frame = frame


        // Set pending removal of children.
        val childIterator = children.iterator()
        while (childIterator.hasNext()) {
            val child = childIterator.next()
            if (!frame.children.containsKey(child.key)) {
                val processor = child.value
                processor.markAsTerminated()
                pendingTermination.add(processor)
                childIterator.remove()
            }
        }

        this.lastInput = input
        canRun = true
        return result
    }

    override fun terminateOldUpdates(currentTransition: Long): Boolean {
        val newFrame = frame ?: throw IllegalStateException("call evaluate before calling nextFrame()")

        if (updateManager.terminateOld(newFrame.updates, currentTransition)) {
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
        if (updateManager.startNew(newFrame.updates, currentTransition)) {
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
        val processorManager = (children[key] ?: run {
            val new = childManagerFactory.createChildManager(formula, input, transitionLock)
            children[key] = new
            new
        }) as FormulaManager<ChildInput, ChildState, ChildOutput, ChildRenderModel>

        processorManager.setTransitionListener {
            val transition = it?.let { onEvent(Transition.Factory, it) } ?: Transition.Factory.none()
            handleTransition(transition)
        }

        return processorManager.evaluate(formula, input, processingPass)
    }

    override fun markAsTerminated() {
        terminated = true

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
