package com.instacart.formula.internal

import com.instacart.formula.Listener

/**
 * Note: this class is not a data class because equality is based on instance and not [key].
 */
@PublishedApi
internal open class CallbackImpl<T>(internal var key: Any) : Listener<T> {
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
internal class UnitCallbackImpl(key: Any): CallbackImpl<Unit>(key), () -> Unit {
    override fun invoke() = invoke(Unit)
}