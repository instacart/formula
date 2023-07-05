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

    override fun onUpdate(formulaType: KClass<*>, effects: Effects?, evaluate: Boolean) {
        threadChecker.check("Only thread that created it can trigger transitions.")

        if (evaluate) {
            effects?.let {
                effectQueue.addLast(it)
            }
            run()
        } else {
            if (isExecuting || isEvaluating) {
                // Add to the queue, it will be picked up by the loop
                effects?.let {
                    effectQueue.addLast(it)
                }
            } else {
                // We can just execute effects.
                effects?.execute()
            }
        }
    }

    /**
     * Performs the evaluation and execution phases.
     */
    private fun run() {
        if (isEvaluating) return

        try {
            runFormula()
            emitOutputIfNeeded(isInitialRun = false)
        } catch (e: Throwable) {
            isEvaluating = false

            manager?.markAsTerminated()
            onError(e)
            manager?.performTerminationSideEffects()
        }
    }

    private fun runFormula() {
        val freshRun = !isExecuting
        if (freshRun) {
            inspector?.onRunStarted(true)
        }

        val manager = checkNotNull(manager)
        val currentInput = checkNotNull(input)

        if (!manager.terminated) {
            isEvaluating = true
            evaluationPhase(manager, currentInput)
            isEvaluating = false
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
     * Executes effects from the [effectQueue].
     */
    private fun executeEffects() {
        isExecuting = true
        // Walk through the effect queue and execute them
        while (effectQueue.isNotEmpty()) {
            val effects = effectQueue.pollFirst()
            effects.execute()
        }
        isExecuting = false
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
