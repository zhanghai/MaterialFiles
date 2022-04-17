/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.RotateDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Property
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.leinardi.android.speeddial.FabWithLabelView
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.createCompat
import me.zhanghai.android.files.compat.setTextAppearanceCompat
import me.zhanghai.android.files.util.ParcelableState
import me.zhanghai.android.files.util.asColor
import me.zhanghai.android.files.util.dpToDimensionPixelSize
import me.zhanghai.android.files.util.getColorByAttr
import me.zhanghai.android.files.util.getParcelableSafe
import me.zhanghai.android.files.util.getResourceIdByAttr
import me.zhanghai.android.files.util.isMaterial3Theme
import me.zhanghai.android.files.util.shortAnimTime
import me.zhanghai.android.files.util.withModulatedAlpha

class ThemedSpeedDialView : SpeedDialView {
    private var mainFabAnimator: Animator? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    init {
        // Work around ripple bug on Android 12 when useCompatPadding = true.
        // @see https://github.com/material-components/material-components-android/issues/2617
        mainFab.apply {
            updateLayoutParams<MarginLayoutParams> {
                setMargins(context.dpToDimensionPixelSize(16))
            }
            useCompatPadding = false
        }
        val context = context
        if (context.isMaterial3Theme) {
            mainFabClosedBackgroundColor = context.getColorByAttr(R.attr.colorSecondaryContainer)
            mainFabClosedIconColor = context.getColorByAttr(R.attr.colorOnSecondaryContainer)
            mainFabOpenedBackgroundColor = context.getColorByAttr(R.attr.colorPrimary)
            mainFabOpenedIconColor = context.getColorByAttr(R.attr.colorOnPrimary)
        } else {
            mainFabClosedBackgroundColor = context.getColorByAttr(R.attr.colorSecondary)
            mainFabClosedIconColor = context.getColorByAttr(R.attr.colorOnSecondary)
            mainFabOpenedBackgroundColor = mainFabClosedBackgroundColor
            mainFabOpenedIconColor = mainFabClosedIconColor
        }
        // Always use our own animation to fix the library issue that ripple is rotated as well.
        val mainFabDrawable = RotateDrawable::class.createCompat().apply {
            drawable = mainFab.drawable
            toDegrees = mainFabAnimationRotateAngle
        }
        mainFabAnimationRotateAngle = 0f
        setMainFabClosedDrawable(mainFabDrawable)
        setOnChangeListener(object : OnChangeListener {
            override fun onMainActionSelected(): Boolean = false

            override fun onToggleChanged(isOpen: Boolean) {
                mainFabAnimator?.cancel()
                mainFabAnimator = createMainFabAnimator(isOpen).apply {
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            mainFabAnimator = null
                        }
                    })
                    start()
                }
            }
        })
    }

    private fun createMainFabAnimator(isOpen: Boolean): Animator =
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofArgb(
                    mainFab, VIEW_PROPERTY_BACKGROUND_TINT,
                    if (isOpen) mainFabOpenedBackgroundColor else mainFabClosedBackgroundColor
                ),
                ObjectAnimator.ofArgb(
                    mainFab, IMAGE_VIEW_PROPERTY_IMAGE_TINT,
                    if (isOpen) mainFabOpenedIconColor else mainFabClosedIconColor
                ),
                ObjectAnimator.ofInt(
                    mainFab.drawable, DRAWABLE_PROPERTY_LEVEL, if (isOpen) 10000 else 0
                )
            )
            duration = context.shortAnimTime.toLong()
            interpolator = FastOutSlowInInterpolator()
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val overlayLayout = overlayLayout
        if (overlayLayout != null) {
            val surfaceColor = context.getColorByAttr(R.attr.colorSurface)
            val overlayColor = surfaceColor.asColor().withModulatedAlpha(0.87f).value
            overlayLayout.setBackgroundColor(overlayColor)
        }
    }

    override fun addActionItem(
        actionItem: SpeedDialActionItem,
        position: Int,
        animate: Boolean
    ): FabWithLabelView? {
        val context = context
        val isMaterial3Theme = context.isMaterial3Theme
        val fabImageTintColor = if (isMaterial3Theme) {
            context.getColorByAttr(R.attr.colorPrimary)
        } else {
            context.getColorByAttr(R.attr.colorSecondary)
        }
        val fabBackgroundColor = context.getColorByAttr(R.attr.colorSurface)
        val labelColor = context.getColorByAttr(android.R.attr.textColorSecondary)
        val labelBackgroundColor = if (isMaterial3Theme) {
            Color.TRANSPARENT
        } else {
            // Label view doesn't have enough elevation (only 1dp) for elevation overlay to work
            // well.
            context.getColorByAttr(R.attr.colorBackgroundFloating)
        }
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
        return super.addActionItem(actionItem, position, animate)?.apply {
            fab.apply {
                updateLayoutParams<MarginLayoutParams> {
                    val horizontalMargin = context.dpToDimensionPixelSize(20)
                    setMargins(horizontalMargin, 0, horizontalMargin, 0)
                }
                useCompatPadding = false
            }
            if (isMaterial3Theme) {
                labelBackground.apply {
                    useCompatPadding = false
                    setContentPadding(0, 0, 0, 0)
                    foreground = null
                    (getChildAt(0) as TextView).apply {
                        setTextAppearanceCompat(
                            context.getResourceIdByAttr(R.attr.textAppearanceLabelLarge)
                        )
                    }
                }
            }
        }
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

    companion object {
        private val VIEW_PROPERTY_BACKGROUND_TINT =
            object : Property<View, Int>(Int::class.java, "backgroundTint") {
                override fun get(view: View): Int? = view.backgroundTintList!!.defaultColor

                override fun set(view: View, value: Int?) {
                    view.backgroundTintList = ColorStateList.valueOf(value!!)
                }
            }

        private val IMAGE_VIEW_PROPERTY_IMAGE_TINT =
            object : Property<ImageView, Int>(Int::class.java, "imageTint") {
                override fun get(view: ImageView): Int? = view.imageTintList!!.defaultColor

                override fun set(view: ImageView, value: Int?) {
                    view.imageTintList = ColorStateList.valueOf(value!!)
                }
            }

        private val DRAWABLE_PROPERTY_LEVEL =
            object : Property<Drawable, Int>(Int::class.java, "level") {
                override fun get(drawable: Drawable): Int? = drawable.level

                override fun set(drawable: Drawable, value: Int?) {
                    drawable.level = value!!
                }
            }
    }

    @Parcelize
    private class State(val superState: Parcelable?, val isOpen: Boolean) : ParcelableState
}
