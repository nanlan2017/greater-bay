package com.jheng.bay.base.query

import org.apache.ibatis.jdbc.SQL

interface SoftDeletableQuery {
    val table_name: String
    val include_deleted: Boolean
        get() = false
    val only_deleted: Boolean
        get() = false

    fun apply_delete_filter(sql: SQL) {
        sql.apply {
            when {
                only_deleted -> {
                    AND()
                    WHERE("$table_name.delete_time > 0")
                }
                !include_deleted -> {
                    AND()
                    WHERE("$table_name.delete_time = 0")
                }
            }
        }
    }
}
