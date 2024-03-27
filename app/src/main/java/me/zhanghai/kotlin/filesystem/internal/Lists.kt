package me.zhanghai.kotlin.filesystem.internal

import kotlin.math.min

internal fun <T : Comparable<T>> List<T>.compareTo(other: List<T>): Int {
    val size = size
    val otherSize = other.size
    val commonSize = min(size, otherSize)
    for (i in 0 ..< commonSize) {
        this[i].compareTo(other[i]).let {
            if (it != 0) {
                return it
            }
        }
    }
    return size.compareTo(otherSize)
}

internal fun <T> List<T>.startsWith(other: List<T>): Boolean {
    val size = size
    val otherSize = other.size
    return when {
        size == otherSize -> this == other
        size > otherSize -> subList(0, otherSize) == other
        else -> false
    }
}

internal fun <T> List<T>.endsWith(other: List<T>): Boolean {
    val size = size
    val otherSize = other.size
    return when {
        size == otherSize -> this == other
        size > otherSize -> subList(size - otherSize, size) == other
        else -> false
    }
}
