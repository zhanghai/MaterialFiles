/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.content.Context
import android.content.Intent
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.file.DocumentUri
import me.zhanghai.android.files.file.displayName
import me.zhanghai.android.files.util.createDocumentManagerViewDirectoryIntent
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.putArgs
import kotlin.random.Random

@Parcelize
data class DocumentManagerShortcut(
    override val id: Long,
    override val customName: String?,
    val uri: DocumentUri
) : Storage() {
    constructor(
        id: Long?,
        customName: String?,
        uri: DocumentUri
    ) : this(id ?: Random.nextLong(), customName, uri)

    override fun getDefaultName(context: Context): String =
        uri.displayName ?: uri.value.lastPathSegment ?: uri.value.toString()

    override val description: String
        get() = uri.value.toString()

    override val path: Path?
        get() = null

    override fun createIntent(): Intent = uri.value.createDocumentManagerViewDirectoryIntent()

    override fun createEditIntent(): Intent =
        EditDocumentManagerShortcutDialogActivity::class.createIntent()
            .putArgs(EditDocumentManagerShortcutDialogFragment.Args(this))
}
