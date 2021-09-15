package com.instacart.formula.internal

/**
 * Note: this class is not a data class because equality is based on instance and not [key].
 */
@PublishedApi
internal open class Callback<T>(internal var key: Any) : (T) -> Unit {
    @PublishedApi internal var delegate: ((T) -> Unit)? = null

    override fun invoke(p1: T) {
        delegate?.invoke(p1)
        // TODO: log if null callback (it might be due to formula removal or due to callback removal)
    }
}

/**
 * Note: this class is not a data class because equality is based on instance and not [key].
 */
@PublishedApi
internal class UnitCallback(key: Any): Callback<Unit>(key), () -> Unit {
    override fun invoke() = invoke(Unit)
}