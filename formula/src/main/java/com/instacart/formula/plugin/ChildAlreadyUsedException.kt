package com.instacart.formula.plugin

class ChildAlreadyUsedException(
    val parentType: Class<*>,
    val childType: Class<*>,
    val key: Any?
) : IllegalStateException("Formula $childType with a key $key already requested by $parentType")