/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone.client

import org.junit.Assert.assertEquals
import org.junit.Test

class RcloneConfigFileTest {
    @Test
    fun parseRemotes_userExample_returnsRemotes() {
        val config = """
            [gdrive]
            type = drive
            client_id =
            client_secret =
            scope = drive
            metadata_owner = off
            token = 
            team_drive = 

            [onedrive]
            type = onedrive
            token =
            drive_id =
            drive_type = documentLibrary
        """.trimIndent()

        val remotes = RcloneConfigFile.parseRemotes(config)
        assertEquals(listOf("gdrive", "onedrive"), remotes)
    }

    @Test
    fun parseRemotes_withCommentsAndSpacing_returnsValidRemotes() {
        val config = """
            # Global comment
            ; Another comment
            
            [remote1]
            type = s3
            
            # comment in between
            [remote2]
            type = b2
            
            [remote1] # duplicate section header
            key = val
        """.trimIndent()

        val remotes = RcloneConfigFile.parseRemotes(config)
        assertEquals(listOf("remote1", "remote2"), remotes)
    }

    @Test
    fun parseRemotes_empty_returnsEmptyList() {
        assertEquals(emptyList<String>(), RcloneConfigFile.parseRemotes(""))
        assertEquals(emptyList<String>(), RcloneConfigFile.parseRemotes("   \n\n# nothing"))
    }
}
