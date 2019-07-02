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
class ProcessorManager<Input, State, Effect>(
    state: State,
    private val transitionLock: TransitionLock,
    private val onTransition: (Effect?) -> Unit
) : FormulaContextImpl.Delegate<State, Effect> {

    private val updateManager = UpdateManager(transitionLock)

    internal val children: MutableMap<FormulaKey, ProcessorManager<*, *, *>> = mutableMapOf()
    internal val pendingTermination = mutableListOf<ProcessorManager<*, *, *>>()
    internal var frame: Frame? = null
    private var terminated = false

    private var state: State = state
    private var lastInput: Input? = null

    private var pendingSideEffects = mutableListOf<SideEffect>()

    private fun handleTransition(transition: Transition<State, Effect>) {
        pendingSideEffects.addAll(transition.sideEffects)
        this.state = transition.state ?: this.state

        if (!terminated) {
            onTransition(transition.output)
        }
    }

    /**
     * Creates the current [RenderModel] and prepares the next frame that will need to be processed.
     */
    fun <RenderModel> evaluate(
        formula: Formula<Input, State, Effect, RenderModel>,
        input: Input,
        currentTransition: Long
    ): Evaluation<RenderModel> {
        // TODO: assert main thread.

        var canRun = false

        val context = FormulaContextImpl(currentTransition, this, onChange = {
            if (!canRun) {
                throw IllegalStateException("Transitions are not allowed during evaluation")
            }

            if (transitionLock.hasTransitioned(currentTransition)) {
                // Some event already won the race
                throw IllegalStateException("event won the race, this shouldn't happen: $it")
            } else {
                handleTransition(it)
            }
        })

        val prevInput = lastInput
        if (prevInput != null && prevInput != input) {
            state = formula.onInputChanged(prevInput, input, state)
        }

        val result = formula.evaluate(input, state, context)
        val frame = Frame(result.updates, context.children)
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

    private fun processUpdates(currentTransition: Long): Boolean {
        val newFrame = frame ?: throw IllegalStateException("call evaluate before calling nextFrame()")

        // Update parent workers so they are ready to handle events
        if (updateManager.updateConnections(newFrame.updates, currentTransition)) {
            return true
        }

        // Step through children frames
        children.forEach {
            if (it.value.processUpdates(currentTransition)) {
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

    private fun processSideEffects(currentTransition: Long): Boolean {
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
        if (processUpdates(currentTransition)) {
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
        onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Effect>,
        processingPass: Long
    ): Evaluation<ChildRenderModel> {
        val processorManager = (children[key] ?: run {
            val initial = formula.initialState(input)
            val new = ProcessorManager<ChildInput, ChildState, ChildOutput>(initial, transitionLock, onTransition = {
                val transition = it?.let { onEvent(Transition.Factory, it) } ?: Transition.Factory.none()
                handleTransition(transition)
            })
            children[key] = new
            new
        }) as ProcessorManager<ChildInput, ChildState, ChildOutput>

        return processorManager.evaluate(formula, input, processingPass)
    }

    private fun markAsTerminated() {
        terminated = true

        // Terminate updates so no transitions happen
        updateManager.terminate()

        children.forEach {
            it.value.markAsTerminated()
        }
    }

    private fun clearSideEffects() {
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
