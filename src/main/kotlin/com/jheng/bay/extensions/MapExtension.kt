package com.jheng.bay.extensions

import com.jheng.bay.base.model.BaseModel
import com.jheng.bay.base.model.BaseModelCompanion
import com.jheng.bay.core.pojo.PageSearch
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObjectInstance

val Map<String, Any?>.id: Int?
    get() = this["id"]?.toString()?.toInt()

fun Map<String, Any?>.toPageSearch(): PageSearch {
    return PageSearch.create(this)
}

//region for cascading patch
inline fun <reified T : BaseModel> Map<String, Any?>.mentioned_extra(): Set<String> {
    return (T::class.companionObjectInstance as? BaseModelCompanion<*>?)
            ?.availableExtras
            ?.filter { this.keys.contains(it) }
            ?.toSet() ?: emptySet()
}

@Suppress("UNCHECKED_CAST")
fun <K : BaseModel> Map<String, Any?>.nested(property: KProperty<K?>): Map<String, Any?>? =
        this[property.name] as Map<String, Any?>?


@Suppress("UNCHECKED_CAST")
fun <K : BaseModel> Map<String, Any?>.nested(property: KProperty<List<K>?>): List<Map<String, Any?>>? =
        this[property.name] as List<Map<String, Any?>>?


fun <K : BaseModel> Map<String, Any?>.delete_ignored(property: KProperty<List<K>?>): Boolean =
        this["${property.name}.delete_ignored"] !in setOf(null, "false", false)

fun <K : BaseModel> MutableMap<String, Any?>.set_delete_ignored(
        property: KProperty<List<K>?>,
        value: Boolean = true
) {
    this["${property.name}.delete_ignored"] = value
}
//endregion

fun MutableMap<String, Any?>.add_quote_for_iterable_value_entry(key: String) {
    this[key]?.toString()?.let { value ->
        this[key] = value.takeIf { it.isNotBlank() }
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?.joinToString(separator = ",") { "'$it'" }
    }
}