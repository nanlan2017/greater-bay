package com.jheng.bay.util

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

object JacksonUtil {

    fun configure(objectMapper: ObjectMapper) = objectMapper
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//            .setSerializationInclusion(JsonInclude.Include.NON_NULL) // ignore properties which have null value
            .registerModule(JavaTimeModule())
            .registerKotlinModule()
            .setPropertyNamingStrategy(FixBooleanPropertyNamingStrategy)!!

    val objectMapper = configure(ObjectMapper())

    val prettyWriter = objectMapper.writerWithDefaultPrettyPrinter()!!

    fun <T : Any> convert(
            source: Any,
            model_class: KClass<T>
    ): T {
        return objectMapper.convertValue(source, model_class.java)
    }

    inline fun <reified T : Any> convert(source: Any): T {
        return convert(source, T::class)
    }

    /**
     * Handle all the obscure bugs(relates to jackson annotation) here.
     * At present, only the following annotation are considered
     * 1. JsonIgnore
     * 2. JsonProperty
     * Note: this method doesn't handle getter/setter annotation(e.g. @get:JsonIgnore)
     */
    @Suppress("UNCHECKED_CAST")
    fun apply_jackson_annotation(
            map: Map<String, Any?>,
            model_class: KClass<*>
    ): Map<String, Any?> {
        // we simply ignore Map type
        // which means if a use define class is subclass of Map
        // this method won't take effects
        if (model_class.isSubclassOf(Map::class)) return map

        val res = mutableMapOf<String, Any?>()
        model_class.memberProperties.forEach { prop ->
            // json ignore
            if (findAnnotationOnProp(prop, JsonIgnore::class) != null) return@forEach

            // json property
            val jsonProperty = findAnnotationOnProp(prop, JsonProperty::class)
            if (jsonProperty?.access === JsonProperty.Access.READ_ONLY) return@forEach
            val json_prop_name = if (jsonProperty != null && jsonProperty.value.isNotEmpty()) {
                jsonProperty.value
            } else {
                prop.name
            }

            if (!map.containsKey(json_prop_name)) return@forEach

            res[prop.name] = map[json_prop_name]
            when (map[json_prop_name]) {
                // nest obj
                is Map<*, *> -> {
                    val clazz = findPropClass(prop) ?: return@forEach
                    // overwrite res[prop.name]
                    res[prop.name] = apply_jackson_annotation(
                            map[json_prop_name] as Map<String, Any?>,
                            clazz
                    )
                }
                // nest array
                is List<*> -> {
                    val clazz = findPropElementClass(prop) ?: return@forEach
                    val list = map[json_prop_name] as List<Any>
                    if (list.isEmpty() || list.first() !is Map<*, *>) return@forEach
                    // overwrite res[prop.name]
                    res[prop.name] = list.map {
                        apply_jackson_annotation(
                                it as Map<String, Any?>,
                                clazz
                        )
                    }
                }
            }
        }
        return res
    }

    private fun <T : Annotation> findAnnotationOnProp(
            prop: KProperty<*>,
            clazz: KClass<T>
    ): T? {
        val res = prop.annotations.find { it.annotationClass == clazz }
                ?: prop.javaField?.annotations?.find { it.annotationClass == clazz }
        @Suppress("UNCHECKED_CAST")
        return res as T?
    }

    private fun findPropClass(prop: KProperty<*>): KClass<*>? {
        return try {
            prop.returnType.jvmErasure
        } catch (e: KotlinReflectionInternalError) {
            null
        }
    }

    private fun findPropElementClass(prop: KProperty<*>): KClass<*>? {
        return try {
            prop.returnType.arguments.firstOrNull()?.type?.jvmErasure
        } catch (e: KotlinReflectionInternalError) {
            null
        }
    }


    object FixBooleanPropertyNamingStrategy : PropertyNamingStrategy() {

        private val logger = LoggerFactory.getLogger(FixBooleanPropertyNamingStrategy::class.java)

        override fun nameForGetterMethod(
                config: MapperConfig<*>?,
                method: AnnotatedMethod,
                defaultName: String?
        ): String {
            if (method.hasReturnType()
                    && (method.rawReturnType == Boolean::class || method.rawReturnType == Boolean::class.javaPrimitiveType)
                    && method.name.startsWith("is")
            ) {
                return method.name
            }
            return super.nameForGetterMethod(config, method, defaultName)
        }

        /**
         * if
         * 1. method name starts with set, e.g. setFoo
         * 2. return type is Boolean or boolean(jvm primitive type)
         * 3. the declaring class contains a method named isFoo
         * we return isFoo as the property name in json
         *
         * refer to https://stackoverflow.com/a/58999529
         */
        override fun nameForSetterMethod(
                config: MapperConfig<*>?,
                method: AnnotatedMethod,
                defaultName: String?
        ): String {
            // logger.trace { "${method.name} $defaultName " + super.nameForSetterMethod(config, method, defaultName) }
            if (method.parameterCount == 1
                    && (method.getRawParameterType(0) == Boolean::class || method.getRawParameterType(0) == Boolean::class.javaPrimitiveType)
                    && method.name.startsWith("set")
            ) {
                try {
                    val potential_name = "is${method.name.substring(3)}"
                    method.declaringClass.getMethod(potential_name)
                    // logger.trace { potential_name }
                    return potential_name
                } catch (e: NoSuchMethodException) {
                    // logger.trace { e }
                }
            }
            return super.nameForSetterMethod(config, method, defaultName)
        }
    }

}
