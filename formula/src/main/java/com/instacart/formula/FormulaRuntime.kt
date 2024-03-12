package com.instacart.formula

import com.instacart.formula.batch.BatchManager
import com.instacart.formula.internal.FormulaManager
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.ManagerDelegate
import com.instacart.formula.internal.SynchronizedUpdateQueue
import com.instacart.formula.plugin.Dispatcher
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Takes a [Formula] and creates an Observable<Output> from it.
 */
class FormulaRuntime<Input : Any, Output : Any>(
    private val formula: IFormula<Input, Output>,
    private val onOutput: (Output) -> Unit,
    private val onError: (Throwable) -> Unit,
    config: RuntimeConfig?,
) : ManagerDelegate {
    private val isValidationEnabled = config?.isValidationEnabled ?: false
    private val inspector = FormulaPlugins.inspector(
        type = formula.type(),
        local = config?.inspector,
    )
    private val defaultDispatcher: Dispatcher = config?.defaultDispatcher ?: FormulaPlugins.defaultDispatcher()
    private val implementation = formula.implementation()
    private val synchronizedUpdateQueue = SynchronizedUpdateQueue(
        onEmpty = { emitOutputIfNeeded() }
    )

    /**
     * Global batched event manager.
     */
    private val batchManager = BatchManager(this)

    @Volatile
    private var manager: FormulaManagerImpl<Input, *, Output>? = null

    private var input: Input? = null
    private var key: Any? = null

    /**
     * Used by [executeBatch] to disable running while we queue up batched updates.
     */
    private var isRunEnabled: Boolean = true

    /**
     * Used when [isRunEnabled] is disabled to track if evaluate is needed when [isRunEnabled]
     * is re-enabled.
     */
    private var pendingEvaluation: Boolean = false

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

    /**
     * Global transition effect queue which executes side-effects after all formulas are idle.
     */
    private var globalEffectQueue = LinkedList<Effect>()

    /**
     * Determines if we are iterating through [globalEffectQueue]. It prevents us from
     * entering executeTransitionEffects block when we are already within it.
     */
    private var isExecutingEffects: Boolean = false

    /**
     * This is a global termination flag that indicates that upstream has disposed of the
     * this [FormulaRuntime] instance. We will not accept any more [onInput] changes and will
     * not emit any new [Output] events.
     */
    @Volatile
    private var isRuntimeTerminated: Boolean = false

    /**
     * Pending output to be emitted to the formula subscriber.
     */
    private var pendingOutput = AtomicReference<Output>()
    private var isEmitting = AtomicBoolean(false)

    private fun isKeyValid(input: Input): Boolean {
        return this.input == null || key == formula.key(input)
    }

    fun onInput(input: Input) {
        synchronizedUpdateQueue.postUpdate { onInputInternal(input) }
    }

    private fun onInputInternal(input: Input) {
        if (isRuntimeTerminated) return

        val isKeyValid = isKeyValid(input)

        this.input = input
        this.key = formula.key(input)

        val current = manager
        if (current == null) {
            // First input arrived, need to start a formula manager
            startNewManager(input)
        } else if (!isKeyValid) {
            // Formula key changed, need to reset the formula state. We mark old manager as
            // terminated, will perform termination effects and then start a new manager.
            current.markAsTerminated()

            // Input changed, increment the id
            inputId += 1

            if (isRunning) {
                // Since we are already running, we let that function to take over.
                // No need to do anything more here
            } else {
                // Let's first execute side-effects
                current.performTerminationSideEffects()

                // Start new manager
                startNewManager(input)
            }
        } else {
            // Input changed, need to re-run
            inputId += 1
            run()
        }
    }

    fun terminate() {
        synchronizedUpdateQueue.postUpdate(this::terminateInternal)
    }

    private fun terminateInternal() {
        if (isRuntimeTerminated) return
        isRuntimeTerminated = true

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
                terminateManager(this)
            }
        }
    }

    override fun onPostTransition(effects: List<Effect>, evaluate: Boolean) {
        if (effects.isNotEmpty()) {
            // Using indices instead of allocating an iterator.
            for (index in effects.indices) {
                globalEffectQueue.addLast(effects[index])
            }
        }

        if (effects.isNotEmpty() || evaluate) {
            if (isRunEnabled) {
                run(evaluate = evaluate)
            } else {
                pendingEvaluation = pendingEvaluation || evaluate
            }
        }
    }

    fun executeBatch(batchExecutable: () -> Unit) {
        // Using default dispatcher for batch execution
        defaultDispatcher.dispatch {
            synchronizedUpdateQueue.postUpdate {
                isRunEnabled = false
                batchExecutable()
                isRunEnabled = true

                if (globalEffectQueue.isNotEmpty() || pendingEvaluation) {
                    val evaluate = pendingEvaluation
                    pendingEvaluation = false
                    run(evaluate = evaluate)
                }
            }
        }
    }

    /**
     * Performs the evaluation and execution phases.
     */
    private fun run(evaluate: Boolean = true) {
        if (isRunning) return

        try {
            val manager = checkNotNull(manager)

            if (evaluate) {
                var shouldRun = true
                while (shouldRun) {
                    val localInputId = inputId
                    if (!manager.isTerminated()) {
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
                        if (manager.isTerminated()) {
                            shouldRun = false
                            terminateManager(manager)

                            // If runtime has been terminated, we are stopping and do
                            // not need to do anything else.
                            if (!isRuntimeTerminated) {
                                // Terminated manager with input change indicates that formula
                                // key changed and we are resetting formula state. We need to
                                // start a new formula manager.
                                if (localInputId != inputId) {
                                    input?.let(this::startNewManager)
                                }
                            }
                        } else {
                            shouldRun = localInputId != inputId
                        }
                    } else {
                        shouldRun = false
                    }
                }
            }

            if (isExecutingEffects) return
            executeTransitionEffects()

        } catch (e: Throwable) {
            isRunning = false

            manager?.markAsTerminated()
            onError(e)
            manager?.let(this::terminateManager)
        }
    }

    /**
     * Runs formula evaluation.
     */
    private fun runFormula(manager: FormulaManager<Input, Output>, currentInput: Input) {
        val result = manager.run(currentInput)
        pendingOutput.set(result.output)

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
     * Iterates through and executes pending transition side-effects. It will keep going until the
     * whole queue is empty. If any transition happens due to an executed effect:
     * - If state change happens, [runFormula] will run before next effect is executed
     * - New transition effects will be added to [globalEffectQueue] which will be picked up within
     * this loop
     */
    private fun executeTransitionEffects() {
        isExecutingEffects = true
        while (globalEffectQueue.isNotEmpty()) {
            val effect = globalEffectQueue.pollFirst()
            val dispatcher = when (effect.type) {
                Effect.Unconfined -> Dispatcher.None
                Effect.Main -> Dispatcher.Main
                Effect.Background -> Dispatcher.Background
            }
            dispatcher.dispatch(effect.executable)
        }
        isExecutingEffects = false
    }

    /**
     * Emits output to the formula subscriber.
     */
    private fun emitOutputIfNeeded() {
        var output = pendingOutput.get()
        while (output != null) {
            if (isEmitting.compareAndSet(false, true)) {
                if (!isRuntimeTerminated && manager?.isTerminated() != true) {
                    onOutput(output)
                }

                // If it is still our output, we try to clear it
                pendingOutput.compareAndSet(output, null)

                // Allow others to take on the processing
                isEmitting.set(false)

                // Check if there is another update and try to process if no-one else has taken over
                output = pendingOutput.get()
            } else {
                // Someone else is emitting the value
                output = null
            }
        }
    }

    /**
     * Creates a new formula manager and runs it.
     */
    private fun startNewManager(initialInput: Input) {
        manager = initManager(initialInput)
        run()
    }

    /**
     * Performs formula termination effects and executes transition effects if needed.
     */
    private fun terminateManager(manager: FormulaManager<Input, Output>) {
        manager.performTerminationSideEffects()
        if (!isExecutingEffects) {
            executeTransitionEffects()
        }
    }

    private fun initManager(initialInput: Input): FormulaManagerImpl<Input, *, Output> {
        return FormulaManagerImpl(
            queue = synchronizedUpdateQueue,
            batchManager = batchManager,
            delegate = this,
            formula = implementation,
            initialInput = initialInput,
            loggingType = formula::class,
            inspector = inspector,
            defaultDispatcher = defaultDispatcher,
        )
    }
}
