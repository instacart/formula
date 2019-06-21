package com.instacart.formula

import kotlin.reflect.KClass

class ProcessorManager<State, Effect>(
    state: State,
    private val onTransition: (Effect?) -> Unit
) : RealRxFormulaContext.Delegate<State, Effect> {

    private val workerManager = WorkerManager()
    internal val children: MutableMap<FormulaKey, ProcessorManager<*, *>> = mutableMapOf()
    private var state: State = state

    data class FormulaKey(
        val type: KClass<*>,
        val tag: String
    )

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
            } else {
                invoked = true

                state = it.state

                onTransition(it.effect)
            }
        })

        val result = formula.process(input, state, context)

        // Tear down old children
        children.keys.forEach {
            if (!context.children.containsKey(it)) {
                val processor = children.remove(it)

            }
        }


        workerManager.updateWorkers(result.workers)


        return result.copy()
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
