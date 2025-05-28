package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.rxjava3.ObservableFormula
import com.instacart.formula.rxjava3.RxAction
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

object RxJavaTestableRuntime : TestableRuntime {

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
        val delegate = RxJavaFormulaTestDelegate(formula, runtimeConfig)
        return TestFormulaObserver(delegate)
    }

    override fun newRelay(): Relay {
        return RxRelay()
    }

    override fun <T : Any> emitEvents(events: List<T>): Action<T> {
        return RxAction.fromObservable {
            Observable.fromIterable(events)
        }
    }
}

private class RxRelay : Relay {
    private val relay = PublishRelay.create<Unit>()

    override fun action() = RxAction.fromObservable { relay }

    override fun triggerEvent() = relay.accept(Unit)
}

