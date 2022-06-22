package com.instacart.formula.internal

sealed class Try<out T> {

    data class Error(val throwable: Throwable): Try<Nothing>() {
        override fun errorOrNull(): Throwable = throwable
    }

    data class Result<out T>(val data: T): Try<T>() {
        override fun errorOrNull(): Throwable? = null
    }

    companion object {

        operator fun <T> invoke(function: () -> T): Try<T> {
            return try {
                Result(function())
            } catch (e: Throwable) {
                Error(e)
            }
        }
    }

    abstract fun errorOrNull(): Throwable?
}
