package me.zhanghai.kotlin.filesystem.internal

import kotlin.jvm.JvmInline

@JvmInline
internal value class IntPair private constructor(private val value: Long) {
    constructor(first: Int, second: Int) : this((first.toLong() shl 32) or second.toLong())

    val first: Int
        inline get() = (value ushr 32).toInt()

    val second: Int
        inline get() = value.toInt()

    inline operator fun component1(): Int = first

    inline operator fun component2(): Int = second
}
