/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone.client

object RcloneConfigFile {
    private val SECTION_REGEX = Regex("""^\s*\[([^\]]+)\]\s*$""")

    fun parseRemotes(content: String): List<String> {
        val remotes = mutableListOf<String>()
        for (line in content.lineSequence()) {
            val trimmed = line.trim()
            if (trimmed.startsWith('#') || trimmed.startsWith(';')) {
                continue
            }
            val match = SECTION_REGEX.matchEntire(trimmed)
            if (match != null) {
                val name = match.groupValues[1].trim()
                if (name.isNotEmpty() && !remotes.contains(name)) {
                    remotes += name
                }
            }
        }
        return remotes
    }
}
