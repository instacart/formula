package com.instacart.formula

import com.instacart.formula.internal.FormulaManagerFactory
import com.instacart.formula.internal.FormulaManagerFactoryImpl
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.ScopedCallbacks
import com.instacart.formula.internal.ThreadChecker
import com.instacart.formula.internal.TransitionLockImpl
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import java.util.LinkedList

/**
 * Takes a [Formula] and creates an Observable<RenderModel> from it.
 */
class FormulaRuntime<Input : Any, State, Output, RenderModel>(
    private val threadChecker: ThreadChecker,
    private val formula: Formula<Input, State, Output, RenderModel>,
    private val childManagerFactory: FormulaManagerFactory,
    private val onEvent: (Output) -> Unit,
    private val onRenderModel: (RenderModel) -> Unit
) {

    companion object {
        /**
         * RuntimeExtensions.kt [state] calls this method.
         */
        fun <Input : Any, State, Output, RenderModel> start(
            input: Observable<Input>,
            formula: Formula<Input, State, Output, RenderModel>,
            onEvent: (Output) -> Unit,
            childManagerFactory: FormulaManagerFactory = FormulaManagerFactoryImpl()
        ): Observable<RenderModel> {
            val threadChecker = ThreadChecker()
            return Observable.create<RenderModel> { emitter ->
                threadChecker.check("Need to subscribe on main thread.")

                val runtime = FormulaRuntime(threadChecker, formula, childManagerFactory, onEvent, emitter::onNext)

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

    private var manager: FormulaManagerImpl<Input, State, Output, RenderModel>? = null
    private val lock = TransitionLockImpl()
    private var hasInitialFinished = false
    private var lastRenderModel: RenderModel? = null

    private val effects = LinkedList<Output>()

    private var input: Input? = null

    fun onInput(input: Input) {
        val initialization = this.input == null
        this.input = input

        if (initialization) {
            val processorManager: FormulaManagerImpl<Input, State, Output, RenderModel> =
                FormulaManagerImpl(
                    state = formula.initialState(input),
                    callbacks = ScopedCallbacks(formula),
                    transitionLock = lock,
                    childManagerFactory = childManagerFactory
                )

            processorManager.setTransitionListener { output, _ ->
                threadChecker.check("Only thread that created it can trigger transitions.")

                if (output != null) {
                    effects.push(output)
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
        val localManager = checkNotNull(manager)
        val currentInput = checkNotNull(input)

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
