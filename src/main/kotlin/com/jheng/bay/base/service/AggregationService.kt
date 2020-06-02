package com.jheng.bay.base.service

import com.jheng.bay.base.query.AggregationQuery
import com.jheng.bay.base.mapper.AggregationMapper
import com.jheng.bay.core.pojo.AggregationBucket
import org.springframework.transaction.annotation.Transactional

interface AggregationService {

    val mapper: AggregationMapper

    @Transactional
    fun aggregations(query: AggregationQuery): Map<String, List<AggregationBucket>>? {
        val agg_mapper = mapper as? AggregationMapper
                ?: throw UnsupportedOperationException("${this::class.qualifiedName} should extend ${AggregationMapper::class.simpleName}")
        val fields = query.aggregation ?: return null
        return fields.map {
            it to agg_mapper.aggregation(query, it)
        }.toMap()
    }
}
