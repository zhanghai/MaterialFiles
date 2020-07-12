/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall

/**
 * @see android.system.StructStat
 */
class StructStat(
    val st_dev: Long, /*dev_t*/
    val st_ino: Long, /*ino_t*/
    val st_mode: Int, /*mode_t*/
    val st_nlink: Long, /*nlink_t*/
    val st_uid: Int, /*uid_t*/
    val st_gid: Int, /*gid_t*/
    val st_rdev: Long, /*dev_t*/
    val st_size: Long, /*off_t*/
    val st_blksize: Long, /*blksize_t*/
    val st_blocks: Long, /*blkcnt_t*/
    val st_atim: StructTimespec,
    val st_mtim: StructTimespec,
    val st_ctim: StructTimespec
) {
    val st_atime: Long /*time_t*/
        get() = st_atim.tv_sec
    val st_mtime: Long /*time_t*/
        get() = st_mtim.tv_sec
    val st_ctime: Long /*time_t*/
        get() = st_ctim.tv_sec
}
