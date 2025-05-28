package com.instacart.formula.internal

import kotlinx.coroutines.CancellationException

@PublishedApi
internal suspend inline fun <Result> runCatchingCoroutines(
    crossinline action: suspend () -> Result
): kotlin.Result<Result> {
    return try {
        val result = action()
        kotlin.Result.success(result)
    } catch (e: CancellationException) {
        // We need to emit cancellation errors
        throw e
    } catch (e: Throwable) {
        kotlin.Result.failure(e)
    }
}