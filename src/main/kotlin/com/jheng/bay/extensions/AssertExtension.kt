package com.jheng.bay.extensions

import com.jheng.bay.base.model.BaseModel
import com.jheng.bay.core.pojo.BusinessException

fun <T : Any> T?.assert_exist(message: String? = null): T {
    return this ?: throw BusinessException(message)
}

/**
 * if message is present use message
 * else if id_hint is present use "resource with id $id_hint not found"
 * else use "resource not found"
 */
fun <T : BaseModel> T?.assert_model_exist(
        id_hint: Int? = null,
        message: String? = null
): T {
    return this ?: throw BusinessException(
            message ?: id_hint?.let {
                "resource with id $id_hint not found"
            } ?: "resource not found"
    )
}

fun Int?.assert_valid_id(message: String? = null): Int {
    val value = this ?: throw BusinessException(message)
    if (value <= 0) throw BusinessException(message)
    return value
}

val Int?.is_valid_id: Boolean
    get() = (this != null) && (this > 0)

fun <T : Any> List<T?>.ensure_at_most_one(message: String = "Resource found more than one!"): T? {
    if (this.size > 1)
        throw BusinessException(message)
    else
        return this.firstOrNull()
}