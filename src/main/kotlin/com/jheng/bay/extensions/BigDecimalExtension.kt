package com.jheng.bay.extensions

import java.math.BigDecimal

/**
 * Looks like BigDecimal literal
 */
val Int.BD: BigDecimal
    get() = this.toBigDecimal()
val Long.BD: BigDecimal
    get() = this.toBigDecimal()
val Float.BD: BigDecimal
    get() = this.toBigDecimal()
val Double.BD: BigDecimal
    get() = this.toBigDecimal()
val String.BD: BigDecimal
    get() = this.toBigDecimal()

infix fun BigDecimal?.eq(another: BigDecimal?): Boolean {
    if (this == null && another == null) return true
    if (this != null && another != null) return this.compareTo(another) == 0
    return false
}

infix fun BigDecimal?.eq(another: Int?) = this.eq(another?.BD)
infix fun BigDecimal?.eq(another: Double?) = this.eq(another?.BD)

infix fun BigDecimal?.ne(another: BigDecimal?) = !this.eq(another)
infix fun BigDecimal?.ne(another: Int?) = !this.eq(another)
infix fun BigDecimal?.ne(another: Double?) = !this.eq(another)

val Iterable<BigDecimal>.sum: BigDecimal
    get() = this.fold(0.BD, BigDecimal::add)

inline fun <T> Iterable<T>.sumBdBy(selector: (T) -> BigDecimal) = this.map(selector).sum

