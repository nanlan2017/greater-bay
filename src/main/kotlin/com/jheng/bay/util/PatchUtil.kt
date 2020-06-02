package com.jheng.bay.util

import com.jheng.bay.base.model.BaseModel
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.MapperFeature
import com.jheng.bay.extensions.id
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.reflect


//fixme
// Here we should use mapper2 or mapper3 which has none business with Jackson annotation but they haven't test passed.
val mapper1 = JacksonUtil.objectMapper

val mapper2 = mapper1.copy()
        .configure(MapperFeature.USE_ANNOTATIONS, false)!!

val mapper3 = mapper1.copy()
        .configure(MapperFeature.USE_ANNOTATIONS, false)
        .setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS)!!
// ----------------------------------------------------------------------------


fun <T : BaseModel> patch(
        old: T?,
        map: Map<String, Any?>?,
        updater: (T) -> Any,
        saver: ((T) -> Any)? = null
) {
    if (map == null) return
    @Suppress("UNCHECKED_CAST")
    val clazz = updater.reflect()!!.parameters.first().type.classifier as KClass<T>
    val t = mapper1.convertValue(map, clazz.java)!!
    if (old == null) {
        when {
            t.is_new && saver != null -> saver(t)
            t.is_new -> error("New records found but no save function provided.")
            !t.is_new -> error("Record of id:${t.id} not found.")
        }
    } else {
        if (!t.is_new && old.id != t.id) error("Id not match, exist id:${old.id} but you send id:${t.id}")
        val mutableProperties = clazz.memberProperties
                .filterIsInstance<KMutableProperty<*>>()
                .filter { it.setter.visibility == KVisibility.PUBLIC }
        val merged = clazz.createInstance()
        mutableProperties.forEach { it.setter.call(merged, it.getter.call(old)) }
        mutableProperties.filter { map.keys.contains(it.name) }.forEach { it.setter.call(merged, it.getter.call(t)) }
        updater(merged)
    }
}

/**
 * old models are read only.
 */
fun <T : BaseModel> patch(
        olds: List<T>?,
        maps: List<Map<String, Any?>>?,
        updater: (T) -> Any,
        saver: ((T) -> Any)? = null,
        batchSaver: ((List<T>) -> Any)? = null,
        deleter: ((T) -> Any)? = null
) {
    if (maps == null) return
    @Suppress("UNCHECKED_CAST")
    val clazz = updater.reflect()!!.parameters.first().type.classifier as KClass<T>
    val (toUpdate, toDelete) = (olds ?: emptyList()).partition { maps.mapNotNull { m -> m.id }.contains(it.id) }
    toUpdate.forEach { patch(it, maps.first { m -> m.id == it.id }, updater) }
    if (toDelete.isNotEmpty()) {
        when {
            deleter != null -> toDelete.forEach { deleter(it) }
            else -> println("Uncovered existing records found but no deleter provided.")
        }
    }
    val toSave = maps.filter { it.id == null || it.id == 0 }
            .map { mapper1.convertValue(it, clazz.java)!! }
    if (toSave.isNotEmpty()) {
        when {
            saver != null -> toSave.forEach { saver(it) }
            batchSaver != null -> batchSaver(toSave)
            else -> error("New records found but no save funtion provided.")
        }
    }
}
