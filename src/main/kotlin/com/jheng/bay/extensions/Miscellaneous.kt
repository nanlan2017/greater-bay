package com.jheng.bay.extensions

import com.jheng.bay.base.mapper.BaseMapper
import com.jheng.bay.base.model.BaseModel
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

inline fun <T, R> Iterable<T>.flatMapNullable(transform: (T) -> Iterable<R>?): List<R> {
    return flatMapTo(ArrayList<R>()) {
        transform(it) ?: emptyList()
    }
}

@Suppress("UNCHECKED_CAST")
val <T : BaseModel> BaseMapper<T>.model_clazz: KClass<T>
    get() {
        val mapperType = this::class.java.genericInterfaces.first() as Class<*>
        val interfaceType = mapperType.genericInterfaces
                .first { it.typeName.startsWith(BaseMapper::class.qualifiedName!!) } as ParameterizedType
        return (interfaceType.actualTypeArguments.first() as Class<*>).kotlin as KClass<T>
    }