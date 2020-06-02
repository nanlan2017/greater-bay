package com.jheng.bay.config.mybatis

import com.jheng.bay.util.JsonUtil
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

class SetJsonTypeHandler : BaseTypeHandler<Set<*>?>() {
    override fun getNullableResult(
            rs: ResultSet,
            columnName: String
    ): Set<*>? {
        return rs.getString(columnName)
                ?.takeIf { it.isNotEmpty() }?.let {
                    JsonUtil.parse(it)
                }
    }

    override fun getNullableResult(
            rs: ResultSet,
            columnIndex: Int
    ): Set<*>? {
        return rs.getString(columnIndex)
                ?.takeIf { it.isNotEmpty() }?.let {
                    JsonUtil.parse(it)
                }
    }

    override fun getNullableResult(
            cs: CallableStatement,
            columnIndex: Int
    ): Set<*>? {
        return cs.getString(columnIndex)
                ?.takeIf { it.isNotEmpty() }?.let {
                    JsonUtil.parse(it)
                }
    }

    override fun setNonNullParameter(
            ps: PreparedStatement,
            i: Int,
            parameter: Set<*>?,
            jdbcType: JdbcType?
    ) {
        ps.setString(i, parameter?.let { JsonUtil.stringify(it) })
    }

}
