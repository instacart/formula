package com.instacart.formula

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.subjects.PublishSubject

object ProcessorFormulaRxRuntime {
    fun <Input, State, Effect, RenderModel> start(
        input: Input,
        formula: ProcessorFormula<Input, State, Effect, RenderModel>,
        onEffect: (Effect) -> Unit
    ): Observable<RenderModel> {

        return Observable
            .create<RenderModel> { emitter ->
                var manager: ProcessorManager<State, Effect>? = null
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

                val processorManager: ProcessorManager<State, Effect> = ProcessorManager(
                    state = formula.initialState(input),
                    onTransition = {
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
}
