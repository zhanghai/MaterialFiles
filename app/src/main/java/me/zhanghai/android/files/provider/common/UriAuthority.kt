package me.zhanghai.android.files.provider.common

import java.net.URI
import java.net.URISyntaxException

data class UriAuthority(
    val userInfo: String?,
    val host: String,
    val port: Int?
) {
    private fun toUri(): URI =
        try {
            URI(null, userInfo, host, port ?: -1, null, null, null)
        } catch (e: URISyntaxException) {
            throw IllegalArgumentException(e)
        }

    fun encode(): String = toUri().rawAuthority

    override fun toString(): String = toUri().authority

    companion object {
        val EMPTY = UriAuthority(null, "", null)
    }
}
