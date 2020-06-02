package com.jheng.bay.core.pojo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.jheng.bay.util.JacksonUtil
import kotlin.reflect.KClass

@JsonDeserialize(using = Patch.PatchDeserializer::class)
data class Patch<T : Any>(
        val raw_map: Map<String, Any?>,
        val model_class: KClass<T>
) {

    val result_map: Map<String, Any?> by lazy {
        JacksonUtil.apply_jackson_annotation(raw_map, model_class)
    }

    val id: Int? = raw_map["id"]?.toString()?.toInt()

    /**
     * refer to:
     * https://stackoverflow.com/questions/36159677/how-to-create-a-custom-deserializer-in-jackson-for-a-generic-type
     */
    class PatchDeserializer : JsonDeserializer<Patch<*>>(), ContextualDeserializer {
        lateinit var model_class: KClass<*>

        override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
            val wrapperType = property?.type ?: ctxt.contextualType
            val clazz = wrapperType.containedType(0).rawClass
            return PatchDeserializer().apply {
                model_class = clazz.kotlin
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Patch<*> {
            val raw_map = p.codec.readValue(p, Map::class.java) as Map<String, Any?>
            return Patch(raw_map, model_class)
        }
    }
}
