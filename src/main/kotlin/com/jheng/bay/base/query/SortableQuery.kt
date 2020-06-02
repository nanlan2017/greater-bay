package com.jheng.bay.base.query

import org.apache.ibatis.jdbc.SQL
import org.springframework.data.domain.Sort

/**
 * we can use spring annotation to set default value
 * e.g.
 * @SortDefault.SortDefaults(
 *      SortDefault(sort = arrayOf("id"), direction = Sort.Direction.DESC)
 * )
 */
interface SortableQuery {
    var sort: Sort?

    fun apply_sort(sql: SQL) {
        sql.apply {
            sort?.apply {
                val order_by = this.map {
                    "${it.property} ${it.direction}"
                }.toList().toTypedArray()
                ORDER_BY(*order_by)
            }
        }
    }
}
