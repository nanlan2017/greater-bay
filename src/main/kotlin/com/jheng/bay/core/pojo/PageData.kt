package com.jheng.bay.core.pojo

class PageData<T : Any> private constructor(
        val list: List<T>,
        val count: Int,
        val aggregations: Map<String, List<AggregationBucket>>?
) {
    companion object {
        fun <T : Any> instance(
                list: List<T>,
                count: Int,
                aggregations: Map<String, List<AggregationBucket>>? = null
        ): PageData<T> {
            return PageData(list, count, aggregations)
        }
    }

    fun <S : Any> map(mapper: (T) -> S): PageData<S> {
        return instance(
                list = list.map(mapper),
                count = count,
                aggregations = aggregations
        )
    }

    fun copy(
            list: List<T> = this.list,
            count: Int = this.count,
            aggregations: Map<String, List<AggregationBucket>>? = this.aggregations
    ): PageData<T> {
        return instance(list, count, aggregations)
    }

}

