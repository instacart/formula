package com.instacart.formula

import com.instacart.formula.internal.FormulaManager
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.ThreadChecker
import com.instacart.formula.internal.TransitionListener
import com.instacart.formula.internal.TransitionLockImpl
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.FormulaDisposableHelper
import java.util.LinkedList

/**
 * Takes a [Formula] and creates an Observable<Output> from it.
 */
class FormulaRuntime<Input : Any, Output : Any>(
    private val threadChecker: ThreadChecker,
    formula: IFormula<Input, Output>,
    private val onOutput: (Output) -> Unit
) {

    companion object {
        /**
         * RuntimeExtensions.kt [start] calls this method.
         */
        fun <Input : Any, Output : Any> start(
            input: Observable<Input>,
            formula: IFormula<Input, Output>
        ): Observable<Output> {
            val threadChecker = ThreadChecker()
            return Observable.create<Output> { emitter ->
                threadChecker.check("Need to subscribe on main thread.")

                var runtime = FormulaRuntime(threadChecker, formula, emitter::onNext)

                val disposables = CompositeDisposable()
                disposables.add(input.subscribe({ input ->
                    threadChecker.check("Input arrived on a wrong thread.")
                    if (!runtime.isKeyValid(input)) {
                        runtime.manager?.terminate()
                        runtime = FormulaRuntime(threadChecker, formula, emitter::onNext)
                    }
                    runtime.onInput(input)
                }, emitter::onError))

                val runnable = Runnable {
                    threadChecker.check("Need to unsubscribe on the main thread.")
                    runtime.manager?.terminate()
                }
                disposables.add(FormulaDisposableHelper.fromRunnable(runnable))

                emitter.setDisposable(disposables)
            }.distinctUntilChanged()
        }
    }

    private val implementation = formula.implementation()
    private var manager: FormulaManager<Input, Output>? = null
    private val lock = TransitionLockImpl()
    private var hasInitialFinished = false
    private var emitOutput = false
    private var lastOutput: Output? = null
    private var processingRequested: Boolean = false

    private val effectQueue = LinkedList<Effects>()

    private var input: Input? = null
    private var key: Any? = null
    private var isProcessing: Boolean = false

    fun isKeyValid(input: Input): Boolean {
        return this.input == null || key == implementation.key(input)
    }

    fun onInput(input: Input) {
        val initialization = this.input == null
        this.input = input
        this.key = implementation.key(input)

        if (initialization) {
            val transitionListener = TransitionListener { effects, isValid ->
                threadChecker.check("Only thread that created it can trigger transitions.")

                if (effects != null) {
                    effectQueue.addLast(effects)
                }

                process(isValid)
            }

            val processorManager: FormulaManager<Input, Output> =
                FormulaManagerImpl(implementation, input, lock, transitionListener)

            manager = processorManager

            process(false)
            hasInitialFinished = true

            lastOutput?.let {
                onOutput(it)
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
            val result: Evaluation<Output> =
                localManager.evaluate(currentInput, processingPass)
            lastOutput = result.output
            emitOutput = true
        }

        if (isProcessing) return

        isProcessing = true
        while (processingRequested) {
            processingRequested = false
            processPass(localManager, lock.processingPass)
        }
        isProcessing = false

        if (hasInitialFinished && emitOutput) {
            emitOutput = false
            onOutput(checkNotNull(lastOutput))
        }
    }

    private fun processPass(
        localManager: FormulaManager<Input, Output>,
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
