package com.jheng.bay.core

import com.jheng.bay.base.model.BaseModel
import com.jheng.bay.base.query.ModelQuery
import com.jheng.bay.base.service.BaseService
import com.jheng.bay.core.pojo.PageData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import kotlin.reflect.KClass

/**
 * Do not use it, this class is still at experimental stage
 */
@Service
class CrudServices {

    @Transient
    private lateinit var model_class_service_map: Map<KClass<*>, BaseService<*>>
    @Transient
    private lateinit var service_class_service_map: Map<KClass<*>, BaseService<*>>

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @PostConstruct
    fun postConstruct() {
        val services = applicationContext.getBeansOfType<BaseService<*>>()
        model_class_service_map = services.values.associateBy { it.model_class }
        service_class_service_map = services.values.associateBy { it::class }
    }

    //region accesor
    fun <M : BaseModel> service_by_model(model_class: KClass<M>): BaseService<M> {
        val service = model_class_service_map[model_class]
                ?: throw IllegalArgumentException("no service for ${model_class.simpleName} registered")
        @Suppress("UNCHECKED_CAST")
        return service as BaseService<M>
    }

    final inline fun <reified M : BaseModel> service_by_model(): BaseService<M> {
        return service_by_model(M::class)
    }

    fun <S : BaseService<out BaseModel>> service(service_class: KClass<S>): S {
        val service = service_class_service_map[service_class]
                ?: throw IllegalArgumentException("no service(class=${service_class.simpleName}) registered")
        @Suppress("UNCHECKED_CAST")
        return service as S
    }

    final inline fun <reified S : BaseService<out BaseModel>> service(): S {
        return service(S::class)
    }
    //endregion

    //region crud
    fun <M : BaseModel> find(
            model_class: KClass<M>,
            id: Int,
            extra: Set<String>? = null
    ): M? {
        return service_by_model(model_class).find(id, extra)
    }

    final inline fun <reified M : BaseModel> find (
            id: Int,
            extra: Set<String>? = null
    ): M? {
        return find(M::class, id, extra)
    }

    fun <M : BaseModel> get(
            model_class: KClass<M>,
            id: Int,
            extra: Set<String>? = null
    ): M {
        return service_by_model(model_class).get(id, extra)
    }

    final inline fun <reified M : BaseModel> get(
            id: Int,
            extra: Set<String>? = null
    ): M {
        return get(M::class, id, extra)
    }

    fun <M : BaseModel> list(
            model_class: KClass<M>,
            query: ModelQuery<M>
    ): List<M> {
        return service_by_model(model_class).list(query)
    }

    fun <M : BaseModel> list(query: ModelQuery<M>): List<M> {
        return list(query.model_class, query)
    }

    final inline fun <reified M : BaseModel> page(query: ModelQuery<M>): PageData<M> {
        return service_by_model<M>().page(query)
    }

    final inline fun <reified M : BaseModel> save(model: M): M {
        return service_by_model<M>().save(model)
    }

    final inline fun <reified M : BaseModel> save_batch(list: List<M>): List<M> {
        return service_by_model<M>().save_batch(list)
    }

    final inline fun <reified M : BaseModel> update(model: M): M {
        return service_by_model<M>().update(model)
    }

    final inline fun <reified M : BaseModel> patch(id: Int, map: Map<String, Any?>): M {
        return service_by_model<M>().patch(id, map)
    }

    final inline fun <reified M: BaseModel> save_or_update(model: M): M {
        return service_by_model<M>().save_or_update(model)
    }

    fun <M : BaseModel> delete(
            model_class: KClass<M>,
            id: Int
    ) {
        return service_by_model(model_class).delete(id)
    }

    final inline fun <reified M : BaseModel> delete(id: Int) {
        return delete(M::class, id)
    }
    //endregion

}
