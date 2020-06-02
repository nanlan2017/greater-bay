package com.jheng.bay.base.service


import com.jheng.bay.base.mapper.BaseMapper
import com.jheng.bay.base.model.BaseModel
import com.jheng.bay.base.model.WithOptimisticLock
import com.jheng.bay.base.query.AggregationQuery
import com.jheng.bay.base.query.ModelQuery
import com.jheng.bay.core.AppContextHolder
import com.jheng.bay.core.CrudServices
import com.jheng.bay.core.pojo.BusinessException
import com.jheng.bay.core.pojo.PageData
import com.jheng.bay.extensions.assert_model_exist
import com.jheng.bay.extensions.base_model_companion
import com.jheng.bay.util.BeanUtil
import org.springframework.beans.factory.getBean
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

@Suppress("UnstableApiUsage")
interface BaseService<T : BaseModel> {

    val mapper: BaseMapper<T>

    @Suppress("UNCHECKED_CAST")
    val model_class: KClass<T>
        get() {
            val interfaceType = this::class.java.genericInterfaces
                    .first { it.typeName.startsWith(BaseService::class.qualifiedName!!) } as ParameterizedType
            return (interfaceType.actualTypeArguments.first() as Class<*>).kotlin as KClass<T>
        }

    @Suppress("RemoveExplicitTypeArguments")
    val crudServices: CrudServices
        get() = AppContextHolder.context.getBean<CrudServices>()


    //region find by id
    fun find(
            id: Int,
            extra: Set<String>? = null
    ): T? {
        val res = mapper.one(id) ?: return null
        return join(res, extra ?: emptySet())
    }

    @Transactional
    fun get(
            id: Int,
            extra: Set<String>? = null
    ): T {
        val res = mapper.one(id).assert_model_exist(message = "${model_class.simpleName} with id:$id not found.")
        return join(res, extra ?: emptySet())
    }
    //endregion

    //region find by query
    @Transactional
    fun first_or_null(query: ModelQuery<T>): T? {
        return list(query).firstOrNull()
    }

    @Transactional
    fun first(query: ModelQuery<T>): T {
        return list(query).firstOrNull().assert_model_exist()
    }

    fun find_all(query: ModelQuery<T>): List<T> {
        return mapper.list(query)
    }

    @Transactional
    fun count(query: ModelQuery<T>): Int {
        return mapper.count(query)
    }

    @Transactional
    fun exists(query: ModelQuery<T>): Boolean {
        return count(query) > 0
    }

    @Transactional
    fun list(query: ModelQuery<T>): List<T> {
        return join(find_all(query), query.extra ?: model_class.base_model_companion.defaultListExtras)
    }

    @Transactional
    fun page(query: ModelQuery<T>): PageData<T> {
        val list = list(query)
        val count = count(query)
        val agg = if (this is AggregationService
                && query is AggregationQuery) {
            this.aggregations(query)
        } else {
            null
        }
        return PageData.instance(list, count, agg)
    }

    //endregion

    @Transactional
    fun join(
            model: T,
            extra: Set<String>
    ): T {
        join(listOf(model), extra)
        return model
    }

    @Transactional
    fun join(
            list: List<T>,
            extra: Set<String>
    ): List<T> {
        return list
    }

    //region save
    fun save(model: T): T {
        mapper.save(model)
        return model
    }

    @Transactional
    fun save_batch(models: List<T>): List<T> {
        return models.map { save(it) }
    }
    //endregion

    //region update
    @Transactional
    fun update(model: T): T {
        val affected = mapper.update(model)

        if (model is WithOptimisticLock) {
            if (affected <= 0)
                throw BusinessException("update failed due to optimistic lock, please retry later")
            model.version++
        }
        return get(model.id)
    }

    @Transactional
    fun update_batch(models: List<T>): List<T> {
        return models.map { update(it) }
    }

    @Transactional
    fun save_or_update(model: T): T {
        return if (model.is_new) save(model) else update(model)
    }

    fun patch(
            id: Int,
            map: Map<String, Any?>
    ): T {
        val old = get(id, emptySet())
        val res = BeanUtil.assign(old, map)
        update(res)
        return get(id)
    }
    //endregion

    //region delete
    fun delete(id: Int) {
        mapper.delete(id)
    }

    @Transactional
    fun delete_batch(ids: Collection<Int>) {
        ids.forEach { delete(it) }
    }
    //endregion
}
