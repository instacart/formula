package com.instacart.formula.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn

class TestFlow<T>(private val upstream: Flow<T>) : Flow<T> by upstream {
    private val values = mutableListOf<T>()
    private val errors = mutableListOf<Throwable>()

    fun values(): List<T> = values.toList()

    fun assertNoErrors() = run {
        check(errors.isEmpty()) { "There are ${errors.size} errors" }
        this
    }

    private val internalFlow  by lazy {
        callbackFlow {
            upstream
                .catch {
                    errors.add(it)
                }
                .collect {
                    values.add(it)
                    trySend(it)
                }

            awaitClose {
                cancel()
            }
        }
    }
    fun test() = internalFlow

}

fun <T> Flow<T>.test(scope: CoroutineScope) : TestFlow<T> {
    val testFlow = TestFlow(this)
    testFlow.test().launchIn(scope)
    return testFlow
}