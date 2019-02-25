package com.instacart.mvi

import com.instacart.formula.EffectHandler

/**
 * MviRelay is both an effect consumer and event producer.
 */
interface MviRelay<Effect, Event> : EffectHandler<Effect>, Producer<Event>
