package com.instacart.formula.android.internal

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

/** Functionally the same as [Iterable.forEach] except it generates an index-based loop that doesn't use an [Iterator]. */
internal inline fun <T> List<T>.forEachIndices(action: (T) -> Unit) {
    for (i in indices) {
        action(get(i))
    }
}

@OptIn(DelicateCoroutinesApi::class)
internal fun <T> FlowCollector<T>.emitBlocking(value: T) {
    GlobalScope.launch(
        context = Dispatchers.Unconfined,
        start = CoroutineStart.UNDISPATCHED,
    ) {
        emit(value)
    }
}