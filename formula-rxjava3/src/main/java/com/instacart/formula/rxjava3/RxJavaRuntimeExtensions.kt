package com.instacart.formula.rxjava3

import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.toFlow
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.handleCoroutineException
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlin.coroutines.CoroutineContext

fun <Output : Any> IFormula<Unit, Output>.toObservable(
    config: RuntimeConfig? = null,
): Observable<Output> {
    return toFlow(config).asObservableV2()
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Input,
    config: RuntimeConfig? = null,
): Observable<Output> {
    return toFlow(input, config).asObservableV2()
}

fun <Input : Any, Output : Any> IFormula<Input, Output>.toObservable(
    input: Observable<Input>,
    config: RuntimeConfig? = null,
): Observable<Output> {
    val inputFlow = input.asFlow()
    return toFlow(inputFlow, config).asObservableV2()
}

/**
 * This logic was copied and modified `kotlinx.coroutines.rx3.asObservable`. The changes are:
 * - Removed `coroutineContext` parameter.
 * - Changing `start` from `ATOMIC` to `UNDISPATCHED` to ensure that the flow starts immediately.
 */
@OptIn(DelicateCoroutinesApi::class)
private fun <Output : Any> Flow<Output>.asObservableV2(): Observable<Output> {
    return Observable.create { emitter ->
        val job = GlobalScope.launch(
            context = Dispatchers.Unconfined,
            start = CoroutineStart.UNDISPATCHED,
        ) {
            try {
                collect { emitter.onNext(it) }
                emitter.onComplete()
            } catch (e: Throwable) {
                // 'create' provides safe emitter, so we can unconditionally call on* here if exception occurs in `onComplete`
                if (e !is CancellationException) {
                    if (!emitter.tryOnError(e)) {
                        handleUndeliverableException(e, coroutineContext)
                    }
                } else {
                    emitter.onComplete()
                }
            }
        }
        emitter.setCancellable { job.cancel() }
    }
}

@OptIn(InternalCoroutinesApi::class)
internal fun handleUndeliverableException(cause: Throwable, context: CoroutineContext) {
    try {
        RxJavaPlugins.onError(cause)
    } catch (e: Throwable) {
        cause.addSuppressed(e)
        handleCoroutineException(context, cause)
    }
}
