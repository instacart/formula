package com.instacart.formula.android

import com.instacart.formula.android.fakes.NoOpViewFactory
import io.reactivex.rxjava3.core.Observable

object TestUtils {
    fun <Value : Any> feature(stateValue: Value): Feature {
        return Feature(
            state = Observable.just(stateValue),
            viewFactory = NoOpViewFactory()
        )
    }
}