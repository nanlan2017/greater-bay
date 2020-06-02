package com.jheng.bay.base.model

import com.google.common.base.CaseFormat
import com.google.common.reflect.TypeToken
import com.jheng.bay.annotation.AvailableExtra
import com.jheng.bay.annotation.Column
import com.jheng.bay.annotation.Table
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

@Suppress("UnstableApiUsage")
abstract class BaseModelCompanion<T : BaseModel> {
    /**
     * this is necessary for get the runtime Class Information of T, more specifically, modelClass
     */
    @Suppress("UNCHECKED_CAST")
    val modelClass: KClass<T> = ((object : TypeToken<T>(this::class.java) {}).rawType as Class<T>).kotlin

    //region extras
    open val availableExtras: Set<String> = modelClass.memberProperties.filter {
        it.findAnnotation<AvailableExtra>() != null
    }.map { it.name }.toSet()

    open val defaultListExtras: Set<String> = modelClass.memberProperties.filter {
        it.findAnnotation<AvailableExtra>()?.defaultShowInList ?: false
    }.map { it.name }.toSet()

    open val defaultDetailExtras: Set<String> = modelClass.memberProperties.filter {
        it.findAnnotation<AvailableExtra>()?.defaultShowInDetail ?: false
    }.map { it.name }.toSet()
    //endregion

    //region sql
    val table_name: String = modelClass.findAnnotation<Table>()?.name
            ?: CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modelClass.simpleName!!)

    val column_names: Set<String> = modelClass.memberProperties
            .mapNotNull { prop ->
                val column = prop.findAnnotation<Column>() ?: return@mapNotNull null
                column.name.takeIf { it.isNotBlank() } ?: prop.name
            }.toSet()

    val column_prop_pairs: List<Pair<String, String>> = modelClass.memberProperties.mapNotNull { prop ->
        val column = prop.findAnnotation<Column>() ?: return@mapNotNull null
        val column_name = column.name.takeIf { it.isNotBlank() } ?: prop.name
        column_name to prop.name
    }


    val sql_select_by_id: String =
            if (modelClass.isSubclassOf(SoftDeletable::class)) {
                """
                SELECT * FROM `$table_name` WHERE id = #{id} and delete_time = 0
                """.trimIndent()
            } else {
                """
                SELECT * FROM `$table_name` WHERE id = #{id}
                """.trimIndent()
            }


    val sql_save: String =
            """
            INSERT INTO `$table_name`
            (
                ${column_prop_pairs.joinToString(",") { "`${it.first}`" }}
            ) values (
                ${column_prop_pairs.joinToString(",") { "#{${it.second}}" }}
            )
            """.trimIndent()


    val sql_update =
            """
            UPDATE `$table_name`
            SET ${column_prop_pairs.joinToString(",") { "`${it.first}` = #{${it.second}}" }}
            WHERE `id` = #{id}
            """.trimIndent()


    val sql_delete_by_id: String =
            if (modelClass.isSubclassOf(SoftDeletable::class)) {
                """
                UPDATE `$table_name`
                SET delete_time = CURRENT_TIMESTAMP
                WHERE id = #{id}
                """.trimIndent()
            } else {
                """
                DELETE FROM `$table_name` WHERE id = #{id} 
                """.trimIndent()
            }
    //endregion
}
