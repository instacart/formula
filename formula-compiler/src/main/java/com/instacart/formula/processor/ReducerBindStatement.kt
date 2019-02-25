package com.instacart.formula.processor

import io.reactivex.BackpressureStrategy

/**
 * Statement should return a reducer flowable. This flowable will be added to a list.
 */
class ReducerBindStatement(val format: String, vararg val args: Any?) {
    companion object {
        fun constructorStream(name: String, reducerVariableName: String): ReducerBindStatement {
            return ReducerBindStatement(
                "%L.map($reducerVariableName::%L)",
                name,
                name
            )
        }

        fun localRelay(name: String, reducerVariableName: String): ReducerBindStatement {
            return ReducerBindStatement(
                "%L.toFlowable(%T.LATEST).map($reducerVariableName::%L)",
                name,
                BackpressureStrategy::class.java,
                name
            )
        }
    }
}
