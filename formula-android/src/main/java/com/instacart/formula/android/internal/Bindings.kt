package com.instacart.formula.android.internal

internal class Bindings<in Component>(
    val types: Set<Class<*>>,
    val bindings: List<Binding<Component>>
)
