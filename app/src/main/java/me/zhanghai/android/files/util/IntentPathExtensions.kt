/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import java8.nio.file.Path
import java8.nio.file.Paths
import me.zhanghai.android.files.BuildConfig
import me.zhanghai.android.files.compat.DocumentsContractCompat
import java.io.Serializable
import java.net.URI
import java.net.URISyntaxException

private const val EXTRA_PATH_URI = "${BuildConfig.APPLICATION_ID}.extra.PATH_URI"

var Intent.extraPath: Path?
    get() = getExtraPath(false)
    set(value) {
        // We cannot put Path into intent here, otherwise we will crash other apps unmarshalling it.
        // We cannot put URI into intent here either, because ShortcutInfo uses PersistableBundle
        // which doesn't support Serializable.
        putExtra(EXTRA_PATH_URI, value?.toUri()?.toString())
    }

fun Intent.getExtraPath(allowDataContentUri: Boolean): Path? {
    val extraPathUriString = getStringExtra(EXTRA_PATH_URI)
    if (extraPathUriString != null) {
        val extraPathUri = try {
            URI(extraPathUriString)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            null
        }
        extraPathUri?.let { return Paths.get(extraPathUri) }
    }
    val data = data
    if (data != null) {
        when (data.scheme) {
            ContentResolver.SCHEME_FILE, null -> {
                val path = data.path?.takeIfNotEmpty()
                path?.let { return Paths.get(it) }
            }
            ContentResolver.SCHEME_CONTENT -> {
                if (allowDataContentUri) {
                    val dataUri = try {
                        URI(data.toString())
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                        // Some people use Uri.parse() without encoding their path. Let's try saving
                        // them by calling the other URI constructor that encodes everything.
                        try {
                            URI(
                                data.scheme, data.userInfo, data.host, data.port, data.path,
                                data.query, data.fragment
                            )
                        } catch (e2: URISyntaxException) {
                            e2.printStackTrace()
                            null
                        }
                    }
                    dataUri?.let { return Paths.get(it) }
                }
            }
        }
    }
    val extraInitialUri = getParcelableExtraSafe<Uri>(DocumentsContractCompat.EXTRA_INITIAL_URI)
    // TODO: Support DocumentsProvider Uri?
    if (extraInitialUri != null && extraInitialUri.scheme == ContentResolver.SCHEME_FILE) {
        val path = extraInitialUri.path?.takeIfNotEmpty()
        path?.let { return Paths.get(it) }
    }
    val extraAbsolutePath = getStringExtra("org.openintents.extra.ABSOLUTE_PATH")?.takeIfNotEmpty()
    extraAbsolutePath?.let { return Paths.get(it) }
    return null
}

private const val EXTRA_PATH_URI_LIST = "${BuildConfig.APPLICATION_ID}.extra.PATH_URI_LIST"

var Intent.extraPathList: List<Path>
    get() = getExtraPathList(false)
    set(value) {
        // We cannot put Path into intent here, otherwise we will crash other apps unmarshalling it.
        val pathUris = value.map { it.toUri() }
        putExtra(EXTRA_PATH_URI_LIST, pathUris as Serializable)
    }

fun Intent.getExtraPathList(allowDataContentUri: Boolean): List<Path> {
    @Suppress("UNCHECKED_CAST")
    val extraPathUris = (getSerializableExtra(EXTRA_PATH_URI_LIST) as List<URI>?)?.takeIfNotEmpty()
    extraPathUris?.let { return it.map { uri -> Paths.get(uri) } }
    val extraPath = getExtraPath(allowDataContentUri)
    return listOfNotNull(extraPath)
}
