/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.system.OsConstants
import java8.nio.file.attribute.FileAttribute
import java8.nio.file.attribute.PosixFilePermission
import me.zhanghai.android.files.util.enumSetOf
import me.zhanghai.android.files.util.hasBits

enum class PosixFileModeBit {
    SET_USER_ID,
    SET_GROUP_ID,
    STICKY,
    OWNER_READ,
    OWNER_WRITE,
    OWNER_EXECUTE,
    GROUP_READ,
    GROUP_WRITE,
    GROUP_EXECUTE,
    OTHERS_READ,
    OTHERS_WRITE,
    OTHERS_EXECUTE
}

object PosixFileMode {
    val CREATE_FILE_DEFAULT = fromInt(
        OsConstants.S_IRUSR or OsConstants.S_IWUSR or OsConstants.S_IRGRP or OsConstants.S_IWGRP
            or OsConstants.S_IROTH or OsConstants.S_IWOTH
    )

    val CREATE_DIRECTORY_DEFAULT = fromInt(
        OsConstants.S_IRWXU or OsConstants.S_IRWXG or OsConstants.S_IRWXO
    )

    fun fromAttribute(attribute: FileAttribute<*>): Set<PosixFileModeBit> {
        if (attribute.name() != PosixFileModeAttribute.NAME) {
            throw UnsupportedOperationException(attribute.name())
        }
        val value = attribute.value()
        @Suppress("UNCHECKED_CAST")
        return value as? Set<PosixFileModeBit>
            ?: throw UnsupportedOperationException(value.toString())
    }

    fun fromAttributes(attributes: Array<out FileAttribute<*>>): Set<PosixFileModeBit>? {
        var mode: Set<PosixFileModeBit>? = null
        for (attribute in attributes) {
            mode = fromAttribute(attribute)
        }
        return mode
    }

    fun fromInt(modeInt: Int): Set<PosixFileModeBit> =
        enumSetOf<PosixFileModeBit>().apply {
            if (modeInt.hasBits(OsConstants.S_ISUID)) {
                this += PosixFileModeBit.SET_USER_ID
            }
            if (modeInt.hasBits(OsConstants.S_ISGID)) {
                this += PosixFileModeBit.SET_GROUP_ID
            }
            if (modeInt.hasBits(OsConstants.S_ISVTX)) {
                this += PosixFileModeBit.STICKY
            }
            if (modeInt.hasBits(OsConstants.S_IRUSR)) {
                this += PosixFileModeBit.OWNER_READ
            }
            if (modeInt.hasBits(OsConstants.S_IWUSR)) {
                this += PosixFileModeBit.OWNER_WRITE
            }
            if (modeInt.hasBits(OsConstants.S_IXUSR)) {
                this += PosixFileModeBit.OWNER_EXECUTE
            }
            if (modeInt.hasBits(OsConstants.S_IRGRP)) {
                this += PosixFileModeBit.GROUP_READ
            }
            if (modeInt.hasBits(OsConstants.S_IWGRP)) {
                this += PosixFileModeBit.GROUP_WRITE
            }
            if (modeInt.hasBits(OsConstants.S_IXGRP)) {
                this += PosixFileModeBit.GROUP_EXECUTE
            }
            if (modeInt.hasBits(OsConstants.S_IROTH)) {
                this += PosixFileModeBit.OTHERS_READ
            }
            if (modeInt.hasBits(OsConstants.S_IWOTH)) {
                this += PosixFileModeBit.OTHERS_WRITE
            }
            if (modeInt.hasBits(OsConstants.S_IXOTH)) {
                this += PosixFileModeBit.OTHERS_EXECUTE
            }
        }
}

fun Set<PosixFilePermission>.toMode(): Set<PosixFileModeBit> =
    enumSetOf<PosixFileModeBit>().apply {
        for (permission in this@toMode) {
            this += when (permission) {
                PosixFilePermission.OWNER_READ -> PosixFileModeBit.OWNER_READ
                PosixFilePermission.OWNER_WRITE -> PosixFileModeBit.OWNER_WRITE
                PosixFilePermission.OWNER_EXECUTE -> PosixFileModeBit.OWNER_EXECUTE
                PosixFilePermission.GROUP_READ -> PosixFileModeBit.GROUP_READ
                PosixFilePermission.GROUP_WRITE -> PosixFileModeBit.GROUP_WRITE
                PosixFilePermission.GROUP_EXECUTE -> PosixFileModeBit.GROUP_EXECUTE
                PosixFilePermission.OTHERS_READ -> PosixFileModeBit.OTHERS_READ
                PosixFilePermission.OTHERS_WRITE -> PosixFileModeBit.OTHERS_WRITE
                PosixFilePermission.OTHERS_EXECUTE -> PosixFileModeBit.OTHERS_EXECUTE
                else -> throw UnsupportedOperationException(permission.toString())
            }
        }
    }

fun Set<PosixFileModeBit>.toAttribute(): FileAttribute<Set<PosixFileModeBit>> =
    PosixFileModeAttribute(this)

private class PosixFileModeAttribute(
    private val mode: Set<PosixFileModeBit>
) : FileAttribute<Set<PosixFileModeBit>> {
    override fun name(): String = NAME

    override fun value(): Set<PosixFileModeBit> = mode

    companion object {
        const val NAME = "posix:mode"
    }
}

fun Set<PosixFileModeBit>.toInt(): Int =
    ((if (contains(PosixFileModeBit.SET_USER_ID)) OsConstants.S_ISUID else 0)
        or (if (contains(PosixFileModeBit.SET_GROUP_ID)) OsConstants.S_ISGID else 0)
        or (if (contains(PosixFileModeBit.STICKY)) OsConstants.S_ISVTX else 0)
        or (if (contains(PosixFileModeBit.OWNER_READ)) OsConstants.S_IRUSR else 0)
        or (if (contains(PosixFileModeBit.OWNER_WRITE)) OsConstants.S_IWUSR else 0)
        or (if (contains(PosixFileModeBit.OWNER_EXECUTE)) OsConstants.S_IXUSR else 0)
        or (if (contains(PosixFileModeBit.GROUP_READ)) OsConstants.S_IRGRP else 0)
        or (if (contains(PosixFileModeBit.GROUP_WRITE)) OsConstants.S_IWGRP else 0)
        or (if (contains(PosixFileModeBit.GROUP_EXECUTE)) OsConstants.S_IXGRP else 0)
        or (if (contains(PosixFileModeBit.OTHERS_READ)) OsConstants.S_IROTH else 0)
        or (if (contains(PosixFileModeBit.OTHERS_WRITE)) OsConstants.S_IWOTH else 0)
        or (if (contains(PosixFileModeBit.OTHERS_EXECUTE)) OsConstants.S_IXOTH else 0))

fun Set<PosixFileModeBit>.toPermissions(): Set<PosixFilePermission> =
    enumSetOf<PosixFilePermission>().apply {
        for (modeBit in this@toPermissions) {
            this += when (modeBit) {
                PosixFileModeBit.OWNER_READ -> PosixFilePermission.OWNER_READ
                PosixFileModeBit.OWNER_WRITE -> PosixFilePermission.OWNER_WRITE
                PosixFileModeBit.OWNER_EXECUTE -> PosixFilePermission.OWNER_EXECUTE
                PosixFileModeBit.GROUP_READ -> PosixFilePermission.GROUP_READ
                PosixFileModeBit.GROUP_WRITE -> PosixFilePermission.GROUP_WRITE
                PosixFileModeBit.GROUP_EXECUTE -> PosixFilePermission.GROUP_EXECUTE
                PosixFileModeBit.OTHERS_READ -> PosixFilePermission.OTHERS_READ
                PosixFileModeBit.OTHERS_WRITE -> PosixFilePermission.OTHERS_WRITE
                PosixFileModeBit.OTHERS_EXECUTE -> PosixFilePermission.OTHERS_EXECUTE
                else -> throw UnsupportedOperationException(modeBit.toString())
            }
        }
    }

fun Set<PosixFileModeBit>.toModeString(): String =
    StringBuilder()
        .append(if (contains(PosixFileModeBit.OWNER_READ)) 'r' else '-')
        .append(if (contains(PosixFileModeBit.OWNER_WRITE)) 'w' else '-')
        .apply {
            val hasSetUserIdBit = contains(PosixFileModeBit.SET_USER_ID)
            append(
                if (contains(PosixFileModeBit.OWNER_EXECUTE)) {
                    if (hasSetUserIdBit) 's' else 'x'
                } else {
                    if (hasSetUserIdBit) 'S' else '-'
                }
            )
        }
        .append(if (contains(PosixFileModeBit.GROUP_READ)) 'r' else '-')
        .append(if (contains(PosixFileModeBit.GROUP_WRITE)) 'w' else '-')
        .apply {
            val hasSetGroupIdBit = contains(PosixFileModeBit.SET_GROUP_ID)
            append(
                if (contains(PosixFileModeBit.GROUP_EXECUTE)) {
                    if (hasSetGroupIdBit) 's' else 'x'
                } else {
                    if (hasSetGroupIdBit) 'S' else '-'
                }
            )
        }
        .append(if (contains(PosixFileModeBit.OTHERS_READ)) 'r' else '-')
        .append(if (contains(PosixFileModeBit.OTHERS_WRITE)) 'w' else '-')
        .apply {
            val hasStickyBit = contains(PosixFileModeBit.STICKY)
            append(
                if (contains(PosixFileModeBit.OTHERS_EXECUTE)) {
                    if (hasStickyBit) 't' else 'x'
                } else {
                    if (hasStickyBit) 'T' else '-'
                }
            )
        }
        .toString()
