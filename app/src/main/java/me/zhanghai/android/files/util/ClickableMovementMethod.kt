/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.text.Selection
import android.text.Spannable
import android.text.method.BaseMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView

/**
 * A movement method that traverses links in the text buffer and fires clicks. Unlike
 * [android.text.method.LinkMovementMethod], this will not consume touch events outside
 * [ClickableSpan]s.
 */
object ClickableMovementMethod : BaseMovementMethod() {
    override fun initialize(view: TextView, text: Spannable) {
        Selection.removeSelection(text)
    }

    override fun onTouchEvent(view: TextView, text: Spannable, event: MotionEvent): Boolean {
        when (val action = event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                val x = event.x.toInt() - view.totalPaddingLeft + view.scrollX
                val y = event.y.toInt() - view.totalPaddingTop + view.scrollY
                val layout = view.layout
                val span = if (y < 0 || y > layout.height) {
                    null
                } else {
                    val line = layout.getLineForVertical(y)
                    if (x < layout.getLineLeft(line) || x > layout.getLineRight(line)) {
                        null
                    } else {
                        val off = layout.getOffsetForHorizontal(line, x.toFloat())
                        text.getSpans(off, off, ClickableSpan::class.java).firstOrNull()
                    }
                }
                if (span != null) {
                    if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(text, text.getSpanStart(span), text.getSpanEnd(span))
                    } else {
                        span.onClick(view)
                    }
                    return true
                } else {
                    Selection.removeSelection(text)
                }
            }
        }
        return false
    }
}
