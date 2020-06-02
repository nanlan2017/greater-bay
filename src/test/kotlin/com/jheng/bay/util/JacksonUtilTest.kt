package com.jheng.bay.util

import com.jheng.bay.base.model.BaseModel
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import java.time.Instant

@Suppress("UNCHECKED_CAST")
class JacksonUtilTest {

    class Model {
        var is_foo: Boolean = false
        var isFoo: Boolean = false
        var bar: Boolean = false

        @JsonProperty("qux")
        var baz: Boolean = false

        override fun toString(): String {
            return "Model(is_foo=$is_foo, bar=$bar, baz=$baz)"
        }
    }

    @Nested
    inner class FixBooleanPropertyNamingStrategyTest {
        @Test
        @DisplayName("serialize")
        fun test100() {
            val model = Model().apply {
                is_foo = true
                isFoo = true
                bar = true
                baz = true
            }
            val json = JacksonUtil.objectMapper.writeValueAsString(model)
            JsonUtil.extract<Boolean>(json, Model::is_foo.name)
            JsonUtil.extract<Boolean>(json, Model::isFoo.name)
            JsonUtil.extract<Boolean>(json, Model::bar.name)
            JsonUtil.extract<Boolean>(json, Model::baz.name)
        }

        @Test
        @DisplayName("deserialize")
        fun test200() {
            val json = """
            {
                "is_foo": true,
                "isFoo": true,
                "bar": true,
                "baz": true,
                "qux": true
            }
        """.trimIndent()
            val res = JacksonUtil.objectMapper.readValue<Model>(json)
            assertThat(res.is_foo).isTrue()
            assertThat(res.isFoo).isTrue()
            assertThat(res.bar).isTrue()
            assertThat(res.baz).isTrue()
        }
    }

    data class ConvertValueFixture(
            @JsonProperty("bar")
            val foo: String
    )

    @Nested
    inner class ConvertValueTest {
        @Test
        @DisplayName("convert to/from map should consider jackson annotation")
        fun test100() {
            val res3 = JacksonUtil.objectMapper
                    .convertValue(mapOf("foo" to "foo", "bar" to "bar"), ConvertValueFixture::class.java)
            assertThat(res3.foo).isEqualTo("bar")
        }
    }

    class ApplyJacksonAnnotationTestFixture100 : BaseModel() {
        @JsonIgnore
        var foo: String = ""
        var bar: String = ""
    }

    class ApplyJacksonAnnotationTestFixture110 : BaseModel() {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        var foo: String = ""
        var bar: String = ""
    }

    class ApplyJacksonAnnotationTestFixture200 : BaseModel() {
        @JsonProperty("bar")
        var foo: String = ""
    }

    class ApplyJacksonAnnotationTestFixture300 : BaseModel() {
        @JsonProperty("bar")
        var foo: String = ""

        @JsonIgnore
        var bar: String = ""
    }

    class ApplyJacksonAnnotationTestFixture1000 : BaseModel() {
        var nest: ApplyJacksonAnnotationTestFixture100? = null
        var nest_list: List<ApplyJacksonAnnotationTestFixture100> = emptyList()
        var nest_simple: List<Int> = emptyList()
        var nest_map: Map<String, Any?> = emptyMap()
    }

    @Nested
    inner class ApplyJacksonAnnotationTest {
        @Test
        @DisplayName("should ignore property with JsonIgnore")
        fun test100() {
            val res = JacksonUtil.apply_jackson_annotation(
                    mapOf(
                            "foo" to "foo",
                            "bar" to "bar"
                    ),
                    ApplyJacksonAnnotationTestFixture100::class
            )
            println(res)
            assertThat(res).containsExactlyEntriesOf(mapOf("bar" to "bar"))
        }

        @Test
        @DisplayName("should ignore property with readonly JsonProperty")
        fun test110() {
            val res = JacksonUtil.apply_jackson_annotation(
                    mapOf(
                            "foo" to "foo",
                            "bar" to "bar"
                    ),
                    ApplyJacksonAnnotationTestFixture110::class
            )
            println(res)
            assertThat(res).containsExactlyEntriesOf(mapOf("bar" to "bar"))
        }

        @Test
        @DisplayName("should rename property")
        fun test200() {
            val res = JacksonUtil.apply_jackson_annotation(
                    mapOf(
                            "foo" to "foo",
                            "bar" to "bar"
                    ),
                    ApplyJacksonAnnotationTestFixture200::class
            )
            println(res)
            assertThat(res).containsExactlyEntriesOf(mapOf("foo" to "bar"))
        }

        @Test
        @DisplayName("combining json property and json ignore")
        fun test300() {
            val res = JacksonUtil.apply_jackson_annotation(
                    mapOf(
                            "foo" to "foo",
                            "bar" to "bar"
                    ),
                    ApplyJacksonAnnotationTestFixture300::class
            )
            println(res)
            assertThat(res).containsExactlyEntriesOf(mapOf("foo" to "bar"))
        }

        @Test
        @DisplayName("nest case 1")
        fun test1000() {
            val res = JacksonUtil.apply_jackson_annotation(
                    mapOf("nest" to mapOf(
                            "foo" to "foo",
                            "bar" to "bar"
                    )),
                    ApplyJacksonAnnotationTestFixture1000::class
            )
            println(res)
            val map = res["nest"] as Map<String, Any?>
            assertThat(map).containsExactlyEntriesOf(mapOf("bar" to "bar"))
        }

        @Test
        @DisplayName("nest case 2")
        fun test1100() {
            val res = JacksonUtil.apply_jackson_annotation(
                    mapOf("nest_list" to listOf(mapOf(
                            "foo" to "foo",
                            "bar" to "bar"
                    ))),
                    ApplyJacksonAnnotationTestFixture1000::class
            )
            println(res)
            val map = (res["nest_list"] as List<Map<String, Any?>>).first()
            assertThat(map).containsExactlyEntriesOf(mapOf("bar" to "bar"))
        }

        @Test
        @DisplayName("nest case 3, collection of simple type")
        fun test1200() {
            val res = JacksonUtil.apply_jackson_annotation(
                    mapOf(ApplyJacksonAnnotationTestFixture1000::nest_simple.name to listOf(1, 2, 3)),
                    ApplyJacksonAnnotationTestFixture1000::class
            )
            assertThat(res[ApplyJacksonAnnotationTestFixture1000::nest_simple.name]).isEqualTo(listOf(1, 2, 3))
        }

        @Test
        @DisplayName("nest case 4, map")
        fun test1300() {
            val map = mapOf("foo" to "bar")
            val res = JacksonUtil.apply_jackson_annotation(
                    mapOf(ApplyJacksonAnnotationTestFixture1000::nest_map.name to map),
                    ApplyJacksonAnnotationTestFixture1000::class
            )
            assertThat(res[ApplyJacksonAnnotationTestFixture1000::nest_map.name]).isEqualTo(map)
        }
    }

    data class DatetimeTestFixture(
            val iso: Instant
    )

    @Nested
    inner class DatetimeTest {
        @Test
        fun test100() {
            val json = """
                { "iso": "2020-04-19T03:00:00Z" }
            """.trimIndent()
            val res = JacksonUtil.objectMapper.readValue<DatetimeTestFixture>(json)
            println(res)
            println(res.iso.epochSecond)
            println(Timestamp.from(res.iso))
            assertThat(res).isNotNull
        }
    }

    @Nested
    inner class BasicTypeTest {
        @Test
        @DisplayName("int can be desearialized as BigDecimal")
        fun test100() {
            val jacksonObjectMapper = JacksonUtil.objectMapper
            val secondObjectMapper = jacksonObjectMapper.copy()
                    .configure(MapperFeature.USE_ANNOTATIONS, false)!!
                    .registerKotlinModule()
            val cloneObjectMapper = jacksonObjectMapper.copy()
                    .configure(MapperFeature.USE_ANNOTATIONS, false)
                    .setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS)!!.registerKotlinModule()
        }
    }

    data class ImmutableDataClass(
            val foo: String,
            val bar: Int = 1
    )

    @Nested
    inner class ConvertTest {
        @Test
        @DisplayName("basic case")
        fun test100() {
            val map = mapOf(
                    "foo" to "foo"
//                    "bar" to 1
            )
            val res = JacksonUtil.convert<ImmutableDataClass>(map)
            assertThat(res.foo).isEqualTo("foo")
        }
    }

}
