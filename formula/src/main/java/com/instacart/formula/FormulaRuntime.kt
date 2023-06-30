package com.instacart.formula

import com.instacart.formula.internal.FormulaManager
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.ManagerDelegate
import com.instacart.formula.internal.ThreadChecker
import java.util.LinkedList
import kotlin.reflect.KClass

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
    private var executionRequested: Boolean = false

    private val effectQueue = LinkedList<Effects>()

    private var input: Input? = null
    private var key: Any? = null
    private var isExecuting: Boolean = false

    fun isKeyValid(input: Input): Boolean {
        return this.input == null || key == formula.key(input)
    }

    fun onInput(input: Input) {
        val initialization = this.input == null
        this.input = input
        this.key = formula.key(input)

        if (initialization) {
            manager = FormulaManagerImpl(this, implementation, input, inspector = inspector)
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

    override fun onTransition(formulaType: KClass<*>, result: Transition.Result<*>, evaluate: Boolean) {
        threadChecker.check("Only thread that created it can trigger transitions.")

        inspector?.onTransition(formulaType, result, evaluate)

        result.effects?.let {
            effectQueue.addLast(it)
        }

        run(shouldEvaluate = evaluate)
    }

    private fun forceRun() = run(shouldEvaluate = true)

    /**
     * Performs the evaluation and execution phases.
     *
     * @param shouldEvaluate Determines if evaluation needs to be run.
     */
    private fun run(shouldEvaluate: Boolean) {
        try {
            val freshRun = !isExecuting
            if (freshRun) {
                inspector?.onRunStarted(shouldEvaluate)
            }

            val manager = checkNotNull(manager)
            val currentInput = checkNotNull(input)

            if (shouldEvaluate && !manager.terminated) {
                evaluationPhase(manager, currentInput)
            }

            executionRequested = true
            if (isExecuting) return

            executionPhase(manager)

            if (freshRun) {
                inspector?.onRunFinished()
            }

            if (hasInitialFinished && emitOutput) {
                emitOutput = false
                onOutput(checkNotNull(lastOutput))
            }
        } catch (e: Throwable) {
            manager?.markAsTerminated()
            onError(e)
            manager?.performTerminationSideEffects()
        }
    }

    /**
     * Runs formula evaluation.
     */
    private fun evaluationPhase(manager: FormulaManager<Input, Output>, currentInput: Input) {
        val result = manager.evaluate(currentInput)
        lastOutput = result.output
        emitOutput = true

        if (isValidationEnabled) {
            try {
                manager.setValidationRun(true)

                // We run evaluation again in validation mode which ensures validates
                // that inputs and outputs are stable and do not break equality across
                // identical runs.
                manager.evaluate(currentInput)
            } finally {
                manager.setValidationRun(false)
            }
        }
    }

    /**
     * Executes operations containing side-effects such as starting/terminating streams.
     */
    private fun executionPhase(manager: FormulaManagerImpl<Input, *, Output>) {
        isExecuting = true
        while (executionRequested) {
            executionRequested = false

            val transitionId = manager.transitionID
            if (!manager.terminated) {
                if (manager.terminateDetachedChildren()) {
                    continue
                }

                if (manager.terminateOldUpdates()) {
                    continue
                }

                if (manager.startNewUpdates()) {
                    continue
                }
            }

            // We execute pending side-effects even after termination
            if (executeEffects(manager, transitionId)) {
                continue
            }
        }
        isExecuting = false
    }

    /**
     * Executes effects from the [effectQueue].
     */
    private fun executeEffects(manager: FormulaManagerImpl<*, *, *>, transitionId: Long): Boolean {
        while (effectQueue.isNotEmpty()) {
            val effects = effectQueue.pollFirst()
            if (effects != null) {
                effects.execute()

                if (manager.hasTransitioned(transitionId)) {
                    return true
                }
            }
        }
        return false
    }
}
