package com.instacart.formula.processor

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.instacart.formula.annotations.DirectInput
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

object ReduceMethodUtils {
    fun findReduceMethods(reducerClass: TypeElement): Either<KnownError, ReduceMethods> {
        val methods = collectMethods(reducerClass)

        // Only care about single parameter
        val validMethods = methods
            .filter { it.parameters.size == 1 }
            .map {
                val parameter = it.parameters.first()
                val parameterType = parameter.asType().asTypeName().javaToKotlinType()
                ReduceMethod(
                    name = it.simpleName.toString(),
                    parameterType = parameterType,
                    returnTypeName = it.returnType.asTypeName().javaToKotlinType(),
                    isDirectInput = it.getAnnotation(DirectInput::class.java) != null
                )
            }

        if (validMethods.isEmpty()) {
            return KnownError("${reducerClass.simpleName} needs to have at least 1 reduce method")
                .left()
        }

        val returnType = validMethods.first().returnTypeName
        val allMethodsReturnSameType = validMethods.all { it.returnTypeName == returnType }
        if (!allMethodsReturnSameType) {
            val types = validMethods.map { it.returnTypeName }.distinct()
            return KnownError("${reducerClass.simpleName} has mismatching type in the reduce function, types: $types")
                .left()
        }

        return ReduceMethods(
            returnType = returnType,
            methods = validMethods
        ).right()
    }

    private fun collectMethods(element: TypeElement): List<ExecutableElement> {
        return element
            .enclosedElements
            .filter { it.kind == ElementKind.METHOD && it.modifiers.contains(Modifier.PUBLIC) }
            .filterIsInstance<ExecutableElement>()
    }
}
