/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone.client

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RcloneClientTest {
    @Test
    fun buildDownloadUrl_withSpaces_encodesAsPercent20() {
        val url = RcloneClient.buildDownloadUrl(
            "http://127.0.0.1:5572/",
            "onedrive",
            "progetto_gip/00 GIP_GlitchingCats.pdf"
        )
        assertEquals(
            "http://127.0.0.1:5572/%5Bonedrive%3A%5D/progetto_gip/00%20GIP_GlitchingCats.pdf",
            url
        )
    }

    @Test
    fun buildDownloadUrl_withSpecialCharsAndParentheses_encodesProperly() {
        val url = RcloneClient.buildDownloadUrl(
            "http://127.0.0.1:5572/",
            "onedrive",
            "UniBo - preventivo per acquisto materiale(1).msg"
        )
        assertEquals(
            "http://127.0.0.1:5572/%5Bonedrive%3A%5D/UniBo%20-%20preventivo%20per%20acquisto%20materiale%281%29.msg",
            url
        )
    }

    @Test
    fun itemCache_storesAndRetrievesAndInvalidates() {
        val item = RcloneItem(
            path = "docs/file.txt",
            name = "file.txt",
            size = 1234L,
            mimeType = "text/plain",
            modTime = null,
            isDir = false
        )
        RcloneClient.cacheItem("gdrive", "docs/file.txt", item)
        val retrieved = RcloneClient.getCachedItem("gdrive", "docs/file.txt")
        assertNotNull(retrieved)
        assertEquals("file.txt", retrieved?.name)
        assertEquals(1234L, retrieved?.size)

        RcloneClient.invalidateCache("gdrive", "docs/file.txt")
        assertNull(RcloneClient.getCachedItem("gdrive", "docs/file.txt"))
    }
}
