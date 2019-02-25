package com.instacart.formula.processor

import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.lang.model.element.Element

interface ProcessorEnv : KotlinMetadataUtils {

    fun Element.getConstructorParamNames(): List<String> = kotlinMetadata
        .let { it as KotlinClassMetadata }.data
        .let { (nameResolver, classProto) ->
            classProto.constructorOrBuilderList
                .first()
                .valueParameterList
                .map(ProtoBuf.ValueParameter::getName)
                .map(nameResolver::getString)
        }
}
