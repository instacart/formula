package com.instacart.formula

/**
 * Used within [Stream] to define how to cancel an operation.
 */
interface Cancelable {
    companion object {
        inline operator fun invoke(crossinline cancel: () -> Unit): Cancelable {
            return object : Cancelable {
                override fun cancel() {
                    cancel()
                }
            }
        }
    }

    fun cancel()
}
