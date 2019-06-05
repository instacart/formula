package com.instacart.formula.processor

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.reactivex.Observable

object EventsClassBindGenerator {
    private const val LIST_VARIABLE_NAME = "list"

    fun generateBindFunction(
        returnType: TypeName,
        params: List<ParameterSpec>,
        bindings: List<ReducerBindStatement>
    ): FunSpec {
        val reducerType = Observable::class.asTypeName().parameterizedBy(returnType)

        val codeBlock = createReducerBindingList(
            LIST_VARIABLE_NAME,
            reducerType,
            bindings
        )

        return FunSpec.builder("bind")
            .returns(reducerType)
            .addParameters(params)
            .addCode(codeBlock)
            .addStatement("return·%T.merge($LIST_VARIABLE_NAME)", Observable::class.java)
            .build()
    }

    private fun createReducerBindingList(
        listVariableName: String,
        observableType: ParameterizedTypeName,
        bindings: List<ReducerBindStatement>
    ): CodeBlock {
        val codeBlockBuilder = CodeBlock.builder()
        codeBlockBuilder.addStatement("val·$listVariableName·=·%T<%T>()", ArrayList::class.java, observableType)

        bindings.forEach {
            codeBlockBuilder.addStatement(
                "$listVariableName.add(${it.format})",
                *it.args
            )
        }

        return codeBlockBuilder.build()
    }
}
