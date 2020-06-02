package com.jheng.bay.base.query.pagination

import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * For now, We use org.springframework.data.domain.Sort instead
 */
data class Sort(
        val orders: List<Order>
) {

    data class Order(
            val column: String,
            val direction: Direction
    )

    enum class Direction {
        ASC,
        DESC
    }

    object SortArgumentResolver : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return parameter.parameterType == Sort::class.java
        }

        override fun resolveArgument(
                parameter: MethodParameter,
                mavContainer: ModelAndViewContainer?,
                webRequest: NativeWebRequest,
                binderFactory: WebDataBinderFactory?
        ): Sort? {
            TODO("not implemented")
        }
    }

}
