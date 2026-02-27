package com.instacart.formula.internal

import com.instacart.formula.Effect
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import com.instacart.formula.plugin.ChildAlreadyUsedException
import com.instacart.formula.plugin.FormulaError
import java.util.LinkedList

/**
 * Responsible for keeping track of formula's state, running actions, and child formulas. The
 * main entry point is the [run] method which will call [Formula.evaluate]. After evaluation, it
 * will call [postEvaluation], which will process the new state by cleaning up detached child
 * formulas, terminating old actions, and then starting new ones. If at any given point there is
 * a state change, it will rerun [Formula.evaluate].
 */
internal class FormulaManagerImpl<Input, State, Output>(
    private val delegate: ManagerDelegate,
    private val formula: Formula<Input, State, Output>,
    override val formulaType: Class<*>,
    initialInput: Input,
) : FormulaManager<Input, Output>, ManagerDelegate by delegate, ActionDelegate, EffectDelegate, LifecycleComponent {
    private val lifecycleCache = LifecycleCache(this)
    private var state: State = formula.initialState(initialInput)
    private var frame: Frame<Input, State, Output>? = null
    private var isValidationEnabled: Boolean = false

    /**
     * Internal accessor for ActionManager.
     * Required by SnapshotImpl to pass to ActionBuilderImpl.
     */
    internal val actionManager: ActionManager = ActionManager(
        manager = this,
        inspector = inspector,
    )

    /**
     * Determines if formula is still attached. Termination is a two step process,
     * first [markAsTerminated] is called to set this boolean which prevents us from
     * start new actions. And then, [performTermination] is called to clean
     * up this [formula] and its child formulas.
     */
    @Volatile
    private var terminated = false

    /**
     * Controls whether listeners can dispatch events. Unlike [isTerminated] which
     * is set early to prevent state changes, this remains true throughout
     * [performTermination] so that termination-triggered events
     * (e.g. [com.instacart.formula.Action.onTerminate]) can still produce their effects.
     * Set to false after all actions and children have been torn down.
     */
    @Volatile
    var isEventHandlingEnabled = true
        private set

    /**
     * Identifier used to track state changes of this [formula] and its children. Whenever
     * there is a state change, this identifier is incremented. This allows us to associate
     * each formula output with an identifier value and compare it for validity with
     * the global value.
     */
    private var globalEvaluationId: Long = 0

    /**
     * Determines if we are executing within [run] block. Enables optimizations
     * such as performing [DeferredTransition] inline.
     */
    private var isRunning: Boolean = false

    /**
     * Pending transition queue which will be populated and executed within [run] function
     * while [isRunning] is true. If [isRunning] is false, we will pass the transitions
     * to [ManagerDelegate].
     */
    private val transitionQueue = LinkedList<DeferredTransition<*, *, *>>()

    fun canUpdatesContinue(evaluationId: Long): Boolean {
        return !isEvaluationNeeded(evaluationId) && transitionQueue.isEmpty()
    }

    fun isEvaluationNeeded(evaluationId: Long): Boolean {
        return globalEvaluationId != evaluationId
    }

    override fun isTerminated(): Boolean {
        return terminated
    }

    fun handleTransitionResult(event: Any?, result: Transition.Result<State>) {
        val effects = result.effects
        if (terminated) {
            // State transitions are ignored, let's just execute side-effects.
            delegate.onPostTransition(effects, false)
            return
        }

        if (result is Transition.Result.Stateful) {
            val old = state
            if (state != result.state) {
                state = result.state

                globalEvaluationId += 1

                inspector?.onStateChanged(
                    formulaType = formulaType,
                    event = event,
                    old = old,
                    new = result.state,
                )
            }
        }

        if (isRunning) {
            delegate.onPostTransition(effects, false)
        } else {
            val lastFrame = checkNotNull(frame) { "Transition cannot happen if frame is null" }
            val evaluationNeeded = isEvaluationNeeded(lastFrame.associatedEvaluationId)
            delegate.onPostTransition(effects, evaluationNeeded)
        }
    }

    override fun setValidationRun(isValidationEnabled: Boolean) {
        this.isValidationEnabled = isValidationEnabled
    }

    /**
     * Within [run], we run through formula [evaluation] and [postEvaluation] until we are idle
     * and can emit the last produced [Output].
     */
    override fun run(input: Input): Output {
        var result: Evaluation<Output>? = null
        isRunning = true
        try {
            var firstRun = true
            while (result == null) {
                val lastFrame = frame
                val evaluationId = globalEvaluationId
                val evaluation = if (firstRun) {
                    firstRun = false
                    evaluation(input, evaluationId)
                } else if (lastFrame == null || isEvaluationNeeded(lastFrame.associatedEvaluationId)) {
                    evaluation(input, evaluationId)
                } else {
                    lastFrame.evaluation
                }

                if (postEvaluation(evaluationId)) {
                    continue
                }

                result = evaluation
            }
            return result.output
        } catch (e: Throwable) {
            if (e is ValidationException) {
                throw e
            } else {
                markAsTerminated()
                performTermination()

                if (e !is CascadingFormulaException) {
                    val error = FormulaError.Unhandled(formulaType, e)
                    onError(error)

                    throw CascadingFormulaException(e)
                } else {
                    throw e
                }
            }
        } finally {
            isRunning = false
        }
    }

    /**
     * Creates the current [Output] and prepares the next frame that will need to be processed.
     */
    private fun evaluation(input: Input, evaluationId: Long): Evaluation<Output> {
        val lastFrame = frame
        if (lastFrame == null && isValidationEnabled) {
            throw ValidationException("Formula should already have run at least once before the validation mode.")
        }

        if (lastFrame == null) {
            inspector?.onFormulaStarted(formulaType)
        }

        if (!isValidationEnabled) {
            inspector?.onEvaluateStarted(formulaType, state)
        }

        if (lastFrame != null) {
            val prevInput = lastFrame.input
            val hasInputChanged = prevInput != input
            if (!isValidationEnabled && lastFrame.associatedEvaluationId == evaluationId && !hasInputChanged) {
                val evaluation = lastFrame.evaluation
                inspector?.onEvaluateFinished(formulaType, evaluation.output, evaluated = false)
                return evaluation
            }

            if (hasInputChanged) {
                if (isValidationEnabled) {
                    throw ValidationException("$formulaType - input changed during identical re-evaluation - old: $prevInput, new: $input")
                }
                state = formula.onInputChanged(prevInput, input, state)
                inspector?.onInputChanged(formulaType, prevInput, input)
            }
        }

        if (isValidationEnabled) actionManager.prepareValidationRun()

        val snapshot = SnapshotImpl(
            input = input,
            state = state,
            lifecycleCache = lifecycleCache,
            delegate = this,
        )
        val result = formula.evaluate(snapshot)

        if (isValidationEnabled) {
            val oldOutput = lastFrame?.evaluation?.output
            if (oldOutput != result.output) {
                throw ValidationException("$formulaType - output changed during identical re-evaluation - old: $oldOutput, new: ${result.output}")
            }
        }

        actionManager.prepareForPostEvaluation()
        lifecycleCache.postEvaluationCleanup()

        val newFrame = Frame(input, state, result, evaluationId)
        this.frame = newFrame
        snapshot.onEvaluationFinished()
        if (!isValidationEnabled) {
            inspector?.onEvaluateFinished(formulaType, newFrame.evaluation.output, evaluated = true)
        }

        return newFrame.evaluation
    }

    fun <ChildInput, ChildOutput> child(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): ChildOutput {
        val manager = findOrInitChildManager(key, formula, input)

        if (manager.isTerminated()) {
            return manager.lastOutput() ?: run {
                /**
                 * This block should never be reached as we should have been terminated as well.
                 */
                throw IllegalStateException("Child formula manager is terminated but has no last output")
            }
        }

        return try {
            manager.setValidationRun(isValidationEnabled)
            manager.run(input)
        } catch (e: ValidationException) {
            throw e
        } catch (e: Throwable) {
            // Use last output if available, otherwise cascade the exception up
            manager.lastOutput() ?: throw e
        }
    }

    fun <ChildInput, ChildOutput> childOrNull(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): ChildOutput? {
        val manager = findOrInitChildManager(key, formula, input)

        if (manager.isTerminated()) {
            // Manager was already terminated in a previous run. No need to run it again.
            return null
        }

        return try {
            manager.setValidationRun(isValidationEnabled)
            manager.run(input)
        } catch (e : ValidationException) {
            throw e
        } catch (e: Throwable) {
            null
        }
    }

    private fun <ChildInput, ChildOutput> findOrInitChildManager(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): FormulaManager<ChildInput, ChildOutput> {
        return lifecycleCache.findOrInit(key, useIndex = true) {
            FormulaManagerImpl(
                delegate = this,
                formula = formula.implementation,
                formulaType = formula.type(),
                initialInput = input,
            )
        }
    }

    override fun markAsTerminated() {
        terminated = true
        lifecycleCache.markAsTerminated()
    }

    override fun performTermination() {
        lifecycleCache.performTermination()
        actionManager.terminate()

        // Execute deferred transitions
        while (transitionQueue.isNotEmpty()) {
            transitionQueue.pollFirst().execute()
        }

        isEventHandlingEnabled = false
        inspector?.onFormulaFinished(formulaType)
    }

    fun onPendingTransition(transition: DeferredTransition<*, *, *>) {
        if (terminated) {
            transition.execute()
        } else if (isRunning) {
            transitionQueue.addLast(transition)
        } else {
            val lastFrame = frame
            if (lastFrame == null || isEvaluationNeeded(lastFrame.associatedEvaluationId)) {
                // Since evaluation is already needed, we can wait for it to happen and
                // then we'll execute the transition.
                transitionQueue.addLast(transition)
            } else {
                transition.execute()
            }
        }
    }

    override fun onPostTransition(effects: List<Effect>, evaluate: Boolean) {
        if (evaluate) {
            globalEvaluationId += 1
        }

        delegate.onPostTransition(effects, evaluate && !isRunning)
    }

    override fun onDetached(scheduler: LifecycleScheduler) {
        markAsTerminated()
        scheduler.scheduleTerminateEffect(this::performTermination)
    }

    override fun onDuplicateKey(log: DuplicateKeyLog, key: Any) {
        if (log.addLog(key)) {
            val error = FormulaError.ChildKeyAlreadyUsed(
                error = ChildAlreadyUsedException(
                    parentType = delegate.formulaType,
                    childType = formulaType,
                    key = key
                )
            )
            onError(error)
        }
    }

    override fun lastOutput(): Output? {
        return frame?.evaluation?.output
    }

    /**
     * Called after [evaluate] to remove detached children, stop detached actions and start
     * new ones. It will start with child formulas first and then perform execution it self.
     *
     * @return True if we need to re-evaluate.
     */
    private fun postEvaluation(evaluationId: Long): Boolean {
        if (isEvaluationNeeded(evaluationId)) return true
        if (handleTransitionQueue(evaluationId)) return true
        if (!terminated && lifecycleCache.terminateDetached(evaluationId)) return true
        if (!terminated && actionManager.terminateOld(evaluationId)) return true
        if (!terminated && actionManager.startNew(evaluationId)) return true
        return false
    }

    /**
     * Iterates through pending deferred transitions.
     *
     * @return True if formula evaluation needs to run again.
     */
    private fun handleTransitionQueue(evaluationId: Long): Boolean {
        while (transitionQueue.isNotEmpty()) {
            val event = transitionQueue.pollFirst()
            event.execute()
            if (isEvaluationNeeded(evaluationId)) {
                return true
            }
        }

        return false
    }

    private fun Formula<Input, State, Output>.evaluate(
        snapshot: Snapshot<Input, State>
    ): Evaluation<Output> {
        return snapshot.run { evaluate() }
    }
}
