package me.zhanghai.android.files.storage

import java.net.URI
import java.net.URISyntaxException
import kotlin.reflect.KClass

fun KClass<URI>.createOrLog(uri: String): URI? =
    try {
        URI(uri)
    } catch (e: URISyntaxException) {
        e.printStackTrace()
        null
    }

fun KClass<URI>.createOrLog(
    scheme: String?,
    userInfo: String?,
    host: String?,
    port: Int,
    path: String?,
    query: String?,
    fragment: String?
): URI? =
    try {
        URI(scheme, userInfo, host, port, path, query, fragment)
    } catch (e: URISyntaxException) {
        e.printStackTrace()
        null
    }

fun KClass<URI>.isValidHost(host: String): Boolean =
    try {
        URI(null, null, host, -1, null, null, null)
        true
    } catch (e: URISyntaxException) {
        false
    }
