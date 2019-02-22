package com.instacart.mvi

/**
 * Handles effects
 */
interface EffectHandler<Effect> {

    fun handle(effect: Effect)
}