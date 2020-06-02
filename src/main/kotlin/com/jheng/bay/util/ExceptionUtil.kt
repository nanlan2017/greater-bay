package com.jheng.bay.util


fun biz_require(
        value: Boolean,
        otherwise: () -> Any = { "Business require unsatisfied." }
) {
    if (!value) {
        val result = otherwise()
        if (result is String)
            throw IllegalArgumentException(result)
    }
}

fun <T : Any> biz_required_not_null(
        value: T?,
        otherwise: () -> String = { "Business require not null." }
): T {
    requireNotNull(value) { otherwise() }
    return value
}

fun biz_check(
        value: Boolean,
        otherwise: () -> Any = { "Business check failed." }
) = biz_require(value, otherwise)

fun <T : Any> biz_checked_not_null(
        value: T?,
        otherwise: () -> String = { "Business check not null failed." }
) = biz_required_not_null(value, otherwise)
