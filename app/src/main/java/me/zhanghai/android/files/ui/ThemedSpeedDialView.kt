/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.annotation.AttrRes
import com.leinardi.android.speeddial.FabWithLabelView
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.ParcelableState
import me.zhanghai.android.files.util.asColor
import me.zhanghai.android.files.util.getColorByAttr
import me.zhanghai.android.files.util.getParcelableSafe
import me.zhanghai.android.files.util.withModulatedAlpha

class ThemedSpeedDialView : SpeedDialView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val overlayLayout = overlayLayout
        if (overlayLayout != null) {
            val surfaceColor = context.getColorByAttr(R.attr.colorSurface)
            val overlayColor = surfaceColor.asColor().withModulatedAlpha(0.6f).value
            overlayLayout.setBackgroundColor(overlayColor)
        }
    }

    override fun addActionItem(
        actionItem: SpeedDialActionItem,
        position: Int,
        animate: Boolean
    ): FabWithLabelView? {
        val context = context
        val fabImageTintColor = context.getColorByAttr(R.attr.colorSecondary)
        val fabBackgroundColor = context.getColorByAttr(R.attr.colorSurface)
        val labelColor = context.getColorByAttr(android.R.attr.textColorSecondary)
        // Label view doesn't have enough elevation (only 1dp) for elevation overlay to work well.
        val labelBackgroundColor = context.getColorByAttr(R.attr.colorBackgroundFloating)
        val actionItem = SpeedDialActionItem.Builder(
            actionItem.id,
            // Should not be a resource, pass null to fail fast.
            actionItem.getFabImageDrawable(null)
        )
            .setLabel(actionItem.getLabel(context))
            .setFabImageTintColor(fabImageTintColor)
            .setFabBackgroundColor(fabBackgroundColor)
            .setLabelColor(labelColor)
            .setLabelBackgroundColor(labelBackgroundColor)
            .setLabelClickable(actionItem.isLabelClickable)
            .setTheme(actionItem.theme)
            .create()
        return super.addActionItem(actionItem, position, animate)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = (super.onSaveInstanceState() as Bundle)
            .getParcelableSafe<Parcelable>("superState")
        return State(superState, isOpen)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        state as State
        super.onRestoreInstanceState(state.superState)
        if (state.isOpen) {
            toggle(false)
        }
    }

    @Parcelize
    private class State(val superState: Parcelable?, val isOpen: Boolean) : ParcelableState
}
