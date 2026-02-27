package com.instacart.formula.lifecycle

/**
 * Tracks new and removed component keys across evaluations to ensure
 * deterministic re-evaluation. Only instantiated when the runtime has
 * validation enabled — zero cost in production.
 */
internal class LifecycleValidationManager(private val formulaType: Class<*>) {
    private var isPrepared = false
    private var previousNewKeys: List<Any>? = null
    private var previousRemovedKeys: List<Any>? = null
    private var newKeys: MutableList<Any>? = null
    private var removedKeys: MutableList<Any>? = null

    fun trackNewKey(key: Any) {
        val list = newKeys ?: mutableListOf<Any>().also { newKeys = it }
        list.add(key)
    }

    fun trackRemovedKey(key: Any) {
        val list = removedKeys ?: mutableListOf<Any>().also { removedKeys = it }
        list.add(key)
    }

    fun prepareValidationRun() {
        isPrepared = true
        previousNewKeys = newKeys?.toList()
        previousRemovedKeys = removedKeys?.toList()
        newKeys = null
        removedKeys = null
    }

    fun validate() {
        if (!isPrepared) {
            newKeys = null
            removedKeys = null
            return
        }
        isPrepared = false

        val unexpectedNew = newKeys.orEmpty()
            .filter { !previousNewKeys.orEmpty().contains(it) }
        val unexpectedRemoved = removedKeys.orEmpty()
            .filter { !previousRemovedKeys.orEmpty().contains(it) }

        if (unexpectedNew.isNotEmpty() || unexpectedRemoved.isNotEmpty()) {
            throw ValidationException(
                "$formulaType - components changed during validation" +
                        " - new: $unexpectedNew, removed: $unexpectedRemoved"
            )
        }

        previousNewKeys = null
        previousRemovedKeys = null
    }
}