package com.jheng.bay.base.query

import org.apache.ibatis.jdbc.SQL

interface AggregationQuery {

    val aggregation: Set<String>?

    fun to_aggregation_sql(aggregation_field: String): String {
        if (this !is ModelQuery<*>) {
            throw UnsupportedOperationException("${this::class.qualifiedName} should extend ${ModelQuery::class.simpleName}")
        }

        return if (this is JoinQuery<*>) {
            val view = SQL().apply {
                SELECT("$table_name.*")
                FROM(table_name)
                apply_join(this)
                apply_where(this)
                if (this@AggregationQuery is SoftDeletableQuery) {
                    apply_delete_filter(this)
                }
                GROUP_BY("$table_name.id")
            }
            """  SELECT $aggregation_field as value, count(1) as count
                 FROM (
                     $view
                 ) $table_name
                 GROUP BY $aggregation_field """.trimIndent()
        } else {
            SQL().apply {
                SELECT("$aggregation_field as value, count(1) as count")
                FROM(table_name)
                apply_where(this)
                if (this@AggregationQuery is SoftDeletableQuery) {
                    apply_delete_filter(this)
                }
                GROUP_BY(aggregation_field)
            }.toString()
        }
    }

}
