/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

class RclonePathTest {
    @Test
    fun getPath_rootUri_parsesCorrectly() {
        val uri = URI.create("rclone://gdrive/")
        val path = RcloneFileSystemProvider.getPath(uri) as RclonePath
        assertEquals("gdrive", path.remoteName)
        assertEquals("", path.remotePath)
        assertTrue(path.isAbsolute)
    }

    @Test
    fun getPath_subUri_parsesCorrectly() {
        val uri = URI.create("rclone://onedrive/Documents/report.pdf")
        val path = RcloneFileSystemProvider.getPath(uri) as RclonePath
        assertEquals("onedrive", path.remoteName)
        assertEquals("Documents/report.pdf", path.remotePath)
        assertEquals("report.pdf", path.fileName.toString())
        assertEquals("Documents", path.parent?.fileName.toString())
    }

    @Test
    fun path_resolve_constructsExpectedPath() {
        val root = RcloneAuthority("gdrive").createRcloneRootPath() as RclonePath
        val child = root.resolve("photos").resolve("pic.jpg") as RclonePath
        assertEquals("gdrive", child.remoteName)
        assertEquals("photos/pic.jpg", child.remotePath)
    }
}
