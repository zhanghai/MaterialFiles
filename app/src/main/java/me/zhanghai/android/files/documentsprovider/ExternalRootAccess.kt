/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.documentsprovider

import android.content.Context
import java8.nio.file.Path
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.provider.root.RootStrategy
import me.zhanghai.android.files.provider.root.RootablePath
import me.zhanghai.android.files.provider.root.isRunningAsRoot
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

/** Whether other apps may access files that we can only reach with root or Shizuku. */
enum class ExternalRootAccess {
    NEVER,
    ASK,
    ALWAYS
}

/**
 * Whether serving this path to another app would go through root or Shizuku, i.e. give that app
 * access it couldn't get from us with normal storage permissions.
 */
val Path.requiresRootForExternalAccess: Boolean
    get() {
        if (this !is RootablePath) {
            return false
        }
        if (!isRunningAsRoot && Settings.ROOT_STRATEGY.valueCompat == RootStrategy.NEVER) {
            return false
        }
        return isRootRequired(false) || isRootRequired(true)
    }

/**
 * Root/Shizuku-backed paths that the user explicitly allowed specific apps to access. A grant on
 * a directory covers everything under it.
 */
object ExternalRootAccessGrants {
    private const val PREFERENCES_NAME = "external_root_access_grants"
    private const val KEY_GRANTS = "grants"
    // Package names can't contain '|', and shared preferences XML can't contain control characters.
    private const val SEPARATOR = '|'

    private val preferences by lazy {
        application.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private val lock = Any()

    val count: Int
        get() = getGrants().size

    fun grant(packageName: String, paths: Collection<Path>) {
        synchronized(lock) {
            val grants = getGrants().toMutableSet()
            paths.mapTo(grants) { "$packageName$SEPARATOR${it.documentId}" }
            preferences.edit().putStringSet(KEY_GRANTS, grants).apply()
        }
    }

    fun isGranted(packageName: String, path: Path): Boolean {
        val normalizedPath = path.normalize()
        return getGrants().any { grant ->
            val separatorIndex = grant.indexOf(SEPARATOR)
            if (grant.substring(0, separatorIndex) != packageName) {
                return@any false
            }
            val grantedPath = grant.substring(separatorIndex + 1).toDocumentPathOrNull()
                ?: return@any false
            normalizedPath.isSameOrDescendantOf(grantedPath)
        }
    }

    fun revokeAll() {
        synchronized(lock) {
            preferences.edit().remove(KEY_GRANTS).apply()
        }
    }

    private fun getGrants(): Set<String> =
        preferences.getStringSet(KEY_GRANTS, null) ?: emptySet()
}

/**
 * Whether [packageName] may access [path] through our providers, given the user's
 * [ExternalRootAccess] setting and any per-app grants.
 */
fun isExternalAccessAllowed(packageName: String?, path: Path): Boolean {
    if (!path.requiresRootForExternalAccess) {
        return true
    }
    if (packageName == application.packageName) {
        return true
    }
    return when (Settings.EXTERNAL_ROOT_ACCESS.valueCompat) {
        ExternalRootAccess.NEVER -> false
        ExternalRootAccess.ASK ->
            packageName != null && ExternalRootAccessGrants.isGranted(packageName, path)
        ExternalRootAccess.ALWAYS -> true
    }
}

internal fun Path.isSameOrDescendantOf(ancestor: Path): Boolean {
    if (fileSystem != ancestor.fileSystem) {
        return false
    }
    return normalize().startsWith(ancestor.normalize())
}
