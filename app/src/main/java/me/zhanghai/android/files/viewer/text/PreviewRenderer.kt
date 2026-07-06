/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.webkit.WebView
import me.zhanghai.android.files.file.PreviewType

class PreviewRenderer(private val webView: WebView) {
    private var isReady = false
    private var pendingRender: (() -> Unit)? = null

    fun onReady() {
        isReady = true
        pendingRender?.invoke()
        pendingRender = null
    }

    fun render(type: PreviewType, content: String, language: String = "", theme: String = "light") {
        val escapedContent = escapeJavaScriptString(content)
        val escapedLanguage = escapeJavaScriptString(language)
        val renderCall = buildRenderCall(type, escapedContent, escapedLanguage)
        setTheme(theme)

        if (!isReady) {
            pendingRender = renderCall
            return
        }
        renderCall.invoke()
    }

    fun setTheme(theme: String) {
        webView.evaluateJavascript("setTheme('$theme');", null)
    }

    private fun buildRenderCall(type: PreviewType, content: String, language: String): () -> Unit {
        return when (type) {
            PreviewType.MARKDOWN -> {
                {
                    webView.evaluateJavascript(
                        "renderMarkdown('$content');", null
                    )
                }
            }
            PreviewType.CODE -> {
                {
                    webView.evaluateJavascript(
                        "renderCode('$content', '$language');", null
                    )
                }
            }
            PreviewType.TEXT -> {
                {
                    webView.evaluateJavascript(
                        "renderText('$content');", null
                    )
                }
            }
        }
    }

    private fun escapeJavaScriptString(s: String): String {
        return s
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\u2028", "\\u2028")
            .replace("\u2029", "\\u2029")
    }
}
