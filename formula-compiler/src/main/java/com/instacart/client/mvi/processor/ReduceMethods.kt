package com.instacart.client.mvi.processor

import com.squareup.kotlinpoet.TypeName

/**
 * All reduce methods should have the same return type
 */
class ReduceMethods(val returnType: TypeName, val methods: List<ReduceMethod>)
