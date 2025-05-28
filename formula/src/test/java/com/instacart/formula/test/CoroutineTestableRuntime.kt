package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.toFlow
import com.instacart.formula.plugin.Dispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object CoroutinesTestableRuntime : TestableRuntime {

    override fun <Input : Any, Output : Any, F : IFormula<Input, Output>> test(
        formula: F,
        inspector: Inspector?,
        defaultDispatcher: Dispatcher?,
        isValidationEnabled: Boolean,
    ): TestFormulaObserver<Input, Output, F> {
        val runtimeConfig = RuntimeConfig(
            isValidationEnabled = isValidationEnabled,
            inspector = inspector,
            defaultDispatcher = defaultDispatcher
        )
        val delegate = CoroutineTestDelegate(formula, runtimeConfig)
        return TestFormulaObserver(delegate)
    }

    override fun newRelay(): Relay {
        return FlowRelay()
    }

    override fun <T : Any> emitEvents(events: List<T>): Action<T> {
        return Action.fromFlow {
            flow {
                events.forEach { emit(it) }
            }
        }
    }
}

private class FlowRelay : Relay {
    private val sharedFlow = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun action(): Action<Unit> = Action.fromFlow { sharedFlow }

    override fun triggerEvent() {
        runBlocking { sharedFlow.emit(Unit) }
    }
}
