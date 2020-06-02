package com.jheng.bay.util

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError
import kotlin.reflect.jvm.jvmErasure

object ReflectionUtil {
    private fun findPropClass(prop: KProperty<*>): KClass<*>? {
        return try {
            prop.returnType.jvmErasure
        } catch (e: KotlinReflectionInternalError) {
            null
        }
    }

    fun findPropElementClass(prop: KProperty<*>): KClass<*>? {
        return try {
            prop.returnType.arguments.firstOrNull()?.type?.jvmErasure
        } catch (e: KotlinReflectionInternalError) {
            null
        }
    }
}
