/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.text.NoCopySpan.Concrete
import android.text.Selection
import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import kotlin.math.max
import kotlin.math.min

/**
 * @see LinkMovementMethod
 * @see ArrowKeyMovementMethod
 * @see ClickableMovementMethod
 */
object ClickableArrowKeyMovementMethod : ArrowKeyMovementMethod() {
    private const val CLICK = 1
    private const val UP = 2
    private const val DOWN = 3

    private val FROM_BELOW = Concrete()

    override fun initialize(view: TextView, text: Spannable) {
        super.initialize(view, text)

        text.removeSpan(FROM_BELOW)
    }

    override fun onTakeFocus(view: TextView, text: Spannable, direction: Int) {
        super.onTakeFocus(view, text, direction)

        if (direction.hasBits(View.FOCUS_BACKWARD)) {
            text.setSpan(FROM_BELOW, 0, 0, Spannable.SPAN_POINT_POINT)
        } else {
            text.removeSpan(FROM_BELOW)
        }
    }

    override fun handleMovementKey(
        view: TextView,
        text: Spannable,
        keyCode: Int,
        movementMetaState: Int,
        event: KeyEvent
    ): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if (KeyEvent.metaStateHasNoModifiers(movementMetaState)) {
                    if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0
                        && action(CLICK, view, text)) {
                        return true
                    }
                }
            }
        }
        return super.handleMovementKey(view, text, keyCode, movementMetaState, event)
    }

    override fun up(view: TextView, text: Spannable): Boolean {
        if (action(UP, view, text)) {
            return true
        }
        return super.up(view, text)
    }

    override fun down(view: TextView, text: Spannable): Boolean {
        if (action(DOWN, view, text)) {
            return true
        }
        return super.down(view, text)
    }

    override fun left(view: TextView, text: Spannable): Boolean {
        if (action(UP, view, text)) {
            return true
        }
        return super.left(view, text)
    }

    override fun right(view: TextView, text: Spannable): Boolean {
        if (action(DOWN, view, text)) {
            return true
        }
        return super.right(view, text)
    }

    private fun action(what: Int, view: TextView, text: Spannable): Boolean {
        val layout = view.layout
        val padding = view.totalPaddingTop + view.totalPaddingBottom
        val areaTop = view.scrollY
        val areaBottom = areaTop + view.height - padding
        val lineTop = layout.getLineForVertical(areaTop)
        val lineBottom = layout.getLineForVertical(areaBottom)
        val first = layout.getLineStart(lineTop)
        val last = layout.getLineEnd(lineBottom)
        val candidates = text.getSpans(first, last, ClickableSpan::class.java)
        val a = Selection.getSelectionStart(text)
        val b = Selection.getSelectionEnd(text)
        var selectionStart = min(a, b)
        var selectionEnd = max(a, b)
        if (selectionStart < 0) {
            if (text.getSpanStart(FROM_BELOW) >= 0) {
                selectionEnd = text.length
                selectionStart = selectionEnd
            }
        }
        if (selectionStart > last) {
            selectionEnd = Int.MAX_VALUE
            selectionStart = selectionEnd
        }
        if (selectionEnd < first) {
            selectionEnd = -1
            selectionStart = selectionEnd
        }
        when (what) {
            CLICK -> {
                if (selectionStart == selectionEnd) {
                    return false
                }
                val span = text.getSpans(selectionStart, selectionEnd, ClickableSpan::class.java)
                    .singleOrNull() ?: return false
                span.onClick(view)
            }
            UP -> {
                var bestStart = -1
                var bestEnd = -1
                for (candidate in candidates) {
                    val end = text.getSpanEnd(candidate)
                    if (end < selectionEnd || selectionStart == selectionEnd) {
                        if (end > bestEnd) {
                            bestStart = text.getSpanStart(candidate)
                            bestEnd = end
                        }
                    }
                }
                if (bestStart >= 0) {
                    Selection.setSelection(text, bestEnd, bestStart)
                    return true
                }
            }
            DOWN -> {
                var bestStart = Int.MAX_VALUE
                var bestEnd = Int.MAX_VALUE
                for (candidate in candidates) {
                    val start = text.getSpanStart(candidate)
                    if (start > selectionStart || selectionStart == selectionEnd) {
                        if (start < bestStart) {
                            bestStart = start
                            bestEnd = text.getSpanEnd(candidate)
                        }
                    }
                }
                if (bestEnd < Int.MAX_VALUE) {
                    Selection.setSelection(text, bestStart, bestEnd)
                    return true
                }
            }
        }
        return false
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
                }
                // Removed
                //else {
                //    Selection.removeSelection(text);
                //}
            }
        }
        return super.onTouchEvent(view, text, event)
    }
}
