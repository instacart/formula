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
    private var executionRequested: Boolean = false

    private val effectQueue = LinkedList<Effects>()

    private var input: Input? = null
    private var key: Any? = null
    private var isExecuting: Boolean = false

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

                run(isValid)
            }

            manager = FormulaManagerImpl(implementation, input, transitionListener)
            forceRun()
            hasInitialFinished = true

            lastOutput?.let {
                onOutput(it)
            }
        } else {
            forceRun()
        }
    }

    fun terminate() {
        manager?.apply {
            markAsTerminated()
            performTerminationSideEffects()
        }
    }

    private fun forceRun() = run(isValid = false)

    /**
     * Performs the evaluation and execution phases.
     *
     * @param isValid Determines if evaluation needs to be run.
     */
    private fun run(isValid: Boolean) {
        val manager = checkNotNull(manager)
        val currentInput = checkNotNull(input)

        if (!isValid) {
            evaluationPhase(manager, currentInput)
        }

        executionRequested = true
        if (isExecuting) return

        executionPhase(manager)

        if (hasInitialFinished && emitOutput) {
            emitOutput = false
            onOutput(checkNotNull(lastOutput))
        }
    }

    /**
     * Runs formula evaluation.
     */
    private fun evaluationPhase(manager: FormulaManager<Input, Output>, currentInput: Input) {
        transitionIdManager.invalidated()

        val result = manager.evaluate(currentInput, transitionIdManager.transitionId)
        lastOutput = result.output
        emitOutput = true
    }

    /**
     * Executes operations containing side-effects such as starting/terminating streams.
     */
    private fun executionPhase(manager: FormulaManager<Input, Output>) {
        isExecuting = true
        while (executionRequested) {
            executionRequested = false

            val transitionId = transitionIdManager.transitionId
            if (manager.terminateDetachedChildren(transitionId)) {
                continue
            }

            if (manager.terminateOldUpdates(transitionId)) {
                continue
            }

            if (manager.startNewUpdates(transitionId)) {
                continue
            }

            if (executeEffects(transitionId)) {
                continue
            }
        }
        isExecuting = false
    }

    /**
     * Executes effects from the [effectQueue].
     */
    private fun executeEffects(transitionId: TransitionId): Boolean {
        while (effectQueue.isNotEmpty()) {
            val effects = effectQueue.pollFirst()
            if (effects != null) {
                effects()

                if (transitionId.hasTransitioned()) {
                    return true
                }
            }
        }
        return false
    }
}
