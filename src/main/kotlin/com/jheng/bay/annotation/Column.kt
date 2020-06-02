package com.jheng.bay.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Column(
        val name: String = ""
)
