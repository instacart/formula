package com.instacart.formula.integration

class Bindings<Component>(
    val types: Set<Class<*>>,
    val bindings: List<Binding<Component>>
)
