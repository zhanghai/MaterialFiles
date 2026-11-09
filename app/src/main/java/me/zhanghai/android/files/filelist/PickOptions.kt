/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import me.zhanghai.android.files.file.MimeType

class PickOptions(
    val mode: Mode,
    val fileName: String?,
    val readOnly: Boolean,
    val mimeTypes: List<MimeType>,
    val localOnly: Boolean,
    val allowMultiple: Boolean,
    /**
     * Whether to return Storage Access Framework document URIs from our DocumentsProvider
     * instead of FileProvider URIs (for ACTION_OPEN_DOCUMENT, ACTION_CREATE_DOCUMENT and
     * ACTION_OPEN_DOCUMENT_TREE, but not ACTION_GET_CONTENT).
     */
    val returnsDocumentUris: Boolean = false
) {
    enum class Mode {
        OPEN_FILE,
        CREATE_FILE,
        OPEN_DIRECTORY
    }
}
