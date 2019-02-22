package com.instacart.mvi

/**
 * MviRelay is both an effect consumer and event producer.
 */
interface MviRelay<Effect, Event> : EffectHandler<Effect>, Producer<Event>
