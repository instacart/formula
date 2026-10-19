package com.instacart.formula.android

/**
 * Used to indicate that a screen render model
 * handles back presses.
 */
fun interface BackCallback {

    /**
     * Returns true if it handles back press.
     */
    fun onBackPressed(): Boolean
}
