package com.instacart.formula.integration

/**
 * Used to indicate that a screen render model
 * handles back presses.
 */
interface BackCallback {

    /**
     * Returns true if it handles back press.
     */
    fun onBackPressed(): Boolean
}
