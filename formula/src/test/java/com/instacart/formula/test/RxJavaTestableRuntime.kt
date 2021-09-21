package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.rxjava3.ObservableFormula
import com.instacart.formula.rxjava3.RxStream
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

object RxJavaTestableRuntime : TestableRuntime {
    override val rule: TestRule = RuleChain.emptyRuleChain()

    override fun <Input : Any, Output : Any, F : IFormula<Input, Output>> test(
        formula: F
    ): TestFormulaObserver<Input, Output, F> {
        return TestFormulaObserver(RxJavaFormulaTestDelegate(formula))
    }

    override fun newIncrementRelay(): IncrementRelay {
        return RxIncrementRelay()
    }

    override fun streamFormula(): StreamFormulaSubject {
        return ObservableStreamFormulaSubject()
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

private class RxIncrementRelay : IncrementRelay {
    private val relay = PublishRelay.create<Unit>()

    override fun stream() = RxStream.fromObservable { relay }

    override fun triggerIncrement() = relay.accept(Unit)
}

