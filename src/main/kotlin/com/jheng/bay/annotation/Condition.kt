package com.jheng.bay.annotation

@Target(AnnotationTarget.PROPERTY)
annotation class Condition(
        val table_alias: String = "",
        val column_name: String = "",
        val operator: String = "",

        /**
         * highest priority.
         * remember to contain alias if needed.
         */
        val expression: String = ""
)