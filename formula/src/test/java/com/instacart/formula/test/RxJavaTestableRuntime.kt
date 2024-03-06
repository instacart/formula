package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.rxjava3.ObservableFormula
import com.instacart.formula.rxjava3.RxAction
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

object RxJavaTestableRuntime : TestableRuntime {
    override val rule: TestRule = RuleChain.emptyRuleChain()

    override fun <Input : Any, Output : Any, F : IFormula<Input, Output>> test(
        formula: F,
        inspector: Inspector?,
        defaultDispatcher: Dispatcher?,
    ): TestFormulaObserver<Input, Output, F> {
        val delegate = RxJavaFormulaTestDelegate(
            formula = formula,
            inspector = inspector,
            dispatcher = defaultDispatcher,
        )
        return TestFormulaObserver(delegate)
    }

    override fun newRelay(): Relay {
        return RxRelay()
    }

    override fun streamFormula(): StreamFormulaSubject {
        return ObservableStreamFormulaSubject()
    }

    override fun <T : Any> emitEvents(events: List<T>): Action<T> {
        return RxAction.fromObservable {
            Observable.fromIterable(events)
        }
    }

    override fun <T : Any> emitEvents(key: Any?, events: List<T>): Action<T> {
        return RxAction.fromObservable(key) {
            Observable.fromIterable(events)
        }
    }
}

private class ObservableStreamFormulaSubject : ObservableFormula<String, Int>(), StreamFormulaSubject {
    private val relay: PublishRelay<Int> = PublishRelay.create()

    override fun emitEvent(event: Int) {
        relay.accept(event)
    }

    override fun initialValue(input: String): Int = 0

    override fun observable(input: String): Observable<Int> {
        return relay
    }
}

private class RxRelay : Relay {
    private val relay = PublishRelay.create<Unit>()

    override fun action() = RxAction.fromObservable { relay }

    override fun triggerEvent() = relay.accept(Unit)
}

