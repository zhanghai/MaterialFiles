/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.text.NoCopySpan.Concrete
import android.text.Selection
import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
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
 */
object LinkArrowKeyMovementMethod : ArrowKeyMovementMethod() {
    private const val CLICK = 1
    private const val UP = 2
    private const val DOWN = 3

    private val FROM_BELOW = Concrete()

    override fun initialize(widget: TextView?, text: Spannable) {
        super.initialize(widget, text)

        text.removeSpan(FROM_BELOW)
    }

    override fun onTakeFocus(view: TextView, text: Spannable, dir: Int) {
        super.onTakeFocus(view, text, dir)

        if (dir.hasBits(View.FOCUS_BACKWARD)) {
            text.setSpan(FROM_BELOW, 0, 0, Spannable.SPAN_POINT_POINT)
        } else {
            text.removeSpan(FROM_BELOW)
        }
    }

    override fun handleMovementKey(
        widget: TextView,
        buffer: Spannable,
        keyCode: Int,
        movementMetaState: Int,
        event: KeyEvent
    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (KeyEvent.metaStateHasNoModifiers(movementMetaState)) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0
                    && action(CLICK, widget, buffer)) {
                    return true
                }
            }
        }
        return super.handleMovementKey(widget, buffer, keyCode, movementMetaState, event)
    }

    override fun up(widget: TextView, buffer: Spannable): Boolean {
        if (action(UP, widget, buffer)) {
            return true
        }
        return super.up(widget, buffer)
    }

    override fun down(widget: TextView, buffer: Spannable): Boolean {
        if (action(DOWN, widget, buffer)) {
            return true
        }
        return super.down(widget, buffer)
    }

    override fun left(widget: TextView, buffer: Spannable): Boolean {
        if (action(UP, widget, buffer)) {
            return true
        }
        return super.left(widget, buffer)
    }

    override fun right(widget: TextView, buffer: Spannable): Boolean {
        if (action(DOWN, widget, buffer)) {
            return true
        }
        return super.right(widget, buffer)
    }

    private fun action(what: Int, widget: TextView, buffer: Spannable): Boolean {
        val layout = widget.layout
        val padding = widget.totalPaddingTop + widget.totalPaddingBottom
        val areaTop = widget.scrollY
        val areaBot = areaTop + widget.height - padding
        val lineTop = layout.getLineForVertical(areaTop)
        val lineBot = layout.getLineForVertical(areaBot)
        val first = layout.getLineStart(lineTop)
        val last = layout.getLineEnd(lineBot)
        val candidates = buffer.getSpans(first, last, ClickableSpan::class.java)
        val a = Selection.getSelectionStart(buffer)
        val b = Selection.getSelectionEnd(buffer)
        var selStart = min(a, b)
        var selEnd = max(a, b)
        if (selStart < 0) {
            if (buffer.getSpanStart(FROM_BELOW) >= 0) {
                selEnd = buffer.length
                selStart = selEnd
            }
        }
        if (selStart > last) {
            selEnd = Int.MAX_VALUE
            selStart = selEnd
        }
        if (selEnd < first) {
            selEnd = -1
            selStart = selEnd
        }
        when (what) {
            CLICK -> {
                if (selStart == selEnd) {
                    return false
                }
                val link = buffer.getSpans(selStart, selEnd, ClickableSpan::class.java)
                if (link.size != 1) {
                    return false
                }
                link[0].onClick(widget)
            }
            UP -> {
                var bestStart = -1
                var bestEnd = -1
                for (candidate in candidates) {
                    val end = buffer.getSpanEnd(candidate)
                    if (end < selEnd || selStart == selEnd) {
                        if (end > bestEnd) {
                            bestStart = buffer.getSpanStart(candidate)
                            bestEnd = end
                        }
                    }
                }
                if (bestStart >= 0) {
                    Selection.setSelection(buffer, bestEnd, bestStart)
                    return true
                }
            }
            DOWN -> {
                var bestStart = Int.MAX_VALUE
                var bestEnd = Int.MAX_VALUE
                for (candidate in candidates) {
                    val start = buffer.getSpanStart(candidate)
                    if (start > selStart || selStart == selEnd) {
                        if (start < bestStart) {
                            bestStart = start
                            bestEnd = buffer.getSpanEnd(candidate)
                        }
                    }
                }
                if (bestEnd < Int.MAX_VALUE) {
                    Selection.setSelection(buffer, bestStart, bestEnd)
                    return true
                }
            }
        }
        return false
    }

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val links = buffer.getSpans(off, off, ClickableSpan::class.java)
            if (links.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    links[0].onClick(widget)
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(
                        buffer, buffer.getSpanStart(links[0]), buffer.getSpanEnd(links[0])
                    )
                }
                return true
            }
            // Removed
            //else {
            //    Selection.removeSelection(buffer);
            //}
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}
