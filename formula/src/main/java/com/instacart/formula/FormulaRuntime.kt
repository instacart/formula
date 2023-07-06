package com.instacart.formula

import com.instacart.formula.internal.DeferredTransition
import com.instacart.formula.internal.FormulaManager
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.ManagerDelegate
import com.instacart.formula.internal.ThreadChecker

/**
 * Takes a [Formula] and creates an Observable<Output> from it.
 */
class FormulaRuntime<Input : Any, Output : Any>(
    private val threadChecker: ThreadChecker,
    private val formula: IFormula<Input, Output>,
    private val onOutput: (Output) -> Unit,
    private val onError: (Throwable) -> Unit,
    private val isValidationEnabled: Boolean = false,
    private val inspector: Inspector? = null,
) : ManagerDelegate {
    private val implementation = formula.implementation()
    private var manager: FormulaManagerImpl<Input, *, Output>? = null
    private var hasInitialFinished = false
    private var emitOutput = false
    private var lastOutput: Output? = null

    private var input: Input? = null
    private var key: Any? = null

    /**
     * Determines if we are executing within [runFormula] block. It prevents to
     * enter [runFormula] block when we are already within it.
     */
    private var isRunning: Boolean = false

    fun isKeyValid(input: Input): Boolean {
        return this.input == null || key == formula.key(input)
    }

    fun onInput(input: Input) {
        val initialization = this.input == null
        this.input = input
        this.key = formula.key(input)

        if (initialization) {
            manager = FormulaManagerImpl(this, implementation, input, loggingType = formula::class, inspector = inspector)
            run()

            hasInitialFinished = true
            emitOutputIfNeeded(isInitialRun = true)
        } else {
            run()
        }
    }

    fun terminate() {
        manager?.apply {
            markAsTerminated()
            performTerminationSideEffects()
        }
    }

    override fun onPendingTransition(transition: DeferredTransition<*, *, *>) {
        threadChecker.check("Only thread that created it can trigger transitions.")
        transition.execute()
    }

    override fun requestEvaluation() {
        threadChecker.check("Only thread that created it can request evaluation.")

        run()
    }

    /**
     * Performs the evaluation and execution phases.
     */
    private fun run() {
        if (isRunning) return

        try {
            runFormula()
            emitOutputIfNeeded(isInitialRun = false)
        } catch (e: Throwable) {
            isRunning = false

            manager?.markAsTerminated()
            onError(e)
            manager?.performTerminationSideEffects()
        }
    }

    private fun runFormula() {
        val freshRun = !isRunning
        if (freshRun) {
            inspector?.onRunStarted(true)
        }

        val manager = checkNotNull(manager)
        val currentInput = checkNotNull(input)

        if (!manager.terminated) {
            isRunning = true
            evaluationPhase(manager, currentInput)
            isRunning = false
        }

        if (freshRun) {
            inspector?.onRunFinished()
        }
    }

    /**
     * Runs formula evaluation.
     */
    private fun evaluationPhase(manager: FormulaManager<Input, Output>, currentInput: Input) {
        val result = manager.run(currentInput)
        lastOutput = result.output
        emitOutput = true

        if (isValidationEnabled) {
            try {
                manager.setValidationRun(true)

                // We run evaluation again in validation mode which ensures validates
                // that inputs and outputs are stable and do not break equality across
                // identical runs.
                manager.run(currentInput)
            } finally {
                manager.setValidationRun(false)
            }
        }
    }

    /**
     * Emits output to the formula subscriber.
     */
    private fun emitOutputIfNeeded(isInitialRun: Boolean) {
        if (isInitialRun) {
            lastOutput?.let(onOutput)
        } else if (hasInitialFinished && emitOutput) {
            emitOutput = false
            onOutput(checkNotNull(lastOutput))
        }
    }
}
