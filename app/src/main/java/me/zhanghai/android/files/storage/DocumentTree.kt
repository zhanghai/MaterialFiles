/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.file.DocumentTreeUri
import me.zhanghai.android.files.provider.document.createDocumentTreeRootPath
import kotlin.random.Random

@Parcelize
class DocumentTree(
    override val id: Long,
    override val name: String,
    val uri: DocumentTreeUri
) : Storage {
    constructor(
        id: Long?,
        name: String,
        uri: DocumentTreeUri
    ) : this(id ?: Random.nextLong(), name, uri)

    override val description: String
        get() = uri.value.toString()
    override val path: Path
        get() = uri.value.createDocumentTreeRootPath()
}
