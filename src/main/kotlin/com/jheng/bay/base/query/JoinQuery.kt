package com.jheng.bay.base.query

import com.jheng.bay.annotation.Condition
import com.jheng.bay.base.model.BaseModel
import org.apache.ibatis.jdbc.SQL
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

abstract class JoinQuery<T : BaseModel> : ModelQuery<T>() {

    companion object {
        val alise_ref_regex: Regex = """[^a-zA-Z0-9_][a-zA-Z0-9_]+\.""".toRegex()    //e.g: " advisor_job.", "(task_schedule.", "=advisor_job."
    }

    /**
     *  currently we force to use table_name as self_alias
     *  to compatible with SoftDeltableQuery, AggregationQuery...
     */
    // val self_alias: String = table_name

    open fun apply_join(sql: SQL) {}

    @Suppress("IfThenToElvis")
    override fun to_conditions(): List<String> {
        val results = mutableListOf<String>()
        for (prop in this::class.declaredMemberProperties) {
            val prop_name = prop.name
            val prop_value = prop.getter.call(this) ?: continue
            val annotation = prop.findAnnotation<Condition>() ?: continue

            val table_alias_dot = annotation.table_alias.takeUnless { it.isBlank() }?.let { "$it." }
                    ?: "$table_name."
            val expression = annotation.expression
            val expr =
                    if (expression.isBlank()) {
                        val column_name = annotation.column_name.takeUnless { it.isBlank() }
                                ?: prop_name2column_name(prop_name)
                        val operator = annotation.operator.takeUnless { it.isBlank() }
                                ?: if (prop_value is Iterable<*>) "in" else "="
                        val right_operand = if (prop_value is Iterable<*>) {
                            val element_class = prop.returnType.arguments.firstOrNull()?.type?.jvmErasure!!
                            val collection = if (element_class.isSubclassOf(Number::class)) {
                                prop_value.joinToString(",")
                            } else {
                                prop_value.joinToString(",") { "'$it'" }
                            }
                            "($collection)"
                        } else {
                            "#{$prop_name}"
                        }
                        "$table_alias_dot$column_name $operator $right_operand"
                    } else {
                        replace_mybatis_placeholder(expr = expression, name = prop_name, value = prop_value)
                    }
            results.add(expr)

        }
        return results
    }

    override fun to_count_sql(): String {
        val view = SQL().apply {
            SELECT("$table_name.*")
            FROM(table_name)
            apply_join(this)
            apply_where(this)
            if (this@JoinQuery is SoftDeletableQuery) {
                apply_delete_filter(this)
            }
            GROUP_BY("$table_name.id")
        }
        return """
            SELECT count(1)
            FROM (
                $view
            ) $table_name
        """.trimIndent()
    }

    override fun to_list_sql(): String {
        val sql = SQL().apply {
            SELECT("$table_name.*")
            FROM(table_name)
            apply_join(this)
            apply_where(this)
            if (this@JoinQuery is SoftDeletableQuery) {
                apply_delete_filter(this)
            }
            GROUP_BY("$table_name.id")
            apply_sort(this)
            apply_pagination(this)
        }
        return sql.toString()
    }

    override fun apply_where(sql: SQL) {
        sql.apply {
            WHERE("1=1", *to_conditions().toTypedArray())
        }
    }

    //fixme
    // not only 'WHERE' , reference can also through "SORT"/"Aggregation"?
    fun get_referred_aliases(): Set<String> {
        val sql = SQL().apply {
            SELECT("$table_name.*")
            FROM(table_name)
            apply_where(this)
        }
        val where_statement = sql.toString().substringAfter("WHERE")
        return alise_ref_regex.findAll(where_statement)
                .asIterable()
                .flatMap { it.groupValues }
                .map { it.substring(1, it.length - 1) }
                .toSet() - table_name
    }
}
