/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.CopyOption
import java8.nio.file.LinkOption
import java8.nio.file.StandardCopyOption

class CopyOptions(
    val replaceExisting: Boolean,
    val copyAttributes: Boolean,
    val atomicMove: Boolean,
    val noFollowLinks: Boolean,
    val progressIntervalMillis: Long,
    val progressListener: ((Long) -> Unit)?
) {
    fun toArray(): Array<CopyOption> {
        val options = mutableListOf<CopyOption>()
        if (replaceExisting) {
            options += StandardCopyOption.REPLACE_EXISTING
        }
        if (copyAttributes) {
            options += StandardCopyOption.COPY_ATTRIBUTES
        }
        if (atomicMove) {
            options += StandardCopyOption.ATOMIC_MOVE
        }
        if (noFollowLinks) {
            options += LinkOption.NOFOLLOW_LINKS
        }
        if (progressListener != null) {
            options += ProgressCopyOption(progressIntervalMillis, progressListener)
        }
        return options.toTypedArray()
    }
}

fun Array<out CopyOption>.toCopyOptions(): CopyOptions {
    var replaceExisting = false
    var copyAttributes = false
    var atomicMove = false
    var noFollowLinks = false
    var progressIntervalMillis = 0L
    var progressListener: ((Long) -> Unit)? = null
    for (option in this) {
        when {
            option is StandardCopyOption ->
                when (option) {
                    StandardCopyOption.REPLACE_EXISTING -> replaceExisting = true
                    StandardCopyOption.COPY_ATTRIBUTES -> copyAttributes = true
                    StandardCopyOption.ATOMIC_MOVE -> atomicMove = true
                    else -> throw UnsupportedOperationException(option.toString())
                }
            option === LinkOption.NOFOLLOW_LINKS -> noFollowLinks = true
            option is ProgressCopyOption -> {
                progressIntervalMillis = option.intervalMillis
                progressListener = option.listener
            }
            else -> {
                throw UnsupportedOperationException(option.toString())
            }
        }
    }
    return CopyOptions(
        replaceExisting, copyAttributes, atomicMove, noFollowLinks, progressIntervalMillis,
        progressListener
    )
}
