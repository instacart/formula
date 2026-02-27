package com.instacart.formula.runtime

/**
 * Indicates a formula exception that could not be isolated and is
 * cascaded up the tree. The global error listener has already been
 * notified, so there's no need to log it again.
 */
class CascadingFormulaException(cause: Throwable) : RuntimeException(cause)
