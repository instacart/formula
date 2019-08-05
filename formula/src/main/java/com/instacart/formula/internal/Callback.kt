package com.instacart.formula.internal

/**
 * Note: this class is not a data class because equality is based on instance and not [key].
 */
class Callback(val key: Any): () -> Unit {
    @PublishedApi internal lateinit var callback: () -> Unit

    override fun invoke() {
        callback()
    }
}
