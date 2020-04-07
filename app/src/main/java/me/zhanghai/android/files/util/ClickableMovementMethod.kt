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
    override fun canSelectArbitrarily(): Boolean = false

    override fun onTouchEvent(widget: TextView, text: Spannable, event: MotionEvent): Boolean {
        when (val action = event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                val x = event.x.toInt() - widget.totalPaddingLeft + widget.scrollX
                val y = event.y.toInt() - widget.totalPaddingTop + widget.scrollY
                val layout = widget.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x.toFloat())
                val span = text.getSpans(off, off, ClickableSpan::class.java).firstOrNull()
                if (span != null) {
                    if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(text, text.getSpanStart(span), text.getSpanEnd(span))
                    } else {
                        span.onClick(widget)
                    }
                    return true
                } else {
                    Selection.removeSelection(text)
                }
            }
        }
        return false
    }

    override fun initialize(widget: TextView, text: Spannable) {
        Selection.removeSelection(text)
    }
}
