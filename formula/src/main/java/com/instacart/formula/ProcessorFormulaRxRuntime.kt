package com.instacart.formula

import com.instacart.formula.internal.ProcessorManager
import com.instacart.formula.internal.TransitionLockImpl
import io.reactivex.Observable
import java.util.LinkedList

/**
 * Takes a [Formula] and creates an Observable<RenderModel> from it.
 */
object ProcessorFormulaRxRuntime {
    fun <Input, State, Output, RenderModel> start(
        input: Input,
        formula: Formula<Input, State, Output, RenderModel>,
        onEvent: (Output) -> Unit
    ): Observable<RenderModel> {
        val threadName = Thread.currentThread().name
        val id = Thread.currentThread().id
        return Observable
            .create<RenderModel> { emitter ->
                checkThread(id, threadName)

                val lock = TransitionLockImpl()
                var manager: ProcessorManager<Input, State, Output>? = null
                var hasInitialFinished = false
                var lastRenderModel: RenderModel? = null

                val effects = LinkedList<Output>()

                /**
                 * Processes the next frame.
                 */
                fun process() {
                    val processingPass = lock.next()
                    val localManager = manager!!
                    val result: Evaluation<RenderModel> = localManager.evaluate(formula, input, processingPass)
                    lastRenderModel = result.renderModel

                    if (localManager.nextFrame(processingPass)) {
                        return
                    }

                    while (effects.isNotEmpty()) {
                        val first = effects.pollFirst()
                        if (first != null) {
                            onEvent(first)

                            if (lock.hasTransitioned(processingPass)) {
                                return
                            }
                        }
                    }

                    if (hasInitialFinished) {
                        emitter.onNext(result.renderModel)
                    }
                }

                val processorManager: ProcessorManager<Input, State, Output> = ProcessorManager(
                    state = formula.initialState(input),
                    transitionLock = lock,
                    onTransition = {
                        checkThread(id, threadName)

                        if (it != null) {
                            effects.push(it)
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
        if (thread.id != id) {
            throw IllegalStateException("Only thread that created it can trigger transitions. Expected: $name, Was: ${thread.name}")
        }
    }
}
