package com.instacart.formula.processor

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.instacart.formula.annotations.DirectInput
import com.instacart.formula.annotations.ExportedProperty
import com.instacart.formula.NextReducers
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.shadow.utils.addToStdlib.ifNotEmpty
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

/**
 * We generate a reducer class for data class marked with [com.instacart.client.mvi.State]
 */
object StateAnnotationProcessingUtils {
    data class GeneratedReducerClass(
        val file: FileSpec,
        val className: ClassName,
        val stateType: TypeName,
        val reduceMethods: List<Pair<ExportedStateProperty, FunSpec>>
    )

    data class ExportedStateProperty(
        val name: String,
        val type: TypeName,
        val isDirectInput: Boolean
    )

    fun handleStateAnnotatedType(
        packageName: String,
        stateElement: TypeElement,
        stateConstructorParamNames: List<String>,
        reducerFactory: TypeElement
    ): Either<KnownError, List<FileSpec>> {
        return ProcessorUtils.asReducerClass(reducerFactory).flatMap {
            val exportedStateProperties =
                findExportedProperties(
                    stateElement,
                    stateConstructorParamNames
                )
            val parsedReducerClass = it.orNull()
            if (parsedReducerClass == null && exportedStateProperties.isEmpty()) {
                return KnownError("need to provide @State(reducers = ReducerClass) or mark some properties with @ExportedProperty")
                    .left()
            }

            val effectType = parsedReducerClass?.effectType ?: Unit::class.asTypeName()

            val prefix =
                createClassNamePrefix(stateElement)
            val generated = exportedStateProperties.ifNotEmpty {
                generateReducerClass(
                    prefix,
                    stateElement,
                    exportedStateProperties,
                    packageName
                )
            }
            val generatedNextReducer = generated?.let {
                asNextReducerClass(
                    it,
                    effectType
                )
            }

            val eventsClassFile = EventsClassGenerator.createFile(
                packageName = packageName,
                prefix = prefix,
                generated = generatedNextReducer,
                passedReduceClass = parsedReducerClass
            )

            return listOfNotNull(generated?.file, eventsClassFile).right()
        }
    }

    private fun createClassNamePrefix(stateElement: TypeElement): String {
        return if (stateElement.enclosingElement.kind == ElementKind.CLASS) {
            val parent = (stateElement.enclosingElement as TypeElement).simpleName.toString()
            "$parent${stateElement.simpleName}"
        } else {
            stateElement.simpleName.toString()
        }
    }

    private fun asNextReducerClass(
        generatedBaseClass: GeneratedReducerClass,
        effectType: TypeName
    ): NextReducerClass {
        val reducerType =
            ProcessorUtils.nextReducerType(generatedBaseClass.stateType, effectType)
        val reduceMethods = generatedBaseClass.reduceMethods.map {
            ReduceMethod(
                it.second.name,
                it.second.parameters.first().type,
                reducerType,
                it.first.isDirectInput
            )
        }

        return NextReducerClass(
            type = generatedBaseClass.className.parameterizedBy(effectType),
            reduceMethods = reduceMethods,
            effectType = effectType
        )
    }

    private fun generateReducerClass(
        prefix: String,
        stateElement: TypeElement,
        exportedStateProperties: List<ExportedStateProperty>,
        packageName: String
    ): GeneratedReducerClass {
        val fileName = "${prefix}GeneratedReducers"

        val stateClassType = stateElement.asType().asTypeName()
        val effectTypeVariable = TypeVariableName("Effect")

        val reduceMethods = exportedStateProperties.map {
            Pair(it,
                generateReducerFunction(
                    it,
                    stateClassType,
                    effectTypeVariable
                )
            )
        }
        val generatedReducerClassType = TypeSpec.classBuilder(fileName)
            .addTypeVariable(effectTypeVariable)
            .superclass(
                NextReducers::class.asTypeName().parameterizedBy(
                    stateClassType,
                    effectTypeVariable
                )
            )
            .addFunctions(reduceMethods.map { it.second })
            .build()

        val file = FileSpec.builder(packageName, fileName)
            .addType(generatedReducerClassType)
            .build()

        return GeneratedReducerClass(
            file = file,
            className = ClassName.bestGuess("$packageName.$fileName"),
            stateType = stateClassType,
            reduceMethods = reduceMethods
        )
    }

    private fun findExportedProperties(
        typeElement: TypeElement,
        constructorParamNames: List<String>
    ): List<ExportedStateProperty> {
        val exportedProperties = typeElement
            .enclosedElements
            .filter { it.getAnnotation(ExportedProperty::class.java) != null }

        return typeElement
            .enclosedElements
            .filter {
                constructorParamNames.contains(it.simpleName.toString())
            }
            .filter { it.kind != ElementKind.METHOD }
            .mapNotNull { element ->
                val name = element.simpleName.toString()
                val propertyType = element.asType().asTypeName().javaToKotlinType()

                exportedProperties.find { it.simpleName.contains(name) }?.let {
                    val annotation = it.getAnnotation(ExportedProperty::class.java)

                    ExportedStateProperty(
                        name = element.simpleName.toString(),
                        type = propertyType,
                        isDirectInput = annotation.isDirectInput
                    )
                }
            }
    }

    private fun generateReducerFunction(
        property: ExportedStateProperty,
        stateClassType: TypeName,
        effectType: TypeName
    ): FunSpec {
        val propertyName = property.name
        val functionName = "on${propertyName.capitalize()}Changed"

        return FunSpec.builder(functionName)
            .addParameter("property", property.type)
            .addCode("return·withoutEffects·{·it.copy($propertyName·=·property)·}")
            .apply {
                if (property.isDirectInput) {
                    addAnnotation(DirectInput::class.java)
                }
            }
            .returns(ProcessorUtils.nextReducerType(stateClassType, effectType))
            .build()
    }
}
