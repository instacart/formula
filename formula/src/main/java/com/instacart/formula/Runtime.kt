package com.instacart.formula

import com.instacart.formula.internal.ProcessorManager
import com.instacart.formula.internal.ThreadChecker
import com.instacart.formula.internal.TransitionLockImpl
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import java.util.LinkedList

/**
 * Takes a [Formula] and creates an Observable<RenderModel> from it.
 */
class Runtime<Input, State, Output, RenderModel>(
    private val threadChecker: ThreadChecker,
    private val formula: Formula<Input, State, Output, RenderModel>,
    private val onEvent: (Output) -> Unit,
    private val onRenderModel: (RenderModel) -> Unit
) {

    companion object {
        /**
         * RuntimeExtensions.kt [state] calls this method.
         */
        fun <Input, State, Output, RenderModel> start(
            input: Observable<Input>,
            formula: Formula<Input, State, Output, RenderModel>,
            onEvent: (Output) -> Unit
        ): Observable<RenderModel> {
            val threadChecker = ThreadChecker()
            return Observable.create<RenderModel> { emitter ->
                threadChecker.check("Need to subscribe on main thread.")

                val runtime = Runtime(threadChecker, formula, onEvent, emitter::onNext)

                val disposables = CompositeDisposable()
                disposables.add(input.subscribe({ input ->
                    threadChecker.check("Input arrived on a wrong thread.")
                    runtime.onInput(input)
                }, emitter::onError))

                disposables.add(Disposables.fromRunnable {
                    threadChecker.check("Need to unsubscribe on the main thread.")
                    runtime.manager?.terminate()
                })

                emitter.setDisposable(disposables)
            }.distinctUntilChanged()
        }
    }

    var manager: ProcessorManager<Input, State, Output>? = null
    val lock = TransitionLockImpl()
    var hasInitialFinished = false
    var lastRenderModel: RenderModel? = null

    val effects = LinkedList<Output>()

    private var input: Input? = null

    fun onInput(input: Input) {
        val initialization = this.input == null
        this.input = input

        if (initialization) {
            val processorManager: ProcessorManager<Input, State, Output> =
                ProcessorManager(
                    state = formula.initialState(input),
                    transitionLock = lock
                )

            processorManager.onTransition = {
                threadChecker.check("Only thread that created it can trigger transitions.")

                if (it != null) {
                    effects.push(it)
                }

                process()
            }
            manager = processorManager

            process()
            hasInitialFinished = true

            lastRenderModel?.let {
                onRenderModel(it)
            }
        } else {
            process()
        }
    }

    /**
     * Processes the next frame.
     */
    private fun process() {
        val localManager = manager ?: throw IllegalStateException("manager not initialized")
        val currentInput = input ?: throw IllegalStateException("input not initialized")

        val processingPass = lock.next()
        val result: Evaluation<RenderModel> = localManager.evaluate(formula, currentInput, processingPass)
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
            onRenderModel(result.renderModel)
        }
    }
}
