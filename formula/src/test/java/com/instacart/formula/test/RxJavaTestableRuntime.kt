package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula
import com.instacart.formula.rxjava3.ObservableFormula
import com.instacart.formula.rxjava3.RxAction
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement

object RxJavaTestableRuntime : TestableRuntime {
    override val rule: TestRule = RxJavaTestRule()

    override fun <Input : Any, Output : Any, F : IFormula<Input, Output>> test(
        formula: F
    ): TestFormulaObserver<Input, Output, F> {
        return TestFormulaObserver(RxJavaFormulaTestDelegate(formula))
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

    override fun <T : Any> errorAction(error: Throwable): Action<T> {
        return RxAction.fromObservable {
            Observable.error(error)
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

 class RxJavaTestRule : TestWatcher() {
    private var errors = mutableListOf<Throwable>()
    override fun starting(description: Description) {
        errors.clear()
        RxJavaPlugins.reset()
        RxJavaPlugins.setErrorHandler { errors.add(it) }
        super.starting(description)
    }

    override fun apply(base: Statement, description: Description): Statement {
        val statement = super.apply(base, description)
        return object : Statement() {
            override fun evaluate() {
                statement.evaluate()

                if (errors.isNotEmpty()) {
                    throw AssertionError("RxJava should not have any uncaught exceptions: $errors")
                }
            }
        }
    }

    override fun finished(description: Description) {
        RxJavaPlugins.reset()
        super.finished(description)
    }
}

