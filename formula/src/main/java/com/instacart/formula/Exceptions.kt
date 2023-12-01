package com.instacart.formula

/**
 * Thrown when a child formula is added with a duplicate key.
 */
class DuplicateKeyException(override val message: String?) : IllegalStateException()
