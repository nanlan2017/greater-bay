package com.jheng.bay.base.mapper

import com.jheng.bay.base.model.BaseModel
import com.jheng.bay.base.query.ModelQuery
import org.apache.ibatis.annotations.*

interface BaseMapper<T : BaseModel> {

    @SelectProvider(type = BaseMapperSqlProvider::class)
    fun one(id: Int): T?

    @SelectProvider(type = BaseMapperSqlProvider::class)
    fun list(query: ModelQuery<T>): List<T>

    @SelectProvider(type = BaseMapperSqlProvider::class)
    fun count(query: ModelQuery<T>): Int

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @InsertProvider(type = BaseMapperSqlProvider::class)
    fun save(model: T)

    @UpdateProvider(type = BaseMapperSqlProvider::class)
    fun update(model: T): Int

    @DeleteProvider(type = BaseMapperSqlProvider::class)
    fun delete(id: Int)
}
