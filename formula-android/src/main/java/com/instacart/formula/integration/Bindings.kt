package com.instacart.formula.integration

class Bindings<Component, Key : Any>(
    val types: Set<Class<*>>,
    val bindings: List<Binding<Component, Key>>
)
