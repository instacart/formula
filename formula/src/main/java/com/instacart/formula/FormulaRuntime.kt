package com.instacart.formula

import com.instacart.formula.internal.FormulaManagerFactory
import com.instacart.formula.internal.FormulaManagerFactoryImpl
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.ScopedCallbacks
import com.instacart.formula.internal.ThreadChecker
import com.instacart.formula.internal.TransitionListener
import com.instacart.formula.internal.TransitionLockImpl
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import java.util.LinkedList

/**
 * Takes a [Formula] and creates an Observable<RenderModel> from it.
 */
class FormulaRuntime<Input : Any, State, RenderModel : Any>(
    private val threadChecker: ThreadChecker,
    private val formula: Formula<Input, State, RenderModel>,
    private val childManagerFactory: FormulaManagerFactory,
    private val onRenderModel: (RenderModel) -> Unit
) {

    companion object {
        /**
         * RuntimeExtensions.kt [start] calls this method.
         */
        fun <Input : Any, State,  RenderModel : Any> start(
            input: Observable<Input>,
            formula: Formula<Input, State, RenderModel>,
            childManagerFactory: FormulaManagerFactory = FormulaManagerFactoryImpl()
        ): Observable<RenderModel> {
            val threadChecker = ThreadChecker()
            return Observable.create<RenderModel> { emitter ->
                threadChecker.check("Need to subscribe on main thread.")

                val runtime = FormulaRuntime(threadChecker, formula, childManagerFactory, emitter::onNext)

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

    private var manager: FormulaManagerImpl<Input, State, RenderModel>? = null
    private val lock = TransitionLockImpl()
    private var hasInitialFinished = false
    private var emitRenderModel = false
    private var lastRenderModel: RenderModel? = null
    private var processingRequested: Boolean = false

    private val effectQueue = LinkedList<Effects>()

    private var input: Input? = null
    private var isProcessing: Boolean = false

    fun onInput(input: Input) {
        val initialization = this.input == null
        this.input = input

        if (initialization) {
            val processorManager: FormulaManagerImpl<Input, State, RenderModel> =
                FormulaManagerImpl(
                    formula = formula,
                    initialInput = input,
                    callbacks = ScopedCallbacks(formula),
                    transitionLock = lock,
                    childManagerFactory = childManagerFactory,
                    transitionListener = TransitionListener { effects, isValid ->
                        threadChecker.check("Only thread that created it can trigger transitions.")

                        if (effects != null) {
                            effectQueue.addLast(effects)
                        }

                        process(isValid)
                    }
                )

            manager = processorManager

            process(false)
            hasInitialFinished = true

            lastRenderModel?.let {
                onRenderModel(it)
            }
        } else {
            process(false)
        }
    }

    /**
     * Processes the next frame.
     */
    private fun process(isValid: Boolean) {
        val localManager = checkNotNull(manager)
        val currentInput = checkNotNull(input)

        val processingPass = if (isValid) {
            lock.processingPass
        } else {
            lock.next()
        }
        processingRequested = true

        if (!isValid) {
            val result: Evaluation<RenderModel> =
                localManager.evaluate(currentInput, processingPass)
            lastRenderModel = result.renderModel
            emitRenderModel = true
        }

        if (isProcessing) return

        isProcessing = true
        while (processingRequested) {
            processingRequested = false
            processPass(localManager, lock.processingPass)
        }
        isProcessing = false

        if (hasInitialFinished && emitRenderModel) {
            emitRenderModel = false
            onRenderModel(checkNotNull(lastRenderModel))
        }
    }

    private fun processPass(
        localManager: FormulaManagerImpl<Input, State, RenderModel>,
        processingPass: Long
    ) {

        if (localManager.nextFrame(processingPass)) {
            return
        }

        while (effectQueue.isNotEmpty() && !lock.hasTransitioned(processingPass)) {
            val effects = effectQueue.pollFirst()
            if (effects != null) {
                effects()
            }
        }
    }
}
