package com.instacart.formula.internal

import com.instacart.formula.Effects
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
import com.instacart.formula.Inspector
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import java.util.LinkedList
import kotlin.reflect.KClass

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
    initialInput: Input,
    private val loggingType: KClass<*>,
    private val listeners: Listeners = Listeners(),
    private val inspector: Inspector?,
) : FormulaManager<Input, Output>, ManagerDelegate {

    private var state: State = formula.initialState(initialInput)
    private var frame: Frame<Input, State, Output>? = null
    private var childrenManager: ChildrenManager? = null
    private var isValidationEnabled: Boolean = false

    private val actionManager: ActionManager = ActionManager(
        manager = this,
        loggingType = loggingType,
        inspector = inspector,
    )

    /**
     * Determines if formula is still attached. Termination is a two step process,
     * first [markAsTerminated] is called to set this boolean which prevents use from
     * start new actions. And then, [performTerminationSideEffects] is called to clean
     * up this [formula] and its child formulas.
     */
    private var terminated = false

    /**
     * Identifier used to track state changes of this [formula] and its children. Whenever
     * there is a state change, this identifier is incremented. This allows us to associate
     * each formula output with an identifier value and compare it for validity with
     * the global value.
     */
    var globalEvaluationId: Long = 0

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

    fun isTerminated(): Boolean {
        return terminated
    }

    fun handleTransitionResult(result: Transition.Result<State>) {
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

                inspector?.onStateChanged(loggingType, old, result.state)
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
    override fun run(input: Input): Evaluation<Output> {
        // TODO: assert main thread.

        var result: Evaluation<Output>? = null
        isRunning = true
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
        isRunning = false

        return result
    }

    /**
     * Creates the current [Output] and prepares the next frame that will need to be processed.
     */
    private fun evaluation(input: Input, evaluationId: Long): Evaluation<Output> {
        // TODO: assert main thread.

        val lastFrame = frame
        if (lastFrame == null && isValidationEnabled) {
            throw ValidationException("Formula should already have run at least once before the validation mode.")
        }

        if (lastFrame == null) {
            inspector?.onFormulaStarted(loggingType)
        }

        if (!isValidationEnabled) {
            inspector?.onEvaluateStarted(loggingType, state)
        }

        if (lastFrame != null) {
            val prevInput = lastFrame.input
            val hasInputChanged = prevInput != input
            if (!isValidationEnabled && lastFrame.associatedEvaluationId == evaluationId && !hasInputChanged) {
                val evaluation = lastFrame.evaluation
                inspector?.onEvaluateFinished(loggingType, evaluation.output, evaluated = false)
                return evaluation
            }

            if (hasInputChanged) {
                if (isValidationEnabled) {
                    throw ValidationException("$loggingType - input changed during identical re-evaluation - old: $prevInput, new: $input")
                }
                state = formula.onInputChanged(prevInput, input, state)
                inspector?.onInputChanged(loggingType, prevInput, input)
            }
        }

        val snapshot = SnapshotImpl(input, state, evaluationId, listeners, this)
        val result = formula.evaluate(snapshot)

        if (isValidationEnabled) {
            val oldOutput = lastFrame?.evaluation?.output
            if (oldOutput != result.output) {
                throw ValidationException("$loggingType - output changed during identical re-evaluation - old: $oldOutput, new: ${result.output}")
            }

            val lastActionKeys = lastFrame?.evaluation?.actions?.map { it.key }
            val currentActionKeys = result.actions.map { it.key }
            if (lastActionKeys != currentActionKeys) {
                throw ValidationException("$loggingType - action keys changed during identical re-evaluation - old: $lastActionKeys, new: $currentActionKeys")
            }
        }

        val newFrame = Frame(input, state, result, evaluationId)
        this.frame = newFrame

        actionManager.prepareForPostEvaluation(newFrame.evaluation.actions)
        listeners.prepareForPostEvaluation()
        childrenManager?.prepareForPostEvaluation()

        snapshot.running = true
        if (!isValidationEnabled) {
            inspector?.onEvaluateFinished(loggingType, newFrame.evaluation.output, evaluated = true)
        }

        return newFrame.evaluation
    }

    fun <ChildInput, ChildOutput> child(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): ChildOutput {
        val childrenManager = getOrInitChildrenManager()
        val manager = childrenManager.findOrInitChild(key, formula, input)

        // If termination happens while running, we might still be initializing child formulas. To
        // ensure correct behavior, we mark each requested child manager as terminated to avoid
        // starting new actions.
        if (isTerminated()) {
            manager.markAsTerminated()
        }
        manager.setValidationRun(isValidationEnabled)
        return manager.run(input).output
    }

    override fun markAsTerminated() {
        terminated = true
        childrenManager?.markAsTerminated()
    }

    override fun performTerminationSideEffects() {
        childrenManager?.performTerminationSideEffects()
        actionManager.terminate()

        // Execute deferred transitions
        for (transition in transitionQueue) {
            transition.execute()
        }

        listeners.disableAll()
        inspector?.onFormulaFinished(loggingType)
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

    override fun onPostTransition(effects: Effects?, evaluate: Boolean) {
        if (evaluate) {
            globalEvaluationId += 1
        }

        delegate.onPostTransition(effects, evaluate && !isRunning)
    }

    /**
     * Called after [evaluate] to remove detached children, stop detached actions and start
     * new ones. It will start with child formulas first and then perform execution it self.
     *
     * @return True if we need to re-evaluate.
     */
    private fun postEvaluation(evaluationId: Long): Boolean {
        if (handleTransitionQueue(evaluationId)) {
            return true
        }

        if (!terminated && childrenManager?.terminateChildren(evaluationId) == true) {
            return true
        }

        if (!terminated && actionManager.terminateOld(evaluationId)) {
            return true
        }

        if (!terminated && actionManager.startNew(evaluationId)) {
            return true
        }

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

    private fun getOrInitChildrenManager(): ChildrenManager {
        return childrenManager ?: run {
            val value = ChildrenManager(this, inspector)
            childrenManager = value
            value
        }
    }

    private fun Formula<Input, State, Output>.evaluate(
        snapshot: Snapshot<Input, State>
    ): Evaluation<Output> {
        return snapshot.run { evaluate() }
    }
}
