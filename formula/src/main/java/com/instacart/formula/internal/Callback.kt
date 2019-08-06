package com.instacart.formula.internal

/**
 * Note: this class is not a data class because equality is based on instance and not [key].
 */
class Callback(internal val key: Any): () -> Unit {
    @PublishedApi internal var callback: (() -> Unit)? = null

    override fun invoke() {
        callback?.invoke()
        // TODO: log if null callback (it might be due to formula removal or due to callback removal)
    }
}
