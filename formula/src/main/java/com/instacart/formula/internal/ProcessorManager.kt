package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Transition

/**
 * Handles state processing.
 */
class ProcessorManager<Input, State, Effect>(
    state: State,
    private val onTransition: (Effect?) -> Unit
) : FormulaContextImpl.Delegate<State, Effect>, TransitionLock {

    private val workerManager = UpdateManager(this)

    internal val children: MutableMap<FormulaKey, ProcessorManager<*, *, *>> = mutableMapOf()
    internal var frame: Frame? = null
    internal var transitionNumber: Long = 0

    private var state: State = state
    private var lastInput: Input? = null

    private fun handleTransition(transition: Transition<State, Effect>) {
        transitionNumber += 1
        this.state = transition.state ?: this.state
        onTransition(transition.effect)
    }

    /**
     * Used within [nextFrame] to indicate if the [nextFrame] has triggered a transition change.
     * Transition change means that the state has changed so we need to short circuit and do
     * another processing round.
     */
    override fun hasTransitioned(transitionNumber: Long) = this.transitionNumber != transitionNumber

    /**
     * Creates the current [RenderModel] and prepares the next frame that will need to be processed.
     */
    fun <RenderModel> process(
        formula: Formula<Input, State, Effect, RenderModel>,
        input: Input
    ): Evaluation<RenderModel> {
        // TODO: assert main thread.

        var canRun = false
        var invoked = false

        val context = FormulaContextImpl(this, onChange = {
            // TODO assert main thread

            if (!canRun) {
                throw IllegalStateException("Transitions are not allowed while processing")
            }

            if (invoked) {
                // Some event already won the race
                throw IllegalStateException("event won the race, this shouldn't happen: $it")
            } else {
                invoked = true

                handleTransition(it)
            }
        })

        val prevInput = lastInput
        val state = if (prevInput != null && prevInput != input) {
            formula.onInputChanged(prevInput, input, state)
        } else {
            state
        }

        val result = formula.evaluate(input, state, context)
        frame = Frame(result.updates, context.children)

        this.lastInput = input
        canRun = true
        return result
    }

    /**
     * Returns true if has transition while moving to next frame.
     */
    fun nextFrame(): Boolean {
        val newFrame = frame ?: throw IllegalStateException("call evaluate before calling nextFrame()")

        val thisTransition = transitionNumber

        // Need to perform units of work.

        // Tear down old children
        children.forEach {
            if (!newFrame.children.containsKey(it.key)) {
                val processor = children.remove(it.key)
                processor?.terminate()

                if (hasTransitioned(thisTransition)) {
                    return true
                }
            }
        }

        children.forEach {
            if (it.value.nextFrame()) {
                return true
            }
        }

        // Should parents workers have priority?
        workerManager.updateConnections(newFrame.updates, thisTransition)
        return hasTransitioned(thisTransition)
    }

    override fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
        input: ChildInput,
        key: FormulaKey,
        onEffect: Transition.Factory.(ChildEffect) -> Transition<State, Effect>
    ): Evaluation<ChildRenderModel> {
        val processorManager = (children[key] ?: run {
            val initial = formula.initialState(input)
            val new = ProcessorManager<ChildInput, ChildState, ChildEffect>(initial, onTransition = {
                // TODO assert main thread

                val effect = if (it != null) {
                    val result = onEffect(Transition.Factory, it)
                    this.state = result.state ?: this.state
                    result.effect
                } else {
                    null
                }

                transitionNumber += 1
                onTransition(effect)
            })
            children[key] = new
            new
        }) as ProcessorManager<ChildInput, ChildState, ChildEffect>

        return processorManager.process(formula, input)
    }

    fun terminate() {
        children.forEach { it.value.terminate() }
        children.clear()
        workerManager.terminate()
    }
}
