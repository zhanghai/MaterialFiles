/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.terminal

import java8.nio.file.Paths
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

class TerminalTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            try {
                FileSystemProvider.installDefaultProvider(LinuxFileSystemProvider)
            } catch (e: Exception) {
                // Ignored if already installed.
            }
        }
    }

    @Test
    fun resolvePath_linuxPath_standardPath() {
        val path = Paths.get("/sdcard/Download")
        val resolved = Terminal.resolvePath(path)
        assertEquals("/sdcard/Download", resolved)
    }

    @Test
    fun resolvePath_linuxPath_termuxStorage() {
        val path = Paths.get("/data/data/com.termux/files/home")
        val resolved = Terminal.resolvePath(path)
        assertEquals("/data/data/com.termux/files/home", resolved)
    }

    @Test
    fun resolvePath_linuxPath_termuxUser0Normalized() {
        val path = Paths.get("/data/user/0/com.termux/files/home/project")
        val resolved = Terminal.resolvePath(path)
        assertEquals("/data/data/com.termux/files/home/project", resolved)
    }
}
