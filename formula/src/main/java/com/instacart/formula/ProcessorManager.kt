package com.instacart.formula

import kotlin.reflect.KClass

class ProcessorManager<State, Effect>(
    state: State,
    private val onTransition: (Effect?) -> Unit
) : RealRxFormulaContext.Delegate<State, Effect> {

    private val workerManager = WorkerManager(this)
    internal val children: MutableMap<FormulaKey, ProcessorManager<*, *>> = mutableMapOf()
    internal var frame: Frame? = null
    internal var transitionNumber: Long = 0

    private var state: State = state

    data class FormulaKey(
        val type: KClass<*>,
        val tag: String
    )

    internal fun hasTransitioned(transitionNumber: Long) = this.transitionNumber != transitionNumber

    /**
     * Creates a next frame that will need to be stepped through.
     */
    fun <Input, RenderModel> process(
        formula: ProcessorFormula<Input, State, Effect, RenderModel>,
        input: Input
    ): ProcessResult<RenderModel> {
        // TODO: assert main thread.

        var invoked = false

        val context = RealRxFormulaContext(this, onChange = {
            // TODO assert main thread
            if (invoked) {
                // Some event already won the race
                throw IllegalStateException("event won the race, this shouldn't happen: $it")
            } else {
                invoked = true
//                transitioned = true
                transitionNumber += 1

                state = it.state
                onTransition(it.effect)
            }
        })

        val result = formula.process(input, state, context)
        frame = Frame(result.workers, context.children)

        if (invoked) {
            throw IllegalStateException("Should not transition while processing")
        }

        return result
    }

    /**
     * Returns true if has transition while moving to next frame.
     */
    fun nextFrame(): Boolean {
        val newFrame = frame ?: throw IllegalStateException("call process before calling nextFrame()")

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
        workerManager.updateWorkers(newFrame.workers, thisTransition)
        return hasTransitioned(thisTransition)
    }

    override fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
        formula: ProcessorFormula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
        input: ChildInput,
        key: FormulaKey,
        onEffect: (ChildEffect) -> Transition<State, Effect>
    ): ProcessResult<ChildRenderModel> {
        val processorManager = (children[key] ?: run {
            val initial = formula.initialState(input)
            val new = ProcessorManager<ChildState, ChildEffect>(initial, onTransition = {
                // TODO assert main thread
                val effect = if (it != null) {
                    val result = onEffect(it)
                    this.state = result.state
                    result.effect
                } else {
                    null
                }

                transitionNumber += 1;
                onTransition(effect)
            })
            children[key] = new
            new
        }) as ProcessorManager<ChildState, ChildEffect>

        return processorManager.process(formula, input)
    }

    fun terminate() {
        children.forEach { it.value.terminate() }
        children.clear()
        workerManager.terminate()
    }
}
