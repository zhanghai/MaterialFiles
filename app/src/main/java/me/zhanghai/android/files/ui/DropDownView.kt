/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ListAdapter
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.FixPaddingListPopupWindow
import androidx.appcompat.widget.ListPopupWindow
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.ParcelableState
import me.zhanghai.android.files.util.doOnGlobalLayout
import me.zhanghai.android.files.util.getBooleanByAttr
import me.zhanghai.android.files.util.getDimensionPixelOffset

class DropDownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private val popup: ListPopupWindow

    init {
        visibility = INVISIBLE
        popup = FixPaddingListPopupWindow(context, attrs).apply {
            isModal = true
            anchorView = this@DropDownView
            inputMethodMode = ListPopupWindow.INPUT_METHOD_NOT_NEEDED
        }
        maybeSimulateElevationOverlay()
    }

    private fun maybeSimulateElevationOverlay() {
        val context = context
        val elevationOverlayEnabled = context.getBooleanByAttr(R.attr.elevationOverlayEnabled)
        if (!elevationOverlayEnabled) {
            return
        }
        val elevation = context.getDimensionPixelOffset(
            R.dimen.mtrl_exposed_dropdown_menu_popup_elevation
        ).toFloat()
        val background = MaterialShapeDrawable.createWithElevationOverlay(context, elevation)
            .apply {
                val cornerSize = context.getDimensionPixelOffset(
                    R.dimen.mtrl_shape_corner_size_small_component
                ).toFloat()
                shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setAllCornerSizes(cornerSize)
                    .build()
            }
        popup.setBackgroundDrawable(background)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        if (popup.isShowing) {
            popup.dismiss()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        popup.width = measuredWidth
    }

    override fun onSaveInstanceState(): Parcelable? =
        State(super.onSaveInstanceState(), popup.isShowing)

    override fun onRestoreInstanceState(state: Parcelable?) {
        state as State
        super.onRestoreInstanceState(state.superState)
        if (state.isShowing) {
            doOnGlobalLayout {
                if (!popup.isShowing) {
                    popup.show()
                }
            }
        }
    }

    fun setAdapter(adapter: ListAdapter?) {
        popup.setAdapter(adapter)
    }

    fun setOnItemClickListener(listener: AdapterView.OnItemClickListener?) {
        popup.setOnItemClickListener(listener)
    }

    fun setOnItemClickListener(listener: (AdapterView<*>, View, Int, Long) -> Unit) {
        popup.setOnItemClickListener(listener)
    }

    val isShowing: Boolean
        get() = popup.isShowing

    fun show() {
        popup.show()
    }

    fun dismiss() {
        popup.dismiss()
    }

    @Parcelize
    private class State(val superState: Parcelable?, val isShowing: Boolean) : ParcelableState
}
