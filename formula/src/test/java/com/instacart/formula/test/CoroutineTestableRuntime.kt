package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.coroutines.CoroutineAction
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.coroutines.FlowFormula
import com.instacart.formula.coroutines.toFlow
import com.instacart.formula.plugin.Dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.withContext
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description


object CoroutinesTestableRuntime : TestableRuntime {
    private val coroutineTestRule = CoroutineTestRule()

    override val rule: TestRule = coroutineTestRule

    override fun <Input : Any, Output : Any, F : IFormula<Input, Output>> test(
        formula: F,
        inspector: Inspector?,
        defaultDispatcher: Dispatcher?,
        isValidationEnabled: Boolean,
    ): TestFormulaObserver<Input, Output, F> {
        val scope = coroutineTestRule.testCoroutineScope
        val runtimeConfig = RuntimeConfig(
            isValidationEnabled = isValidationEnabled,
            inspector = inspector,
            defaultDispatcher = defaultDispatcher
        )
        val delegate = CoroutineTestDelegate(scope, formula, runtimeConfig)
        return TestFormulaObserver(delegate)
    }

    override fun newRelay(): Relay {
        return FlowRelay()
    }

    override fun streamFormula(): StreamFormulaSubject {
        return FlowStreamFormulaSubject()
    }

    override fun <T : Any> emitEvents(events: List<T>): Action<T> {
        return CoroutineAction.fromFlow {
            toFlow(events)
        }
    }

    override fun <T : Any> emitEvents(key: Any?, events: List<T>): Action<T> {
        return CoroutineAction.fromFlow(key = key) {
            toFlow(events)
        }
    }

    private fun <T> toFlow(events: List<T>) = flow {
        events.forEach { emit(it) }
    }
}

private class FlowRelay : Relay {
    private val sharedFlow = MutableSharedFlow<Unit>(0, 0)

    override fun action(): Action<Unit> = CoroutineAction.fromFlow { sharedFlow }

    @OptIn(DelicateCoroutinesApi::class)
    override fun triggerEvent() {
        GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(Dispatchers.Unconfined) {
                sharedFlow.emit(Unit)
            }
        }
    }
}

private class FlowStreamFormulaSubject : FlowFormula<String, Int>(), StreamFormulaSubject {
    private val sharedFlow = MutableSharedFlow<Int>(0, extraBufferCapacity = 0)

    @OptIn(DelicateCoroutinesApi::class)
    override fun emitEvent(event: Int) {
        GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(Dispatchers.Unconfined) {
                sharedFlow.emit(event)
            }
        }
    }

    override fun initialValue(input: String): Int = 0

    override fun flow(input: String): Flow<Int> {
        return sharedFlow
    }
}

private class CoroutineTestDelegate<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    private val scope: CoroutineScope,
    override val formula: FormulaT,
    private val runtimeConfig: RuntimeConfig,
): FormulaTestDelegate<Input, Output, FormulaT> {
    private val values = mutableListOf<Output>()
    private val errors = mutableListOf<Throwable>()

    private val inputFlow = MutableSharedFlow<Input>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val formulaFlow = formula.toFlow(inputFlow, runtimeConfig)
        .onEach { values.add(it) }
        .catch { errors.add(it) }

    private val job = formulaFlow.launchIn(scope)

    override fun values(): List<Output> {
        return values
    }

    override fun input(input: Input) {
        inputFlow.tryEmit(input)
    }

    override fun assertNoErrors() {
        if (errors.isNotEmpty()) {
            throw AssertionError("There are ${errors.size} errors", errors.last())
        }
    }

    override fun dispose() {
        job.cancel()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class CoroutineTestRule(
    val testCoroutineScope: TestCoroutineScope = TestCoroutineScope(TestCoroutineDispatcher())
) : TestWatcher() {

    override fun finished(description: Description) {
        super.finished(description)
        testCoroutineScope.cleanupTestCoroutines()
    }
}
