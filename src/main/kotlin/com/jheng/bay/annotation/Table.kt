package com.jheng.bay.annotation

@Target(AnnotationTarget.CLASS)
annotation class Table(
        val name: String
)
