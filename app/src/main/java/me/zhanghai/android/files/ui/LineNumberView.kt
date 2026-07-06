/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText

class LineNumberView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var editText: AppCompatEditText? = null
        set(value) {
            field = value
            updateTypeface()
        }

    private var scrollOffset = 0

    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
    }

    private val separatorPaint = Paint().apply {
        strokeWidth = resources.displayMetrics.density
    }

    private var padding = 0f

    private fun updateTypeface() {
        val edit = editText
        numberPaint.typeface = if (edit != null) edit.typeface else Typeface.MONOSPACE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val textColor = context.obtainStyledAttributes(
            intArrayOf(android.R.attr.textColorTertiary)
        ).let { attrs ->
            val color = attrs.getColor(0, 0xFF9E9E9E.toInt())
            attrs.recycle()
            color
        }
        numberPaint.color = textColor
        separatorPaint.color = (textColor and 0x00FFFFFF) or (0x33000000.toInt())
    }

    fun setScrollOffset(offset: Int) {
        if (scrollOffset != offset) {
            scrollOffset = offset
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val edit = editText
        if (edit != null && edit.layout != null) {
            val layout = edit.layout!!
            val lineCount = layout.lineCount
            val maxDigits = lineCount.toString().length
            numberPaint.textSize = edit.textSize * 0.85f
            val textWidth = numberPaint.measureText("9".repeat(maxDigits))
            padding = resources.displayMetrics.density * 4
            val totalWidth = (textWidth + padding * 2 + separatorPaint.strokeWidth).toInt()
            setMeasuredDimension(totalWidth, MeasureSpec.getSize(heightMeasureSpec))
        } else {
            setMeasuredDimension(
                (resources.displayMetrics.density * 32).toInt(),
                MeasureSpec.getSize(heightMeasureSpec)
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val edit = editText ?: return
        val layout = edit.layout ?: return
        val lineCount = layout.lineCount
        if (lineCount == 0) return

        val baselineOffset = edit.baseline - layout.getLineBaseline(0)
        val firstVisibleLine = layout.getLineForVertical(scrollOffset).coerceAtLeast(0)
        val lastVisibleLine =
            layout.getLineForVertical(scrollOffset + height).coerceAtMost(lineCount - 1)

        val textSize = numberPaint.textSize
        val drawPadding = padding

        for (i in firstVisibleLine..lastVisibleLine) {
            val lineTop = layout.getLineTop(i)
            val lineBottom = layout.getLineBottom(i)
            if (lineBottom < scrollOffset || lineTop > scrollOffset + height) continue

            val y = lineTop - scrollOffset + baselineOffset
            canvas.drawText(
                "${i + 1}",
                width - drawPadding,
                y,
                numberPaint
            )
        }

        canvas.drawLine(
            width - separatorPaint.strokeWidth, 0f,
            width - separatorPaint.strokeWidth, height.toFloat(),
            separatorPaint
        )
    }
}
