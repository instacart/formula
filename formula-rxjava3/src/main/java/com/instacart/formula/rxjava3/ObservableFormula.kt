package com.instacart.formula.rxjava3

import com.instacart.formula.Action
import com.instacart.formula.ActionFormula
import io.reactivex.rxjava3.core.Observable

/**
 * Formula that emits [initialValue] and then subsequent values returned by [observable].
 */
abstract class ObservableFormula<Input : Any, Output : Any> : ActionFormula<Input, Output>() {

    abstract override fun initialValue(input: Input): Output

    abstract fun observable(input: Input): Observable<Output>

    final override fun action(input: Input): Action<Output> {
        return RxAction.fromObservable {
            observable(input)
        }
    }
}
