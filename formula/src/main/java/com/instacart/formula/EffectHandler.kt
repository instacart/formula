package com.instacart.formula

/**
 * Handles effects
 */
interface EffectHandler<Effect> {

    fun handle(effect: Effect)
}
