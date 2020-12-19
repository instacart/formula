package com.instacart.formula.rxjava3

import com.instacart.formula.Stream
import com.instacart.formula.StreamFormula
import io.reactivex.rxjava3.core.Observable

/**
 * Formula that emits [initialValue] and then subsequent values returned by [observable].
 */
abstract class ObservableFormula<Input : Any, Output : Any> : StreamFormula<Input, Output>() {

    abstract override fun initialValue(input: Input): Output

    abstract fun observable(input: Input): Observable<Output>

    final override fun stream(input: Input): Stream<Output> {
        return RxStream.fromObservable {
            observable(input)
        }
    }
}
