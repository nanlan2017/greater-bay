package com.jheng.bay.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class AvailableExtra(
        val defaultShowInDetail: Boolean = true,
        val defaultShowInList: Boolean = false
)
