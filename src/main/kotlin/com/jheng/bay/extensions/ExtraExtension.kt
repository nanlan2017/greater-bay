package com.jheng.bay.extensions

/**
 * e.g. "foo,foo.bar,bar".to_extra() => setOf("foo", "foo.bar", "bar")
 */
fun String.to_extra(): Set<String> {
    return this.split(',')
            .filter { it.isNotEmpty() }
            .toSet()
}

//region for a.b.c
/**
 * e.g. setOf("foo.foo", "foo.bar", "bar").remove_prefix("foo") => setOf("foo", "bar")
 */
fun Iterable<String>.remove_prefix(prefix: String): Set<String> {
    return this.map {
        if (prefix.isBlank()) {
            it
        } else {
            it.substringAfter("$prefix.", "")
        }
    }.filter { it.isNotEmpty() }.toSet()
}

fun Iterable<String>.add_prefix(prefix: String): Array<String> {
    return this.map { "$prefix.$it" }.toTypedArray()
}

//endregion
