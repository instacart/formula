package com.instacart.formula

/**
 * Used within [Stream] to define how to cancel an operation.
 */
interface Cancelation {
    companion object {
        inline operator fun invoke(crossinline cancel: () -> Unit): Cancelation {
            return object : Cancelation {
                override fun cancel() {
                    cancel()
                }
            }
        }
    }

    fun cancel()
}