package com.jheng.bay.extensions

import java.time.Instant
import java.time.ZoneOffset

fun Instant.gt(otherInstant: Instant) = this.isAfter(otherInstant)
fun Instant.gte(otherInstant: Instant) = !this.isBefore(otherInstant)
fun Instant.lt(otherInstant: Instant) = this.isBefore(otherInstant)
fun Instant.lte(otherInstant: Instant) = !this.isAfter(otherInstant)


infix fun Pair<Instant, Instant>.none_intersection_with(range: Pair<Instant, Instant>): Boolean {
    val rangeA = if (this.first.lte(this.second)) this else this.second to this.first
    val rangeB = if (range.first.lte(range.second)) range else range.second to range.first
    return rangeB.first.gte(rangeA.second) || rangeB.second.lte(rangeA.first)
}

infix fun Pair<Instant, Instant>.has_intersection_with(range: Pair<Instant, Instant>) =
        !(this none_intersection_with range)

val Collection<Pair<Instant, Instant>>.has_intersection: Boolean
    get() {
        this.forEachIndexed { index, range ->
            for (another in this.drop(index + 1)) {
                if (range has_intersection_with another)
                    return true
            }
        }
        return false
    }

val Collection<Pair<Instant, Instant>>.none_intersection: Boolean
    get() = !this.has_intersection


/**
 * 02:03' --> 02:00'
 * 01:58' --> 02:00'
 */
fun Instant.regulate_to_nearest_minute(): Instant {
    val dt = this.atOffset(ZoneOffset.UTC)
    return if (dt.second > 30)
        dt.withSecond(0).withNano(0).plusMinutes(1).toInstant()
    else
        dt.withSecond(0).withNano(0).toInstant()
}