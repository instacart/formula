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

    var transitionID: Long = 0
    var terminated = false

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

    /**
     * A queue for transition [Effects]. It will be executed within [run] block, after
     * evaluation and action updates.
     */
    private val transitionEffectQueue = LinkedList<Effects>()

    fun canUpdatesContinue(id: Long): Boolean {
        return !isEvaluationNeeded(id) && transitionQueue.isEmpty()
    }

    fun isEvaluationNeeded(id: Long): Boolean {
        return transitionID != id
    }

    fun isTerminated(): Boolean {
        return terminated
    }

    fun handleTransitionResult(result: Transition.Result<State>) {
        val frame = checkNotNull(frame) {
            "Transition cannot happen if frame is null"
        }

        val effects = result.effects
        if (terminated) {
            // State transitions are ignored, let's just execute side-effects.
            effects?.execute()
            return
        }

        if (result is Transition.Result.Stateful) {
            val old = state
            if (state != result.state) {
                state = result.state

                transitionID += 1

                inspector?.onStateChanged(loggingType, old, result.state)
            }
        }

        if (isRunning) {
            if (effects != null) {
                transitionEffectQueue.addLast(effects)
            }
        } else {
            if (isEvaluationNeeded(frame.transitionID)) {
                if (effects != null) {
                    transitionEffectQueue.addLast(effects)
                }
                delegate.requestEvaluation()
            } else {
                effects?.execute()
            }
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
            val transitionID = transitionID
            val evaluation = if (firstRun) {
                firstRun = false
                evaluation(input, transitionID)
            } else if (lastFrame == null || isEvaluationNeeded(lastFrame.transitionID)) {
                evaluation(input, transitionID)
            } else {
                lastFrame.evaluation
            }

            if (postEvaluation(transitionID)) {
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
    private fun evaluation(input: Input, transitionID: Long): Evaluation<Output> {
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
            if (!isValidationEnabled && lastFrame.transitionID == transitionID && !hasInputChanged) {
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

        val snapshot = SnapshotImpl(input, state, transitionID, listeners, this)
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

        val frame = Frame(snapshot, result, transitionID)
        this.frame = frame

        actionManager.onNewFrame(frame.evaluation.actions)
        listeners.evaluationFinished()
        childrenManager?.evaluationFinished()

        snapshot.running = true
        if (!isValidationEnabled) {
            inspector?.onEvaluateFinished(loggingType, frame.evaluation.output, evaluated = true)
        }

        return frame.evaluation
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
        frame?.snapshot?.terminated = true
        childrenManager?.markAsTerminated()
    }

    override fun performTerminationSideEffects() {
        childrenManager?.performTerminationSideEffects()
        actionManager.terminate()
        listeners.disableAll()

        inspector?.onFormulaFinished(loggingType)
    }

    override fun onPendingTransition(transition: DeferredTransition<*, *, *>) {
        if (terminated) {
            transition.execute()
        } else if (isRunning) {
            transitionQueue.addLast(transition)
        } else {
            delegate.onPendingTransition(transition)
        }
    }

    override fun requestEvaluation() {
        transitionID += 1

        if (!isRunning) {
            delegate.requestEvaluation()
        }
    }

    /**
     * Called after [evaluate] to remove detached children, stop detached actions and start
     * new ones. It will start with child formulas first and then perform execution it self.
     *
     * @return True if we need to re-evaluate.
     */
    private fun postEvaluation(transitionID: Long): Boolean {
        if (handleTransitionQueue(transitionID)) {
            return true
        }

        if (!terminated && childrenManager?.terminateChildren(transitionID) == true) {
            return true
        }

        if (!terminated && actionManager.terminateOld(transitionID)) {
            return true
        }

        if (!terminated && actionManager.startNew(transitionID)) {
            return true
        }

        return handleSideEffectQueue(transitionID)
    }

    /**
     * Iterates through pending deferred transitions.
     *
     * @return True if formula evaluation needs to run again.
     */
    private fun handleTransitionQueue(transitionID: Long): Boolean {
        while (transitionQueue.isNotEmpty()) {
            val event = transitionQueue.pollFirst()
            event.execute()
            if (isEvaluationNeeded(transitionID)) {
                return true
            }
        }

        return false
    }

    /**
     * Iterates through pending transition side-effects.
     *
     * @return True if formula evaluation needs to run again.
     */
    private fun handleSideEffectQueue(transitionID: Long): Boolean {
        while (transitionEffectQueue.isNotEmpty()) {
            val effect = transitionEffectQueue.pollFirst()
            effect.execute()
            if (!canUpdatesContinue(transitionID)) {
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
