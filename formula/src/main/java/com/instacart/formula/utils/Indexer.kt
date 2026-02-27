package com.instacart.formula.utils

internal class Indexer {
    private var indexes: MutableMap<Any, Int>? = null

    /**
     * Function which returns next index for a given key. It will
     * mutate the [indexes] map.
     */
    fun nextIndexedKey(key: Any): IndexedKey {
        val index = nextIndex(key)
        return IndexedKey(key, index)
    }

    fun clear() {
        indexes?.clear()
    }

    private fun nextIndex(key: Any): Int {
        val indexes = indexes ?: run {
            val initialized = mutableMapOf<Any, Int>()
            this.indexes = initialized
            initialized
        }

        val previousIndex = indexes[key]
        val index = if (previousIndex == null) {
            0
        } else {
            previousIndex + 1
        }
        indexes[key] = index
        return index
    }
}