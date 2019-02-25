package com.instacart.client.mvi.processor

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.reactivex.Flowable

object ICEventsClassBindGenerator {
    private const val LIST_VARIABLE_NAME = "list"

    fun generateBindFunction(
        returnType: TypeName,
        params: List<ParameterSpec>,
        bindings: List<ICReducerBindStatement>
    ): FunSpec {
        val reducerType = Flowable::class.asTypeName().parameterizedBy(returnType)

        val codeBlock = createReducerBindingList(
            LIST_VARIABLE_NAME,
            reducerType,
            bindings
        )

        return FunSpec.builder("bind")
            .returns(reducerType)
            .addParameters(params)
            .addCode(codeBlock)
            .addStatement("return·%T.merge($LIST_VARIABLE_NAME)", Flowable::class.java)
            .build()
    }

    private fun createReducerBindingList(
        listVariableName: String,
        flowableType: ParameterizedTypeName,
        bindings: List<ICReducerBindStatement>
    ): CodeBlock {
        val codeBlockBuilder = CodeBlock.builder()
        codeBlockBuilder.addStatement("val·$listVariableName·=·%T<%T>()", ArrayList::class.java, flowableType)

        bindings.forEach {
            codeBlockBuilder.addStatement(
                "$listVariableName.add(${it.format})",
                *it.args
            )
        }

        return codeBlockBuilder.build()
    }
}
