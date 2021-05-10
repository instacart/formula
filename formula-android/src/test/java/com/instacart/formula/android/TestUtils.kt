package com.instacart.formula.android

import io.reactivex.rxjava3.core.Observable

object TestUtils {
    fun <Value> feature(stateValue: Value): Feature<Value> {
        return Feature(
            state = Observable.just(stateValue),
            viewFactory = ViewFactory.fromLayout(-1) {
                TODO("not needed")
            }
        )
    }
}