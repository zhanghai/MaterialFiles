package me.zhanghai.android.files.provider.common

import java.net.URI
import java.net.URISyntaxException

data class UriAuthority(
    val userInfo: String?,
    val host: String,
    val port: Int?
) {
    fun encode(): String {
        val uri = try {
            // HACK: An empty host/authority requires a path, so use "/" as path here.
            URI(null, userInfo, host, port ?: -1, "/", null, null)
        } catch (e: URISyntaxException) {
            throw IllegalArgumentException(e)
        }
        // URI.getRawAuthority() returns null when authority is empty.
        return uri.rawAuthority ?: ""
    }

    // toString() is called by UI when the URI may not be valid, so build the string manually.
    override fun toString(): String = buildString {
        if (userInfo != null) {
            append(userInfo)
            append('@')
        }
        append(host)
        if (port != null) {
            append(':')
            append(port.toString())
        }
    }

    companion object {
        val EMPTY = UriAuthority(null, "", null)
    }
}
