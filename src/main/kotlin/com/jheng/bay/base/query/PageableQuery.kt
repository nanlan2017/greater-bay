package com.jheng.bay.base.query

import com.jheng.bay.base.query.pagination.PageRequest
import org.apache.ibatis.jdbc.SQL

/**
 * we can use spring annotation to set default value
 * e.g.
 * @PageableDefault(size = 50)
 * @SortDefault.SortDefaults(
 *      SortDefault(sort = arrayOf("id"), direction = Sort.Direction.DESC)
 * )
 */
interface PageableQuery : SortableQuery {
    /**
     * for now, it has to be mutable
     * because PageRequest class cannot be serialize,deserialize via jackson
     * if someday we managed to created our own Pageable implementation
     * we should change it to immutable property
     */
    var pageable: PageRequest?

    fun apply_pagination(sql: SQL) {
        val page = pageable?.page
        val size = pageable?.size
        sql.apply {
            size?.let { s ->
                LIMIT(s)
                page?.let { p ->
                    // offset = (page - 1) * size, offset must be gte 0
                    val tmp = ((p - 1) * s).takeIf { it > 0 } ?: 0
                    OFFSET(tmp.toLong())
                }
            }
        }
    }
}
