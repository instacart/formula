package com.instacart.formula

import com.instacart.formula.internal.FormulaLogger
import com.instacart.formula.internal.FormulaManager
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.ThreadChecker
import com.instacart.formula.internal.TransitionId
import com.instacart.formula.internal.TransitionIdManager
import com.instacart.formula.internal.TransitionListener
import java.util.LinkedList

/**
 * Takes a [Formula] and creates an Observable<Output> from it.
 */
class FormulaRuntime<Input : Any, Output : Any>(
    private val threadChecker: ThreadChecker,
    private val formula: IFormula<Input, Output>,
    private val loggerDelegate: Logger?,
    private val onOutput: (Output) -> Unit,
    private val onError: (Throwable) -> Unit
) {
    private val implementation = formula.implementation()
    private var manager: FormulaManagerImpl<Input, *, Output>? = null
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
        this.key = formula.key(input)

        if (initialization) {
            val transitionListener = TransitionListener { transition, isValid ->
                threadChecker.check("Only thread that created it can trigger transitions.")

                transition.effects?.let {
                    effectQueue.addLast(it)
                }

                run(shouldEvaluate = !isValid)
            }

            val logger = FormulaLogger(formula, key, loggerDelegate)
            manager = FormulaManagerImpl(implementation, input, transitionListener, logger)
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

    private fun forceRun() = run(shouldEvaluate = true)

    /**
     * Performs the evaluation and execution phases.
     *
     * @param shouldEvaluate Determines if evaluation needs to be run.
     */
    private fun run(shouldEvaluate: Boolean) {
        try {
            val manager = checkNotNull(manager)
            val currentInput = checkNotNull(input)

            if (shouldEvaluate && !manager.terminated) {
                if (isExecuting) {
                    manager.logger.log { "execution:stopped (finished early due to a transition)"}
                }
                evaluationPhase(manager, currentInput)
            }

            executionRequested = true
            if (isExecuting) return

            executionPhase(manager)

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
        transitionIdManager.invalidated()

        val result = manager.evaluate(currentInput, transitionIdManager.transitionId)
        lastOutput = result.output
        emitOutput = true
    }

    /**
     * Executes operations containing side-effects such as starting/terminating streams.
     */
    private fun executionPhase(manager: FormulaManagerImpl<Input, *, Output>) {
        isExecuting = true

        val logger = manager.logger
        while (executionRequested) {
            executionRequested = false
            logger.log { "execution:started" }

            val transitionId = transitionIdManager.transitionId
            if (!manager.terminated) {
                if (manager.terminateDetachedChildren(transitionId)) {
                    continue
                }

                if (manager.terminateOldUpdates(transitionId)) {
                    continue
                }

                if (manager.startNewUpdates(transitionId)) {
                    continue
                }
            }

            // We execute pending side-effects even after termination
            if (executeEffects(logger, transitionId)) {
                continue
            }

            logger.log { "execution:finished" }
        }

        isExecuting = false
    }

    /**
     * Executes effects from the [effectQueue].
     */
    private fun executeEffects(logger: FormulaLogger, transitionId: TransitionId): Boolean {
        while (effectQueue.isNotEmpty()) {
            val effects = effectQueue.pollFirst()
            if (effects != null) {
                logger.log { "side-effects:execute ${toDisplayName(effects)}" }
                effects()

                if (transitionId.hasTransitioned()) {
                    return true
                }
            }
        }

        return false
    }

    private fun toDisplayName(effects: Effects): String {
        return effects.javaClass.name
            .replace("$$", "-")
            .replace('$', '-')
    }
}
