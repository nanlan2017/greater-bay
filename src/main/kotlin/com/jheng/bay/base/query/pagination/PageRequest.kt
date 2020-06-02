package com.jheng.bay.base.query.pagination

import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * a simplified Pageable class
 * see org.springframework.data.domain.PageRequest
 */
data class PageRequest(
        val size: Int,
        val page: Int = FIRST_PAGE_INDEX
) {
    companion object {
        const val FIRST_PAGE_INDEX = 1
    }

    object PageRequestArgumentResolver : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return parameter.parameterType == PageRequest::class.java
        }

        override fun resolveArgument(
                parameter: MethodParameter,
                mavContainer: ModelAndViewContainer?,
                webRequest: NativeWebRequest,
                binderFactory: WebDataBinderFactory?
        ): PageRequest? {
            val page = webRequest.getParameter("page")?.toIntOrNull() ?: FIRST_PAGE_INDEX
            val size = webRequest.getParameter("size")?.toIntOrNull()
            val def = parameter.getParameterAnnotation(PageRequestDefault::class.java)
            return when {
                size != null -> PageRequest(size, page)
                def != null -> PageRequest(def.size, def.page)
                else -> null
            }
        }
    }

}
