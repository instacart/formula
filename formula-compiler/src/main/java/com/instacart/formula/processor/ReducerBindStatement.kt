package com.instacart.formula.processor

/**
 * Statement should return a reducer observable. This observable will be added to a list.
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
                "%L.map($reducerVariableName::%L)",
                name,
                name
            )
        }
    }
}
