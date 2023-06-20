package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * Binds [formula] to RxJava3 runtime.
 */
class RxJavaFormulaTestDelegate<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    override val formula: FormulaT,
    private val isValidationEnabled: Boolean = true,
) : FormulaTestDelegate<Input, Output, FormulaT> {
    private val inputRelay = BehaviorSubject.create<Input>()
    private val observer = formula
        .toObservable(inputRelay, isValidationEnabled = isValidationEnabled)
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