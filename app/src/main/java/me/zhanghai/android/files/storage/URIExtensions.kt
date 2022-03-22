package me.zhanghai.android.files.storage

import java.net.URI
import java.net.URISyntaxException
import kotlin.reflect.KClass

fun KClass<URI>.isValidHost(host: String): Boolean =
    try {
        URI(null, null, host, -1, null, null, null)
        true
    } catch (e: URISyntaxException) {
        false
    }
