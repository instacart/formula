package com.instacart.formula

import kotlin.reflect.KClass

interface Plugin {
    /**
     * A global callback to create [Inspector] for any formula. This will be called once when
     * formula is initially started.
     *
     * @param type Formula type.
     */
    fun inspector(type: KClass<*>): Inspector? {
        return null
    }
}