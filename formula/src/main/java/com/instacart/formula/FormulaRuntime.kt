package com.instacart.formula

import com.instacart.formula.internal.Event
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
    private var isEvaluating: Boolean = false
    private var isExecuting: Boolean = false

    fun isKeyValid(input: Input): Boolean {
        return this.input == null || key == formula.key(input)
    }

    fun onInput(input: Input) {
        val initialization = this.input == null
        this.input = input
        this.key = formula.key(input)

        if (initialization) {
            manager = FormulaManagerImpl(this, implementation, formula::class, input, inspector = inspector)
            forceRun()
            hasInitialFinished = true

            emitOutputIfNeeded(isInitialRun = true)
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

        run(evaluate = evaluate)
    }

    override fun onPostponedTransition(event: Event) {
        event.dispatch()
    }

    private fun forceRun() = run(evaluate = true)

    /**
     * Performs the evaluation and execution phases.
     *
     * @param evaluate Determines if evaluation needs to be run.
     */
    private fun run(evaluate: Boolean) {
        if (isEvaluating) return

        try {
            runFormula(evaluate)
            emitOutputIfNeeded(isInitialRun = false)
        } catch (e: Throwable) {
            isEvaluating = false

            manager?.markAsTerminated()
            onError(e)
            manager?.performTerminationSideEffects()
        }
    }

    private fun runFormula(evaluate: Boolean) {
        val freshRun = !isExecuting
        if (freshRun) {
            inspector?.onRunStarted(evaluate)
        }

        val manager = checkNotNull(manager)
        val currentInput = checkNotNull(input)

        if (evaluate && !manager.terminated) {
            isEvaluating = true
            evaluationPhase(manager, currentInput)
            isEvaluating = false
        }

        if (evaluate || effectQueue.isNotEmpty()) {
            executionRequested = true
        }

        if (isExecuting) return

        executeEffects()

        if (freshRun) {
            inspector?.onRunFinished()
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
     * Executes effects from the [effectQueue].
     */
    private fun executeEffects() {
        isExecuting = true
        // Walk through the effect queue and execute them
        while (effectQueue.isNotEmpty()) {
            val effects = effectQueue.pollFirst()
            if (effects != null) {
                effects.execute()
                inspector?.onEffectExecuted()
            }
        }
        isExecuting = false
    }

    private fun emitOutputIfNeeded(isInitialRun: Boolean,) {
        if (isInitialRun) {
            lastOutput?.let(onOutput)
        } else if (hasInitialFinished && emitOutput) {
            emitOutput = false
            onOutput(checkNotNull(lastOutput))
        }
    }
}
