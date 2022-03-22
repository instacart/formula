package com.instacart.formula

/**
 * Used within [Action] to receive a cancel event and perform clean up.
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
