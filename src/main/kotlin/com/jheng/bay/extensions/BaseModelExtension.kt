package com.jheng.bay.extensions

import com.jheng.bay.base.model.BaseModel
import com.jheng.bay.base.model.BaseModelCompanion
import com.jheng.bay.util.BeanUtil

import com.jheng.bay.util.biz_checked_not_null
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObjectInstance

@Suppress("UNCHECKED_CAST")
val <T : BaseModel> KClass<T>.base_model_companion: BaseModelCompanion<T>
    get() = biz_checked_not_null(this.companionObjectInstance as? BaseModelCompanion<T>) {
        "Rember to inherite BaseModelCompanion for ${this.simpleName}"
    }

//region ids
@Deprecated("use ids() instead.")
fun <T : BaseModel> List<T>.legacy_ids(id_property: KProperty<Int?> = BaseModel::id): String {
    val ids = this.mapNotNull { id_property.getter.call(it) }
            .distinct()
    return if (ids.isEmpty())
    // a simple trick to prevent return empty string
    // it may break things as the caller of this method may not expect this
    // anyway, let's see
        "0"
    else
        ids.joinToString(",")
}

fun <T : BaseModel> List<T>.ids(id_property: KProperty<Int?> = BaseModel::id): Set<Int> {
    val ids = this.mapNotNull { id_property.getter.call(it) }
            .toSet()
    return ids.takeIf { it.isNotEmpty() } ?: setOf(0)
}

fun String.to_ids(vararg delimiters: Char = charArrayOf(',')): Set<Int> {
    return this.split(*delimiters)
            .filter { it.isNotEmpty() }
            .map { it.toInt() }
            .toSet()
}

//endregion

fun <T : BaseModel> T.props_match(
        other: T,
        properties: Set<KProperty<*>>
): Boolean {
    return properties.all {
        it.getter.call(this) == it.getter.call(other)
    }
}

/**
 * this method mutates "this" obj
 * @see BeanUtil.assign
 */
fun <T : BaseModel> T.patch(obj: Any) {
    BeanUtil.assign(this, obj)
}


