package com.jheng.bay.extensions

import com.fasterxml.jackson.module.kotlin.convertValue
import com.jheng.bay.util.JacksonUtil
import kotlin.reflect.full.memberProperties

fun <T : Any> T.filterFields(
        remainFields: List<String>? = null,
        excludeFields: List<String>? = emptyList()
): Map<String, Any?> {
    val remain_keys = remainFields ?: this::class.memberProperties.map { it.name }
    val exclude_keys = excludeFields ?: emptyList()
    return JacksonUtil.objectMapper.convertValue<Map<String, Any?>>(this).filter {
        it.key in (remain_keys - exclude_keys)
    }
}

fun <T : Any> List<T>.filterFields(
        remainFields: List<String>? = null,
        excludeFields: List<String>? = emptyList()
): List<Map<String, Any?>> {
    return this.map { it.filterFields(remainFields, excludeFields) }
}
