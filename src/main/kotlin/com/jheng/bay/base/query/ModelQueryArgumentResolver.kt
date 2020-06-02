package com.jheng.bay.base.query

import com.jheng.bay.base.query.pagination.PageRequest
import com.jheng.bay.util.JacksonUtil
import org.springframework.core.MethodParameter
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortHandlerMethodArgumentResolver
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

/**
 * argument resolver for ModelQuery class
 * see also:
 * org.springframework.web.method.support.HandlerMethodArgumentResolver
 * org.springframework.web.method.annotation.ModelAttributeMethodProcessor
 */
class ModelQueryArgumentResolver : HandlerMethodArgumentResolver {

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> resolve_query(
                param_map: Map<String, String?>,
                clazz: KClass<T>
        ): T {
            return if (clazz.isSubclassOf(JoinQuery::class))
                param_map2join_query(param_map, clazz as KClass<out JoinQuery<*>>) as T
            else
                param_map2query(param_map, clazz)
        }

        /**
         * convert parameter map to query object
         * e.g. ids=1,2 => {ids: [1,2]}
         * e.g. foo.foo=1&foo.bar=2 => {"foo": {"foo":1,"bar":2}}
         */
        fun <T : Any> param_map2query(
                param_map: Map<String, String?>,
                clazz: KClass<T>
        ): T {
            val name_type_map = clazz.memberProperties
                    .map { it.name to it.returnType.jvmErasure }
                    .toMap()
            val nested_name_value_map = param_map.toList()
                    // if key doesn't contains "." , it is considered as top level key
                    .groupBy { it.first.substringBefore(".", "") }
                    .mapValues { (k, v) ->
                        val value = v.toMap().mapKeys { it.key.substringAfter("$k.") }
                        if (k.isEmpty()) {
                            value
                        } else {
                            name_type_map[k]?.let {
                                // recursive call to handle multiple nested level(something like 'foo.bar.baz')
                                param_map2query(value, it)
                            }
                        }
                    }

            @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
            val top_level_map = (nested_name_value_map[""] as? Map<String, String?> ?: emptyMap())
                    .mapValues { (k, v) ->
                        // ignore sort field as we are using SortHandlerMethodArgumentResolver to resolve sor property
                        if (k == PageableQuery::sort.name) return@mapValues null

                        val value_type = name_type_map[k]
                        if (value_type != null && value_type.isSubclassOf(Iterable::class)) {
                            v?.split(",")
                                    ?.filter { it.isNotBlank() }
                                    // we ignore query string like ?property_in=&ids=
                                    ?.takeIf { it.isNotEmpty() }
                        } else {
                            v
                        }
                    }
            val name_value_map = top_level_map + nested_name_value_map
            return JacksonUtil.objectMapper.convertValue(name_value_map, clazz.java)
        }

        fun <T : JoinQuery<*>> param_map2join_query(
                map: Map<String, String?>,
                clazz: KClass<T>
        ): T {
            val name_type_map = clazz.memberProperties
                    .map { it.name to it.returnType.jvmErasure }
                    .toMap()

            @Suppress("IMPLICIT_CAST_TO_ANY")
            val new_map = map.mapValues { (k, v) ->
                if (k == PageableQuery::sort.name) return@mapValues null
                val value_type = name_type_map[k]
                if (value_type != null && value_type.isSubclassOf(Iterable::class)) {
                    v?.split(",")?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
                } else {
                    v
                }
            }

            return JacksonUtil.objectMapper.convertValue(new_map, clazz.java)
        }
    }

    val sortResolver: SortHandlerMethodArgumentResolver = SortHandlerMethodArgumentResolver()

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return ModelQuery::class.java.isAssignableFrom(parameter.parameterType)
    }

    override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
    ): Any? {
        val clazz = parameter.parameterType.kotlin
        val param_map = webRequest.parameterMap
                .mapValues { (_, v) ->
                    // as we don't use query parameter suck like "foo=a&foo=b"
                    // the v should at most contains one element
                    v.firstOrNull()
                }
        val res = resolve_query(param_map, clazz)

        if (res is SortableQuery) {
            val sort = sortResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)
            res.sort = sort.takeIf { it != Sort.unsorted() }
        }

        if (res is PageableQuery) {
            res.pageable = PageRequest.PageRequestArgumentResolver
                    .resolveArgument(parameter, mavContainer, webRequest, binderFactory)
//            val pageable = pageableResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)
//            res.pageable = pageable.takeIf { it != Pageable.unpaged() }
        }
        return res
    }
}
