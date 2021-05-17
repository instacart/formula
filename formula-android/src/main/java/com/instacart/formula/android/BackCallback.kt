package com.instacart.formula.android

/**
 * Used to indicate that a screen render model
 * handles back presses.
 */
interface BackCallback {

    /**
     * Returns true if it handles back press.
     */
    fun onBackPressed(): Boolean

    companion object {
        inline operator fun invoke(crossinline op: () -> Boolean): BackCallback {
            return object : BackCallback {
                override fun onBackPressed(): Boolean = op()
            }
        }
    }
}
