package com.instacart.formula

/**
 * Used within [Stream] to receive a cancel event. Use this to perform clean up.
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
