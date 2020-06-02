package com.jheng.bay.base.mapper

import com.jheng.bay.base.model.BaseModel
import com.jheng.bay.base.query.ModelQuery
import com.jheng.bay.extensions.base_model_companion
import com.jheng.bay.extensions.ensure_at_most_one
import org.apache.ibatis.builder.annotation.ProviderContext
import org.apache.ibatis.builder.annotation.ProviderMethodResolver
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


@Suppress("UNUSED_PARAMETER")
class BaseMapperSqlProvider : ProviderMethodResolver {
    @Suppress("UNCHECKED_CAST")
    val ProviderContext.modelClass: KClass<out BaseModel>
        get() = this.mapperType.genericInterfaces
                .flatMap { type ->
                    (type as? ParameterizedType)
                            ?.actualTypeArguments
                            ?.mapNotNull { (it as Class<*>).kotlin }
                            ?.toList()
                            ?: emptyList()
                }.filter { it.isSubclassOf(BaseModel::class) }
                .distinctBy { it.simpleName }
                .ensure_at_most_one("Too many type arguments.")!! as KClass<out BaseModel>

    fun list(
            context: ProviderContext,
            query: ModelQuery<*>
    ): String {
        return query.to_list_sql()
    }

    fun count(
            context: ProviderContext,
            query: ModelQuery<*>
    ): String {
        return query.to_count_sql()
    }

    fun one(
            context: ProviderContext,
            id: Int
    ): String = context.modelClass.base_model_companion.sql_select_by_id

    fun save(
            context: ProviderContext,
            model: BaseModel
    ): String = context.modelClass.base_model_companion.sql_save


    fun update(
            context: ProviderContext,
            model: BaseModel
    ): String = context.modelClass.base_model_companion.sql_update

    fun delete(
            context: ProviderContext,
            id: Int
    ): String = context.modelClass.base_model_companion.sql_delete_by_id
}
