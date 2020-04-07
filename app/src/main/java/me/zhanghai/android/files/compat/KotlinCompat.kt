/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import java.io.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.comparisons.reversed as kotlinReversed
import kotlin.io.use as kotlinUse
import kotlin.use as kotlinUse

// @see https://youtrack.jetbrains.com/issue/KT-35216
@OptIn(ExperimentalContracts::class)
inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return kotlinUse(block)
}

// @see https://youtrack.jetbrains.com/issue/KT-35216
@OptIn(ExperimentalContracts::class)
inline fun <T : Closeable?, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return kotlinUse(block)
}

fun <T> Comparator<T>.reversedCompat(): Comparator<T> = kotlinReversed()
