package com.instacart.client.mvi.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Encapsulates KotlinPoet code that generates MyReducersEvents class
 */
object ICMviEventClassGenerator {

    fun createFile(
        packageName: String,
        prefix: String,
        generated: NextReducerClass?,
        passedReduceClass: NextReducerClass?
    ): FileSpec {
        val fileName = eventClassName(prefix = prefix)

        val passedReduceMethods = passedReduceClass?.reduceMethods.orEmpty()
        val generatedReduceMethods = generated?.reduceMethods.orEmpty()

        if (generatedReduceMethods.isEmpty() && passedReduceMethods.isEmpty()) {
            throw IllegalStateException("$prefix needs to have at least 1 reduce method")
        }

        val passedReducerVariableName = "reducers"
        val generatedReducerVariableName = "generatedReducers"

        val generatedProperties = parseConstructorProperties(generatedReduceMethods)
        val passedConstructorProperties = parseConstructorProperties(passedReduceMethods)
        val constructorProperties = passedConstructorProperties.plus(generatedProperties)
        val directInputCode = ICDirectInputCodeGenerators.generateCode(passedReduceMethods, passedReducerVariableName)

        val generatedDirectInputCode =
            ICDirectInputCodeGenerators.generateCode(generatedReduceMethods, generatedReducerVariableName)

        val bindParameters = constructorProperties.map {
            ParameterSpec.builder(it.name, it.type).build()
        }

        val bindStatements = ArrayList<ICReducerBindStatement>().apply {
            this += generatedDirectInputCode.bindStatements

            this += generatedReduceMethods.filter { !it.isDirectInput }.map {
                ICReducerBindStatement.constructorStream(
                    name = it.name,
                    reducerVariableName = generatedReducerVariableName
                )
            }

            this += passedConstructorProperties.map {
                ICReducerBindStatement.constructorStream(
                    name = it.name,
                    reducerVariableName = passedReducerVariableName
                )
            }

            this += directInputCode.bindStatements
        }

        return FileSpec.builder(packageName, fileName)
            .addType(
                TypeSpec
                    .classBuilder(fileName)
                    .addAnnotation(lombok.Generated::class)
                    .apply {
                        if (passedReduceClass != null) {
                            primaryConstructor(createConstructor(passedReducerVariableName, passedReduceClass))
                            addProperty(
                                PropertySpec
                                    .builder(passedReducerVariableName, passedReduceClass.type)
                                    .initializer(passedReducerVariableName)
                                    .addModifiers(KModifier.PRIVATE)
                                    .build()
                            )
                        }
                    }
                    .addProperties(directInputCode.relayProperties)
                    .addProperties(generatedDirectInputCode.relayProperties)
                    .apply {
                        if (generated != null) {
                            addProperty(
                                PropertySpec.builder(generatedReducerVariableName, generated.type)
                                    .initializer("%T()", generated.type)
                                    .addModifiers(KModifier.PRIVATE)
                                    .build()
                            )
                        }
                    }
                    .addFunctions(directInputCode.relayAccessorFunctions)
                    .addFunctions(generatedDirectInputCode.relayAccessorFunctions)
                    .apply {
                        addFunction(
                            ICEventsClassBindGenerator.generateBindFunction(
                                returnType = generated?.reduceMethods?.firstOrNull()?.returnTypeName
                                    ?: passedReduceClass?.reduceMethods?.first()?.returnTypeName!!,
                                params = bindParameters,
                                bindings = bindStatements
                            )
                        )
                    }
                    .build()
            )
            .build()
    }

    private fun eventClassName(prefix: String): String {
        return "${prefix}Events"
    }

    private fun parseConstructorProperties(reduceMethods: List<ReduceMethod>): List<PropertySpec> {
        return reduceMethods
            .filter { !it.isDirectInput }
            .map {
                PropertySpec.builder(it.name, it.flowableType())
                    .initializer(it.name)
                    .build()
            }
    }

    private fun createConstructor(
        parameterName: String,
        reducerClass: NextReducerClass
    ): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter(parameterName, reducerClass.type)
            .build()
    }
}
