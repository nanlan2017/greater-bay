package com.jheng.bay.base.query

import com.jheng.bay.base.model.BaseModel
import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.reflect.TypeToken
import com.jheng.bay.annotation.Condition
import com.jheng.bay.extensions.base_model_companion
import com.jheng.bay.util.ReflectionUtil
import org.apache.ibatis.jdbc.SQL
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

@Suppress("UnstableApiUsage")
abstract class ModelQuery<T : BaseModel> : PageableQuery {
    abstract val extra: Set<String>?

    companion object {
        fun prop_name2column_name(prop_name: String): String {
            return when {
                prop_name == "ids" -> "id"
                prop_name.endsWith("_ids") -> {
                    // remove trailing 's'
                    prop_name.slice(0..prop_name.length - 2)
                }
                else -> prop_name
            }
        }

        fun replace_mybatis_placeholder(
                expr: String,
                name: String,
                value: Any
        ): String {
            return expr.replace("#{}", "#{$name}")
                    .replace("\${}", "$value")
        }
    }

    @Suppress("UNCHECKED_CAST")
    @JsonIgnore
    val model_class: KClass<T> = ((object : TypeToken<T>(this::class.java) {}).rawType as Class<T>).kotlin
    @JsonIgnore
    val table_name = model_class.base_model_companion.table_name

    open fun to_conditions(): List<String> {
        return this::class.declaredMemberProperties
                .mapNotNull { prop ->
                    val prop_name = prop.name

                    @Suppress("UNUSED_VARIABLE")
                    val prop_value = prop.getter.call(this) ?: return@mapNotNull null
                    val condition = prop.findAnnotation<Condition>() ?: return@mapNotNull null
                    if (condition.expression.isBlank()) {
                        // resolve column name
                        val column_name = condition.column_name.takeIf { it.isNotBlank() }
                                ?: prop_name2column_name(prop_name)

                        // resolve operator
                        val operator = if (condition.operator.isNotEmpty()) {
                            condition.operator
                        } else {
                            if (prop_value is Iterable<*>) {
                                "in"
                            } else {
                                "="
                            }
                        }

                        val right_operand = if (prop_value is Iterable<*>) {
                            val element_class = ReflectionUtil.findPropElementClass(prop)!!
                            val collection = if (element_class.isSubclassOf(Int::class)) {
                                prop_value.joinToString(",")
                            } else {
                                prop_value.joinToString(",") { "'$it'" }
                            }
                            "($collection)"
                        } else {
                            "#{$prop_name}"
                        }

                        "$table_name.$column_name $operator $right_operand"
                    } else {
                        replace_mybatis_placeholder(expr = condition.expression, name = prop_name, value = prop_value)
                    }
                }
    }

    open fun apply_where(sql: SQL) {
        sql.apply {
            // we need 1=1 here
            // otherwise AbstractSQL.AND() method may produce an empty condition block
            // such as where () and (name='foo')
            WHERE("1=1", *to_conditions().toTypedArray())
        }
    }

    open fun to_list_sql(): String {
        return SQL().apply {
            SELECT("*")
            FROM(table_name)
            apply_where(this)
            if (this@ModelQuery is SoftDeletableQuery) {
                apply_delete_filter(this)
            }
            apply_sort(this)
            apply_pagination(this)
        }.toString()
    }

    open fun to_count_sql(): String {
        return SQL().apply {
            SELECT("count(1)")
            FROM(table_name)
            apply_where(this)
            if (this@ModelQuery is SoftDeletableQuery) {
                apply_delete_filter(this)
            }
        }.toString()
    }
}
