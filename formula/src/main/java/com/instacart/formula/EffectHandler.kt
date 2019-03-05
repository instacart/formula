package com.instacart.formula

/**
 * Handles effects
 * @param Effect the type of the effect that is handled
 */
interface EffectHandler<Effect> {

    fun handle(effect: Effect)
}
