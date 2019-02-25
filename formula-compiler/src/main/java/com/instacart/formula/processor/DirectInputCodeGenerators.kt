package com.instacart.formula.processor

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName

object DirectInputCodeGenerators {
    class Code(
        val relayProperties: List<PropertySpec>,
        val relayAccessorFunctions: List<FunSpec>,
        val bindStatements: List<ReducerBindStatement>
    )

    private fun createRelayProperties(method: ReduceMethod): PropertySpec {
        return PropertySpec.builder(method.name, method.relayType())
            .initializer("BehaviorRelay.create()")
            .addModifiers(KModifier.PRIVATE)
            .build()
    }

    private fun createRelayAccessorFunctions(method: ReduceMethod): FunSpec {
        val reducerType = Unit::class.asTypeName()

        val inputName = "action"
        val codeBlockBuilder = CodeBlock.builder()
        codeBlockBuilder.addStatement("${method.name}.accept($inputName)")

        return FunSpec.builder(method.name)
            .returns(reducerType)
            .addParameter(inputName, method.parameterType)
            .addCode(codeBlockBuilder.build())
            .build()
    }

    fun generateCode(
        methods: List<ReduceMethod>,
        reducerVariableName: String
    ): Code {
        val directInputMethods = methods.filter { it.isDirectInput }
        return Code(
            relayProperties = directInputMethods.map(this::createRelayProperties),
            relayAccessorFunctions = directInputMethods.map(this::createRelayAccessorFunctions),
            bindStatements = directInputMethods.map {
                ReducerBindStatement.localRelay(
                    name = it.name,
                    reducerVariableName = reducerVariableName
                )
            }
        )
    }
}
