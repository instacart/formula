package com.instacart.formula.android

class Bindings<in Component>(
    val types: Set<Class<*>>,
    val bindings: List<Binding<Component>>
)
