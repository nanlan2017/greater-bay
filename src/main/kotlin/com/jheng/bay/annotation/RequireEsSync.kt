package com.jheng.bay.annotation

/**
 * Ensure the corresponding id consumer is added in `EsSyncAdvice` before using.
 *
 * Useful when id can be acquired from method parameter after method executed.
 *
 * By default , if first parameter is Int, it will take it as id,
 * otherwise it will take `id` property from first parameter.(if it is collection, take id from each element of collection)
 *
 *
 * @param es_index must provide.
 * @param which_arg FIRST/ SECOND/ THIRD. Its type should be Int/ model/ Collection<>.
 * @param id_field field name in this model corresponding to es doc id.
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireEsSync(
        val es_index: String = "",
        val which_arg: Int = 0,
        val id_field: String = "id"
)
