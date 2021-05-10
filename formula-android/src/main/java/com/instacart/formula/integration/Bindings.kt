package com.instacart.formula.integration

class Bindings<in Component>(
    val types: Set<Class<*>>,
    val bindings: List<Binding<Component>>
)
