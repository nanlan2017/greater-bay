package com.jheng.bay.base.query.pagination

/**
 * see org.springframework.data.web.PageableDefault
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class PageRequestDefault(
        val page: Int = 1,
        val size: Int = 20
)
