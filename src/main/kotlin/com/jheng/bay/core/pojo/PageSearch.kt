package com.jheng.bay.core.pojo

import com.jheng.bay.extensions.add_prefix
import com.jheng.bay.extensions.to_extra
import com.jheng.bay.util.Regexes
import java.time.Instant

class PageSearch private constructor(
        private val map: Map<String, Any?>
) : Map<String, Any?> by map {
    companion object {

        fun create(
                vararg pairs: Pair<String, Any?>
        ): PageSearch {
            return create(pairs.toMap())
        }

        fun create(map: Map<String, Any?> = mapOf()): PageSearch {
            if (map.isEmpty()) return PageSearch(emptyMap())
            return PageSearch(map).map_UTC_string_to_Instant()
        }

        /**
         * @deprecated use set.add_prefix(path) instead
         */
        fun withPrefix(
                prefix: String,
                extras: Iterable<String>
        ): Array<String> {
            return extras.add_prefix(prefix)
        }
    }

    private fun map_UTC_string_to_Instant(): PageSearch {
        return PageSearch(this.map.mapValues {
            val value = it.value
            if (value != null && value is String && Regexes.UTC_Time.matches(value))
                Instant.parse(value)
            else
                value
        })
    }

    val extra: Set<String>?
        get() {
            if (!containsKey("extra")) return null
            return this["extra"]?.toString()?.to_extra()
        }
}


