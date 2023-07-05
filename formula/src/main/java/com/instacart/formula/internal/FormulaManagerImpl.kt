package com.instacart.formula.internal

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
import com.instacart.formula.Inspector
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import java.util.LinkedList
import kotlin.reflect.KClass

/**
 * Handles formula and its children state processing.
 *
 * Order of processing:
 * 1. Evaluate
 * 2. Disable old event listeners
 * 3. Terminate removed children
 * 4. Prepare parent and alive children for updates.
 */
internal class FormulaManagerImpl<Input, State, Output>(
    private val delegate: ManagerDelegate,
    private val formula: Formula<Input, State, Output>,
    private val type: KClass<*>,
    initialInput: Input,
    private val listeners: Listeners = Listeners(),
    private val inspector: Inspector?,
) : FormulaManager<Input, Output>, ManagerDelegate {

    private var state: State = formula.initialState(initialInput)
    private var frame: Frame<Input, State, Output>? = null
    private var childrenManager: ChildrenManager? = null
    private var isValidationEnabled: Boolean = false

    private val actionManager: ActionManager = ActionManager(
        manager = this,
        formulaType = type,
        inspector = inspector,
    )

    var transitionID: Long = 0
    var terminated = false
    private var isEvaluating: Boolean = false

    private val eventQueue = LinkedList<Event>()

    fun onEvent(event: Event) {
        if (terminated) {
            event.dispatch()
        } else if (isEvaluating) {
            eventQueue.addLast(event)
        } else {
            delegate.onPostponedTransition(event)
        }
    }

    fun hasPendingTransitions(): Boolean {
        return eventQueue.isNotEmpty()
    }

    fun hasTransitioned(id: Long): Boolean {
        return transitionID != id
    }

    fun handleTransitionResult(result: Transition.Result<State>) {
        val effects = result.effects
        if (terminated) {
            // State transitions are ignored, only side effects are passed up to be executed.
            delegate.onTransition(type, result, evaluate = false)
            return
        }

        val frame = this.frame
        if (result is Transition.Result.Stateful) {
            if (state != result.state) {
                state = result.state

                transitionID += 1
            }
        }

        val evaluate = !isEvaluating && frame?.transitionID != transitionID
        if (evaluate || effects != null) {
            delegate.onTransition(type, result, evaluate)
        }
    }

    override fun setValidationRun(isValidationEnabled: Boolean) {
        this.isValidationEnabled = isValidationEnabled
    }

    /**
     * Creates the current [Output] and prepares the next frame that will need to be processed.
     */
    override fun evaluate(input: Input): Evaluation<Output> {
        // TODO: assert main thread.

        var result: Evaluation<Output>? = null
        isEvaluating = true
        var allEvaluationsSkipped = true
        if (frame == null) {
            inspector?.onFormulaStarted(type)
        }

        if (!isValidationEnabled) {
            inspector?.onEvaluateStarted(type, state)
        }

        while (result == null) {
            val lastFrame = frame
            if (lastFrame != null && executeTransitions(lastFrame.transitionID)) {
                continue
            }

            val transitionID = transitionID

            val (evaluation, skipped) = evaluation(input, transitionID)
            allEvaluationsSkipped = allEvaluationsSkipped && skipped
            if (executeTransitions(transitionID)) {
                continue
            }

            if (executeUpdates(transitionID)) {
                continue
            }

            result = evaluation
            if (!isValidationEnabled) {
                inspector?.onEvaluateFinished(type, evaluation.output, evaluated = !allEvaluationsSkipped)
            }
        }
        isEvaluating = false
        return result
    }

    private data class EvaluationResult<Output>(val evaluation: Evaluation<Output>, val skipped: Boolean)

    /**
     * Creates the current [Output] and prepares the next frame that will need to be processed.
     */
    private fun evaluation(input: Input, transitionID: Long): EvaluationResult<Output> {
        // TODO: assert main thread.

        val lastFrame = frame
        if (lastFrame == null && isValidationEnabled) {
            throw ValidationException("Formula should already have run at least once before the validation mode.")
        }

        if (lastFrame != null) {
            val prevInput = lastFrame.input
            val hasInputChanged = prevInput != input
            if (!isValidationEnabled && lastFrame.transitionID == transitionID && !hasInputChanged) {
                val evaluation = lastFrame.evaluation
                return EvaluationResult(evaluation, skipped = true)
            }

            if (hasInputChanged) {
                if (isValidationEnabled) {
                    throw ValidationException("$type - input changed during identical re-evaluation - old: $prevInput, new: $input")
                }
                state = formula.onInputChanged(prevInput, input, state)
                inspector?.onInputChanged(type, prevInput, input)
            }
        }

        val snapshot = SnapshotImpl(input, state, transitionID, listeners, this)
        val result = formula.evaluate(snapshot)

        if (isValidationEnabled) {
            val oldOutput = lastFrame?.evaluation?.output
            if (oldOutput != result.output) {
                throw ValidationException("$type - output changed during identical re-evaluation - old: $oldOutput, new: ${result.output}")
            }

            val lastActionKeys = lastFrame?.evaluation?.actions?.map { it.key }
            val currentActionKeys = result.actions.map { it.key }
            if (lastActionKeys != currentActionKeys) {
                throw ValidationException("$type - action keys changed during identical re-evaluation - old: $lastActionKeys, new: $currentActionKeys")
            }
        }

        val frame = Frame(snapshot, result, transitionID)
        this.frame = frame

        actionManager.onNewFrame(frame.evaluation.actions)
        listeners.evaluationFinished()
        childrenManager?.evaluationFinished()

        snapshot.running = true
        return EvaluationResult(frame.evaluation, skipped = false)
    }

    private fun executeTransitions(transitionID: Long): Boolean {
        if (!hasTransitioned(transitionID)) {
            while (eventQueue.isNotEmpty()) {
                val event = eventQueue.pollFirst()
                event.dispatch()
                if (hasTransitioned(transitionID)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Called after [evaluate] to remove detached children, stop detached actions and start
     * new ones. It will start with child formulas first and then perform execution it self.
     */
    private fun executeUpdates(transitionID: Long): Boolean {
        if (childrenManager?.terminateChildren(transitionID) == true) {
            return true
        }

        if (actionManager.terminateOld(transitionID)) {
            return true
        }

        return actionManager.startNew(transitionID)
    }

    fun <ChildInput, ChildOutput> child(
        key: Any,
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput,
    ): ChildOutput {
        val childrenManager = getOrInitChildrenManager()
        val manager = childrenManager.findOrInitChild(key, formula, input)
        manager.setValidationRun(isValidationEnabled)
        return manager.evaluate(input).output
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

        inspector?.onFormulaFinished(type)
    }

    override fun onTransition(formulaType: KClass<*>, result: Transition.Result<*>, evaluate: Boolean) {
        if (evaluate) {
            transitionID += 1
        }
        delegate.onTransition(formulaType, result, !isEvaluating && evaluate)
    }

    override fun onPostponedTransition(event: Event) {
        if (isEvaluating) {
            eventQueue.addLast(event)
        } else {
            delegate.onPostponedTransition(event)
        }
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
