package com.instacart.formula.processor

import arrow.core.Either
import arrow.core.Option
import arrow.core.right
import arrow.core.toOption
import com.instacart.formula.Next
import com.instacart.formula.NextReducers
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.TypeElement

object ProcessorUtils {

    fun asReducerClass(reducerClassElement: TypeElement): Either<KnownError, Option<NextReducerClass>> {
        return if (isNotEmptyReducerClass(reducerClassElement)) {
            ReduceMethodUtils.findReduceMethods(reducerClassElement).map {
                val effectType = parseEffectType(it)

                NextReducerClass(
                    type = reducerClassElement.asType().asTypeName(),
                    reduceMethods = it.methods,
                    effectType = effectType
                ).toOption()
            }
        } else {
            Option.empty<NextReducerClass>().right()
        }
    }

    fun nextReducerType(stateClassType: TypeName, effectType: TypeName): TypeName {
        return Function1::class.asTypeName().parameterizedBy(
            stateClassType,
            Next::class.asTypeName().parameterizedBy(stateClassType, effectType)
        )
    }

    private fun isNotEmptyReducerClass(element: TypeElement): Boolean {
        return !element.asType().asTypeName().toString().contains(NextReducers::class.asTypeName().toString())
    }

    private fun parseEffectType(reduceMethods: ReduceMethods): TypeName {
        val reducerFunctionType = reduceMethods.returnType as ParameterizedTypeName
        val nextType = reducerFunctionType.typeArguments[1] as ParameterizedTypeName
        return nextType.typeArguments[1]
    }
}
