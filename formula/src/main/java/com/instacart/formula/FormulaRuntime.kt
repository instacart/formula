package com.instacart.formula

import com.instacart.formula.internal.FormulaManager
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.ThreadChecker
import com.instacart.formula.internal.TransitionId
import com.instacart.formula.internal.TransitionListener
import com.instacart.formula.internal.TransitionIdManager
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
                        runtime.terminate()
                        runtime = FormulaRuntime(threadChecker, formula, emitter::onNext)
                    }
                    runtime.onInput(input)
                }, emitter::onError))

                val runnable = Runnable {
                    threadChecker.check("Need to unsubscribe on the main thread.")
                    runtime.terminate()
                }
                disposables.add(FormulaDisposableHelper.fromRunnable(runnable))

                emitter.setDisposable(disposables)
            }.distinctUntilChanged()
        }
    }

    private val implementation = formula.implementation()
    private var manager: FormulaManager<Input, Output>? = null
    private val transitionIdManager = TransitionIdManager()
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

            manager = FormulaManagerImpl(implementation, input, transitionListener)
            process(false)
            hasInitialFinished = true

            lastOutput?.let {
                onOutput(it)
            }
        } else {
            process(false)
        }
    }

    fun terminate() {
        manager?.apply {
            markAsTerminated()
            performTerminationSideEffects()
        }
    }

    /**
     * Processes the next frame.
     */
    private fun process(isValid: Boolean) {
        val localManager = checkNotNull(manager)
        val currentInput = checkNotNull(input)

        if (!isValid) { transitionIdManager.next() }

        processingRequested = true

        if (!isValid) {
            val result: Evaluation<Output> =
                localManager.evaluate(currentInput, transitionIdManager.transitionId)
            lastOutput = result.output
            emitOutput = true
        }

        if (isProcessing) return

        isProcessing = true
        while (processingRequested) {
            processingRequested = false
            processPass(localManager, transitionIdManager.transitionId)
        }
        isProcessing = false

        if (hasInitialFinished && emitOutput) {
            emitOutput = false
            onOutput(checkNotNull(lastOutput))
        }
    }

    private fun processPass(
        manager: FormulaManager<Input, Output>,
        transitionId: TransitionId
    ) {

        if (manager.terminateDetachedChildren(transitionId)) {
            return
        }

        if (manager.terminateOldUpdates(transitionId)) {
            return
        }

        if (manager.startNewUpdates(transitionId)) {
            return
        }

        while (effectQueue.isNotEmpty() && !transitionId.hasTransitioned()) {
            val effects = effectQueue.pollFirst()
            if (effects != null) {
                effects()
            }
        }
    }
}
