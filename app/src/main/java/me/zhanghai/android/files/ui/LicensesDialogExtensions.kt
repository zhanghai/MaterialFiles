/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Message
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import de.psdev.licensesdialog.model.Notices
import me.zhanghai.android.files.compat.scrollIndicatorsCompat
import me.zhanghai.android.files.util.createViewIntent
import me.zhanghai.android.files.util.getColorByAttr
import me.zhanghai.android.files.util.getDimensionPixelSize
import me.zhanghai.android.files.util.startActivitySafe
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

/**
 * @see de.psdev.licensesdialog.LicensesDialog
 */
fun AlertDialog.Builder.setLicensesView(notices: Notices): AlertDialog.Builder {
    val context = context
    val html = createHtml(notices, context)
    return setView(createView(html, context))
}

private fun createHtml(notices: Notices, context: Context): String =
    StringBuilder().apply {
        append("<!DOCTYPE html><html lang=\"en-US\"><head><meta charset=\"utf-8\"><style>")
        append(createStyle(context))
        append("</style></head><body><ul>")
        for (notice in notices.notices) {
            append("<li><div>")
            append(notice.name)
            val url = notice.url
            if (!url.isNullOrEmpty()) {
                append(" (<a href=\"")
                append(url)
                append("\" target=\"_blank\">")
                append(url)
                append("</a>)")
            }
            append("</div><pre>")
            val copyright = notice.copyright
            if (!copyright.isNullOrEmpty()) {
                append(copyright)
                append("<br><br>")
            }
            val license = notice.license
            if (license != null) {
                append(license.getSummaryText(context))
            }
            append("</pre></li>")
        }
        append("</ul></body></html>")
    }.toString()

private fun createStyle(context: Context): String {
    val primaryTextColor = context.getColorByAttr(android.R.attr.textColorPrimary).toCssColor()
    val preformattedTextBackgroundColor = ColorUtils.setAlphaComponent(
        context.getColorByAttr(com.google.android.material.R.attr.colorOnSurface),
        (0.08f * 0xFF).roundToInt()
    ).toCssColor()
    val linkTextColor = context.getColorByAttr(android.R.attr.textColorLink).toCssColor()
    val textHighlightColor = context.getColorByAttr(android.R.attr.textColorHighlight).toCssColor()
    return """
        ::selection {
            background: $textHighlightColor;
        }
        body {
            color: $primaryTextColor;
            margin: 0;
            overflow-wrap: break-word;
            -webkit-tap-highlight-color: $textHighlightColor;
        }
        ul {
            list-style-type: none;
            margin: 0;
            padding: 0;
        }
        li {
            padding: 12px;
        }
        div {
            padding: 0 12px;
        }
        pre {
            background: $preformattedTextBackgroundColor;
            margin: 12px 0 0 0;
            padding: 12px;
            white-space: pre-wrap;
        }
        a, a:link, a:visited, a:hover, a:focus, a:active {
            color: $linkTextColor;
        }
    """.trimIndent()
}

private fun Int.toCssColor(): String =
    if (Color.alpha(this) == 0xFF) {
        "#%06X".format(this and 0x00FFFFFF)
    } else {
        "rgba(${Color.red(this)}, ${Color.green(this)}, ${Color.blue(this)}, ${
            Color.alpha(this).toFloat() / 0xFF
        })"
    }

private fun createView(html: String, context: Context): View {
    val webView = WebView(context).apply {
        scrollIndicatorsCompat = (ViewCompat.SCROLL_INDICATOR_TOP
            or ViewCompat.SCROLL_INDICATOR_BOTTOM)
        setBackgroundColor(Color.TRANSPARENT)
        settings.setSupportMultipleWindows(true)
        webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                val data = view.hitTestResult.extra
                if (data != null) {
                    context.startActivitySafe(Uri.parse(data).createViewIntent())
                }
                return false
            }
        }
        loadDataWithBaseURL(null, html, "text/html", StandardCharsets.UTF_8.name(), null)
    }
    return FrameLayout(context).apply {
        setPaddingRelative(
            0, context.getDimensionPixelSize(
                androidx.appcompat.R.dimen.abc_dialog_title_divider_material
            ), 0, 0
        )
        addView(webView)
    }
}
