package com.instacart.formula.android.internal

/** Functionally the same as [Iterable.forEach] except it generates an index-based loop that doesn't use an [Iterator]. */
internal inline fun <T> List<T>.forEachIndices(action: (T) -> Unit) {
    for (i in indices) {
        action(get(i))
    }
}
