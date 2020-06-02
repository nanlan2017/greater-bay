package com.jheng.bay.util

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.readValue
import com.jheng.bay.util.JacksonUtil.objectMapper
import com.jheng.bay.util.JacksonUtil.prettyWriter
import kotlin.reflect.KClass

object JsonUtil {

    fun stringify(
            obj: Any?,
            pretty: Boolean = false
    ): String {
        return if (pretty)
            prettyWriter.writeValueAsString(obj)
        else
            objectMapper.writeValueAsString(obj)
    }

    fun <T : Any> parse(
            json: String,
            clazz: KClass<T>
    ): T {
        return objectMapper.readValue(json, clazz.java)
    }

    inline fun <reified T : Any> parse(json: String): T {
        return objectMapper.readValue(json, jacksonTypeRef<T>())
    }

    fun <T : Any> extract(
            json: String,
            vararg path: Any
    ): T? {
        require(path.isNotEmpty()) { "path can not be empty" }

        var node: Any? = objectMapper.readValue(json)

        path.forEachIndexed { index, element ->
            node = when (element) {
                is String -> (node as Map<*, *>)[element]
                is Int -> (node as List<*>)[element]
                else -> throw IllegalArgumentException("path arguments can only be String or Int")
            }

            if (index != path.size - 1 && node == null)
                throw NullPointerException("double check your path")
        }

        @Suppress("UNCHECKED_CAST")
        return node as T?
    }
}
