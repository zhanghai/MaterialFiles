/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class PreviewWebViewClient(
    private val onPageLoaded: (() -> Unit)? = null,
    private val onError: ((String) -> Unit)? = null
) : WebViewClient() {
    private var hasLoadedOnce = false

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (!hasLoadedOnce) {
            hasLoadedOnce = true
            onPageLoaded?.invoke()
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        onError?.invoke(error?.description?.toString() ?: "Unknown error")
    }

    fun reset() {
        hasLoadedOnce = false
    }
}
