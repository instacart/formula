package com.instacart.formula

import com.instacart.formula.internal.ProcessorManager
import io.reactivex.Observable

/**
 * Takes a [ProcessorFormula] and creates an Observable<RenderModel> from it.
 */
object ProcessorFormulaRxRuntime {
    fun <Input, State, Effect, RenderModel> start(
        input: Input,
        formula: ProcessorFormula<Input, State, Effect, RenderModel>,
        onEffect: (Effect) -> Unit
    ): Observable<RenderModel> {
        val threadName = Thread.currentThread().name
        val id = Thread.currentThread().id
        return Observable
            .create<RenderModel> { emitter ->
                checkThread(id, threadName)

                var manager: ProcessorManager<Input, State, Effect>? = null
                var hasInitialFinished = false
                var lastRenderModel: RenderModel? = null

                /**
                 * Processes the next frame.
                 */
                fun process() {
                    val localManager = manager!!
                    val result: ProcessResult<RenderModel> = localManager.process(formula, input)
                    lastRenderModel = result.renderModel
                    if (!localManager.nextFrame() && hasInitialFinished) {
                        emitter.onNext(result.renderModel)
                    }
                }

                val processorManager: ProcessorManager<Input, State, Effect> =
                    ProcessorManager(
                        state = formula.initialState(input),
                        onTransition = {
                            checkThread(id, threadName)

                            if (it != null) {
                                onEffect(it)
                            }

                            process()
                        }
                    )
                manager = processorManager

                emitter.setCancellable {
                    processorManager.terminate()
                }

                process()
                hasInitialFinished = true

                lastRenderModel?.let {
                    emitter.onNext(it)
                }
            }
            .distinctUntilChanged()
    }

    private fun checkThread(id: Long, name: String) {
        val thread = Thread.currentThread()
        if(thread.id != id) {
            throw IllegalStateException("Only thread that created it can trigger transitions. Expected: $name, Was: ${thread.name}")
        }
    }
}
