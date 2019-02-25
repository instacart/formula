package com.instacart.client.mvi.processor

import com.squareup.kotlinpoet.TypeName

data class NextReducerClass(
    val type: TypeName,
    val reduceMethods: List<ReduceMethod>,
    val effectType: TypeName
)
