package com.jheng.bay.base.mapper

import com.jheng.bay.base.query.AggregationQuery
import com.jheng.bay.core.pojo.AggregationBucket
import org.apache.ibatis.annotations.SelectProvider
import org.apache.ibatis.builder.annotation.ProviderContext
import org.apache.ibatis.builder.annotation.ProviderMethodResolver

interface AggregationMapper {
    class SqlProvider : ProviderMethodResolver {
        fun aggregation(
                context: ProviderContext,
                query: AggregationQuery,
                aggregation_field: String
        ): String {
            val sql = query.to_aggregation_sql(aggregation_field)
            return add_query_prefix_to_parameters(sql)
        }

        private val pattern = """#\{([a-zA-Z_.]+?)}""".toRegex()

        /**
         * replace #{param} to #{query.param}
         */
        private fun add_query_prefix_to_parameters(sql: String): String {
            return sql.replace(pattern) {
                "#{query.${it.groupValues[1]}}"
            }
        }
    }

    @SelectProvider(type = SqlProvider::class)
    fun aggregation(
            query: AggregationQuery,
            aggregation_field: String
    ): List<AggregationBucket>
}
