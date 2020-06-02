package com.jheng.bay.util

import com.jheng.bay.base.model.BaseModel
import com.jheng.bay.extensions.id
import kotlin.reflect.KClass

object CrudUtils {

    data class GroupedModel<T : BaseModel>(
            val to_save: List<T>,
            val to_update: List<T>,
            val to_remove: Set<Int>
    )

    data class GroupedPatch<T : BaseModel>(
            val to_save: List<T>,
            val to_patch: List<Map<String, Any?>>,
            val to_remove: Set<Int>
    )

    fun <T : BaseModel> classify_model(
            old_list: List<T>,
            new_list: List<T>
    ): GroupedModel<T> {
        val (to_save, to_update) = new_list.partition { it.is_new }
        val new_ids = to_update.map { it.id }.toSet()
        val to_remove = old_list.filter { it.id !in new_ids }
        return GroupedModel(
                to_save = to_save,
                to_update = to_update,
                to_remove = to_remove.map { it.id }.toSet()
        )
    }

    fun <T : BaseModel> classify_patch(
            old_list: List<T>,
            new_list: List<Map<String, Any?>>,
            model_class: KClass<T>
    ): GroupedPatch<T> {
        val new_map = new_list.associateBy { it.id }
        val new_ids = new_map.keys
        val (to_patch, to_remove) = old_list
                .map { it.id }
                .partition { it in new_ids }
        val to_save = new_list
                .filter { it.id == null || it.id == 0 }
                .map { JacksonUtil.objectMapper.convertValue(it, model_class.java) }
        return GroupedPatch<T>(
                to_save = to_save,
                to_patch = to_patch.mapNotNull { new_map[it] },
                to_remove = to_remove.toSet()
        )
    }

}
