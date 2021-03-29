/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import java.util.EnumSet

inline fun <reified T : Enum<T>> enumSetOf(): EnumSet<T> = EnumSet.noneOf(T::class.java)

fun <T : Enum<T>> enumSetOf(element: T): EnumSet<T> = EnumSet.of(element)

fun <T : Enum<T>> enumSetOf(element1: T, element2: T): EnumSet<T> = EnumSet.of(element1, element2)

fun <T : Enum<T>> enumSetOf(element1: T, element2: T, element3: T): EnumSet<T> =
    EnumSet.of(element1, element2, element3)

fun <T : Enum<T>> enumSetOf(element1: T, element2: T, element3: T, element4: T): EnumSet<T> =
    EnumSet.of(element1, element2, element3, element4)

fun <T : Enum<T>> enumSetOf(
    element1: T,
    element2: T,
    element3: T,
    element4: T,
    element5: T
): EnumSet<T> = EnumSet.of(element1, element2, element3, element4, element5)

fun <T : Enum<T>> enumSetOf(first: T, vararg rest: T): EnumSet<T> = EnumSet.of(first, *rest)

fun <T> Iterable<T>.toLinkedSet(): LinkedHashSet<T> = toCollection(LinkedHashSet())

fun <T : Enum<T>> Collection<T>.toEnumSet(): EnumSet<T> = EnumSet.copyOf(this)

fun <T: Collection<*>> T.takeIfNotEmpty(): T? = if (isNotEmpty()) this else null

fun <T> MutableCollection<T>.removeFirst(): T {
    val iterator = iterator()
    val element = iterator.next()
    iterator.remove()
    return element
}

fun <K, V> MutableMap<K, V>.removeFirst(): Map.Entry<K, V> {
    val iterator = iterator()
    val element = iterator.next()
    iterator.remove()
    return element
}

fun <T> MutableCollection<T>.removeFirst(predicate: (T) -> Boolean): T? {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (predicate(element)) {
            iterator.remove()
            return element
        }
    }
    return null
}

fun <K, V> MutableMap<K, V>.removeFirst(predicate: (Map.Entry<K, V>) -> Boolean): Map.Entry<K, V>? =
    entries.removeFirst(predicate)
