package com.instacart.client.mvi.processor

import io.reactivex.BackpressureStrategy

/**
 * Statement should return a reducer flowable. This flowable will be added to a list.
 */
class ICReducerBindStatement(val format: String, vararg val args: Any?) {
    companion object {
        fun constructorStream(name: String, reducerVariableName: String): ICReducerBindStatement {
            return ICReducerBindStatement(
                "%L.map($reducerVariableName::%L)",
                name,
                name
            )
        }

        fun localRelay(name: String, reducerVariableName: String): ICReducerBindStatement {
            return ICReducerBindStatement(
                "%L.toFlowable(%T.LATEST).map($reducerVariableName::%L)",
                name,
                BackpressureStrategy::class.java,
                name
            )
        }
    }
}
