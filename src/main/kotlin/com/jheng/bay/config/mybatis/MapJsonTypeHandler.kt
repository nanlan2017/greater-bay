package com.jheng.bay.config.mybatis

import com.jheng.bay.util.JsonUtil
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

class MapJsonTypeHandler : BaseTypeHandler<Map<*, *>?>() {
    override fun getNullableResult(rs: ResultSet, columnName: String): Map<*, *>? {
        return rs.getString(columnName)
                ?.takeIf { it.isNotEmpty() }?.let {
                    JsonUtil.parse(it)
                }
    }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): Map<*, *>? {
        return rs.getString(columnIndex)
                ?.takeIf { it.isNotEmpty() }?.let {
                    JsonUtil.parse(it)
                }
    }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): Map<*, *>? {
        return cs.getString(columnIndex)
                ?.takeIf { it.isNotEmpty() }?.let {
                    JsonUtil.parse(it)
                }
    }

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: Map<*, *>?, jdbcType: JdbcType?) {
        ps.setString(i, parameter?.let { JsonUtil.stringify(it) })
    }
}
