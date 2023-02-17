package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.rxjava3.RxJavaRuntime
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Binds [formula] to RxJava3 runtime.
 */
class RxJavaFormulaTestDelegate<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    override val formula: FormulaT
) : FormulaTestDelegate<Input, Output, FormulaT> {
    val testScope = TestScope(TestCoroutineDispatcher())
    @OptIn(ExperimentalStdlibApi::class)
    private val dispatcher = testScope.coroutineContext[CoroutineDispatcher]!!
    private val inputRelay = BehaviorSubject.create<Input>()
    private val observer = RxJavaRuntime
        .start(formula, inputRelay, CoroutineScope(dispatcher))
        .test()

    override fun values(): List<Output> {
        return observer.values()
    }

    override fun assertNoErrors() {
        observer.assertNoErrors()
    }

    override fun dispose() {
        observer.dispose()
    }

    override fun input(input: Input) {
        inputRelay.onNext(input)
    }
}