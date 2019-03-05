package com.instacart.formula

/**
 * Takes an old state and produces a new state. Typically, this class is not used directly, but instead used through
 * implementing [Reducers]
 */
typealias Reducer<T> = (T) -> T
