package com.instacart.formula.processor

import com.google.auto.service.AutoService
import com.instacart.formula.annotations.State
import com.instacart.formula.processor.StateAnnotationProcessor.ClassType.DATA_CLASS
import com.instacart.formula.processor.StateAnnotationProcessor.ClassType.OTHER
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.isDataClass
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.Filer
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

@Suppress("unused")
@AutoService(Processor::class)
class StateAnnotationProcessor : KotlinAbstractProcessor(), ProcessorEnv {

    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(State::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv
            .getElementsAnnotatedWith(State::class.java)
            .forEach { element ->
                val elementName = element.simpleName.toString()

                val error = { message: String ->
                    throw IllegalStateException("Issue with $elementName: $message")
                }

                // Check if a class has been annotated with @State
                if (element.kind != ElementKind.CLASS) {
                    error("can only annotate classes with ${State::class.java}")
                }

                // We can cast it, because we know that it of ElementKind.CLASS
                val typeElement = element as TypeElement

                if (element.classType != DATA_CLASS) {
                    error("can only annotate data classes with ${State::class.java}")
                }

                val packageName = elementUtils.getPackageOf(element).toString()
                val handled = StateAnnotationProcessingUtils.handleStateAnnotatedType(
                    packageName = packageName,
                    stateElement = element,
                    stateConstructorParamNames = element.getConstructorParamNames(),
                    reducerFactory = findReducerClassElement(typeElement)
                )

                handled.fold(
                    ifLeft = {
                        error(it.message)
                    },
                    ifRight = { files ->
                        files.forEach {
                            it.writeTo(filer)
                        }
                    })
            }

        return true
    }

    private fun findReducerClassElement(typeElement: TypeElement): TypeElement {
        return (try {
            elementUtils.getTypeElement(typeElement.getAnnotation(State::class.java).reducers.qualifiedName)
        } catch (e: MirroredTypeException) {
            typeUtils.asElement(e.typeMirror)
        } as TypeElement)
    }

    private fun fileDirectory(): File {
        val kaptKotlinGeneratedDir = options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            throw IllegalStateException("missing $KAPT_KOTLIN_GENERATED_OPTION_NAME")
        }

        // There is an issue in kapt where by default the build system
        // doesn't pick up generated files inside of kaptKotlin folder.
        // It does pickup files inside kapt folder
        // More info: https://github.com/square/kotlinpoet/issues/105
        val sanitize = kaptKotlinGeneratedDir.replace("kaptKotlin", "kapt")
        return File(sanitize)
    }

    private enum class ClassType {
        DATA_CLASS,
        OTHER;
    }

    private val Element.classType: ClassType
        get() = when {
            (kotlinMetadata as? KotlinClassMetadata)?.data?.classProto?.isDataClass == true -> ClassType.DATA_CLASS
            else -> OTHER
        }
}
