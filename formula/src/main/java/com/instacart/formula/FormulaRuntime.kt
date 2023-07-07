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

    /**
     * When we are within the [run] block, inputId allows us to notice when input has changed
     * and to re-run when that happens.
     */
    private var inputId: Int = 0

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
            inputId += 1
            run()
        }
    }

    fun terminate() {
        manager?.apply {
            markAsTerminated()

            /**
             * The way termination side-effects are performed:
             * - If we are not running, let's perform them here
             * - If we are running, runFormula() will handle them
             *
             * This way, we let runFormula() exit out before we terminate everything.
             */
            if (!isRunning) {
                performTerminationSideEffects()
            }
        }
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
            val manager = checkNotNull(manager)

            var run = true
            while (run) {
                val localInputId = inputId
                if (!manager.terminated) {
                    isRunning = true
                    inspector?.onRunStarted(true)

                    val currentInput = checkNotNull(input)
                    runFormula(manager, currentInput)
                    isRunning = false

                    inspector?.onRunFinished()

                    /**
                     * If termination happened during runFormula() execution, let's perform
                     * termination side-effects here.
                     */
                    if (manager.terminated) {
                        run = false
                        manager.performTerminationSideEffects()
                    } else {
                        run = localInputId != inputId
                    }
                } else {
                    run = false
                }
            }

            if (!manager.terminated) {
                emitOutputIfNeeded(isInitialRun = false)
            }
        } catch (e: Throwable) {
            isRunning = false

            manager?.markAsTerminated()
            onError(e)
            manager?.performTerminationSideEffects()
        }
    }

    /**
     * Runs formula evaluation.
     */
    private fun runFormula(manager: FormulaManager<Input, Output>, currentInput: Input) {
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
