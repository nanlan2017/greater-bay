package com.jheng.bay.util

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter

data class Tuple4<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
)

infix fun <A, B>
        List<A>.join(
        bList: List<B>
): Pair<List<A>, List<B>> =
        Pair(this, bList)

infix fun <A, B>
        Pair<List<A>, List<B>>.on(
        firstID: KProperty<Int?>
): Triple<List<A>, List<B>, KProperty<Int?>> =
        Triple(this.first, this.second, firstID)

infix fun <A, B>
        Triple<List<A>, List<B>, KProperty<Int?>>.eq(
        secondID: KProperty<Int?>
): Tuple4<List<A>, List<B>, KProperty<Int?>, KProperty<Int?>> =
        Tuple4(this.first, this.second, this.third, secondID)

inline infix fun <reified A, reified B, reified P>
        Tuple4<List<A>, List<B>, KProperty<Int?>, KProperty<Int?>>.into(
        target: KMutableProperty<P>
) {
    val (aList, bList, firstID, secondID) = this
    if (aList.isEmpty()) return

    // any order should be ok.
    val idFromA: KProperty<Int?>?
    val idFromB: KProperty<Int?>?
    if (firstID.javaGetter!!.declaringClass.isAssignableFrom(A::class.java)
            && secondID.javaGetter!!.declaringClass.isAssignableFrom(B::class.java)) {
        idFromA = firstID
        idFromB = secondID
    } else if (firstID.javaGetter!!.declaringClass.isAssignableFrom(B::class.java)
            && secondID.javaGetter!!.declaringClass.isAssignableFrom(A::class.java)) {
        idFromA = secondID
        idFromB = firstID
    } else {
        error("JoinUtil: require one id from ${A::class.simpleName}, another id from ${B::class.simpleName}.")
    }
    if (!target.javaGetter!!.declaringClass.isAssignableFrom(A::class.java)) {
        error("JoinUtil: You should be going to join into ${A::class.simpleName}.")
    }

    if (P::class.isSubclassOf(Iterable::class)) {
        val map = bList.groupBy { idFromB.getter.call(it) }
        aList.forEach { target.setter.call(it, map[idFromA.getter.call(it)] ?: emptyList<B>()) }
    } else {
        val map = bList.associateBy { idFromB.getter.call(it) }
        aList.forEach { target.setter.call(it, map[idFromA.getter.call(it)]) }
    }
}


