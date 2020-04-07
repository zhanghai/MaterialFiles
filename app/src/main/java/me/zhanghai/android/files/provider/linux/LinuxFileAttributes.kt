/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.PosixFileAttributes
import me.zhanghai.android.files.provider.common.PosixFileMode
import me.zhanghai.android.files.provider.common.PosixFileModeBit
import me.zhanghai.android.files.provider.common.PosixFileType
import me.zhanghai.android.files.provider.common.PosixGroup
import me.zhanghai.android.files.provider.common.PosixUser
import me.zhanghai.android.files.provider.linux.syscall.StructStat
import me.zhanghai.android.files.util.readParcelable
import org.threeten.bp.Instant

internal class LinuxFileAttributes constructor(
    private val stat: StructStat,
    private val owner: PosixUser,
    private val group: PosixGroup,
    private val seLinuxContext: ByteString?
) : Parcelable, PosixFileAttributes {
    override fun lastModifiedTime(): FileTime =
        FileTime.from(Instant.ofEpochSecond(stat.st_mtim.tv_sec, stat.st_mtim.tv_nsec))

    override fun lastAccessTime(): FileTime =
        FileTime.from(Instant.ofEpochSecond(stat.st_atim.tv_sec, stat.st_atim.tv_nsec))

    override fun creationTime(): FileTime = lastModifiedTime()

    override fun type(): PosixFileType = PosixFileType.fromMode(stat.st_mode)

    override fun size(): Long = stat.st_size

    override fun fileKey(): Parcelable = LinuxFileKey(stat.st_dev, stat.st_ino)

    override fun owner(): PosixUser = owner

    override fun group(): PosixGroup = group

    override fun mode(): Set<PosixFileModeBit> = PosixFileMode.fromInt(stat.st_mode)

    override fun seLinuxContext(): ByteString? = seLinuxContext

    private constructor(source: Parcel) : this(
        source.readParcelable()!!, source.readParcelable()!!, source.readParcelable()!!,
        source.readParcelable()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(stat, flags)
        dest.writeParcelable(owner, flags)
        dest.writeParcelable(group, flags)
        dest.writeParcelable(seLinuxContext, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LinuxFileAttributes> {
            override fun createFromParcel(source: Parcel): LinuxFileAttributes =
                LinuxFileAttributes(source)

            override fun newArray(size: Int): Array<LinuxFileAttributes?> = arrayOfNulls(size)
        }
    }
}
