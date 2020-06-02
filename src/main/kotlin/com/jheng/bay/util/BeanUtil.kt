package com.jheng.bay.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.convertValue
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

object BeanUtil {
    val objectMapper = JacksonUtil.objectMapper.copy()
            // fixme
            // disable jackson annotation
            // cause "Cannot construct instance of `ImmutableDataClass` (no Creators, like default construct, exist)" problem
            // when construct a class via primary constructor
            // we may need to write our own method instead of calling "objectMapper.convertValue(source)"
            .configure(MapperFeature.USE_ANNOTATIONS, false)
            .setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS)!! // include null value

    fun to_map(source: Any): Map<String, Any?> {
        return objectMapper.convertValue(source)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun merge_to_map(vararg sources: Any): Map<String, Any?> {
        return sources.map { to_map(it) }
                .foldRight(mapOf()) { curr, acc ->
                    curr + acc
                }
    }

    /**
     * this method mutates target obj
     * it acts like Object.assign(target, ...sources) in javascript
     */
    fun <T : Any> assign(
            target: T,
            vararg sources: Any
    ): T {
        val map = merge_to_map(*sources)
        val source = objectMapper.convertValue(map, target::class.java)
        target::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
                .filter { map.containsKey(it.name) && it.setter.visibility == KVisibility.PUBLIC }
                .forEach { it.setter.call(target, it.getter.call(source)) }
        return target
    }

}
