package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula
import com.instacart.formula.Inspector
import com.instacart.formula.coroutines.FlowAction
import com.instacart.formula.coroutines.FlowFormula
import com.instacart.formula.coroutines.toFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement


object CoroutinesTestableRuntime : TestableRuntime {
    private val coroutineTestRule = CoroutineTestRule()

    override val rule: TestRule = coroutineTestRule

    override fun <Input : Any, Output : Any, F : IFormula<Input, Output>> test(
        formula: F,
        inspector: Inspector?,
    ): TestFormulaObserver<Input, Output, F> {
        val scope = coroutineTestRule.testCoroutineScope
        val delegate = CoroutineTestDelegate(scope, formula, inspector)
        return TestFormulaObserver(delegate)
    }

    override fun newRelay(): Relay {
        return FlowRelay()
    }

    override fun streamFormula(): StreamFormulaSubject {
        return FlowStreamFormulaSubject()
    }

    override fun <T : Any> emitEvents(events: List<T>): Action<T> {
        return FlowAction.fromFlow {
            toFlow(events)
        }
    }

    override fun <T : Any> emitEvents(key: Any?, events: List<T>): Action<T> {
        return FlowAction.fromFlow(key = key) {
            toFlow(events)
        }
    }

    private fun <T> toFlow(events: List<T>) = flow {
        events.forEach { emit(it) }
    }
}

private class FlowRelay : Relay {
    private val sharedFlow = MutableSharedFlow<Unit>(0, 1)

    override fun action(): Action<Unit> = FlowAction.fromFlow { sharedFlow }

    override fun triggerEvent() {
        sharedFlow.tryEmit(Unit)
    }
}

private class FlowStreamFormulaSubject : FlowFormula<String, Int>(), StreamFormulaSubject {
    private val sharedFlow = MutableSharedFlow<Int>(0, extraBufferCapacity = 1, BufferOverflow.DROP_OLDEST)

    override fun emitEvent(event: Int) {
        sharedFlow.tryEmit(event)
    }

    override fun initialValue(input: String): Int = 0

    override fun flow(input: String): Flow<Int> {
        return sharedFlow
    }
}

private class CoroutineTestDelegate<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    private val scope: CoroutineScope,
    override val formula: FormulaT,
    private val inspector: Inspector?,
): FormulaTestDelegate<Input, Output, FormulaT> {
    private val values = mutableListOf<Output>()
    private val errors = mutableListOf<Throwable>()

    private val inputFlow = MutableSharedFlow<Input>(1)
    private val formulaFlow = formula.toFlow(inputFlow, isValidationEnabled = true, inspector = inspector)
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

@OptIn(ExperimentalStdlibApi::class)
private class CoroutineTestRule(
    val testCoroutineScope: TestCoroutineScope = TestCoroutineScope(TestCoroutineDispatcher())
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.resetMain()
        Dispatchers.setMain(testCoroutineScope.coroutineContext[CoroutineDispatcher]!!)
        super.starting(description)
    }

    override fun apply(base: Statement, description: Description): Statement {
        var result: Statement? = null
        testCoroutineScope.runBlockingTest {
            result = super.apply(base, description)
        }
        return result!!
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        testCoroutineScope.cleanupTestCoroutines()
    }
}
