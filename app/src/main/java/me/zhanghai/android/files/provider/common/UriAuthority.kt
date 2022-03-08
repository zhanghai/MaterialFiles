package me.zhanghai.android.files.provider.common

import java.net.URI
import java.net.URISyntaxException

data class UriAuthority(
    val userInfo: String?,
    val host: String,
    val port: Int?
) {
    fun encode(): String = toUri().rawAuthority ?: ""

    override fun toString(): String = toUri().authority ?: ""

    private fun toUri(): URI {
        return try {
            // HACK: An empty host/authority requires a path, so use "/" as path here.
            URI(null, userInfo, host, port ?: -1, "/", null, null)
        } catch (e: URISyntaxException) {
            throw IllegalArgumentException(e)
        }
    }

    companion object {
        val EMPTY = UriAuthority(null, "", null)
    }
}
