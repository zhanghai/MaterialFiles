/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeDrawableAccessor
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.getColorByAttr
import me.zhanghai.android.files.util.getDimension
import me.zhanghai.android.files.util.getResourceIdByAttr

fun View.maybeUseMd3AppBarElevationOverlay() {
    val background = background
    val context = context
    if (background is MaterialShapeDrawable
        && context.getResourceIdByAttr(R.attr.colorLiftedAppBarSurface)
        != ResourcesCompat.ID_NULL) {
        stateListAnimator = null
        MaterialShapeDrawableAccessor.setElevationOverlayProvider(
            background, Md3AppBarElevationOverlayProvider(context)
        )
        MaterialShapeDrawableAccessor.updateZ(background)
    }
}

private class Md3AppBarElevationOverlayProvider(
    context: Context
) : ElevationOverlayProvider(context) {
    private val liftedAppBarSurfaceColor =
        context.getColorByAttr(R.attr.colorLiftedAppBarSurface)

    // @see AppBarLayout.startLiftOnScrollElevationOverlayAnimation
    private val liftedElevation = context.getDimension(R.dimen.design_appbar_elevation)

    // @see ElevationOverlayProvider.compositeOverlayIfNeeded
    // Uses isThemeElevationOverlayEnabled() instead of elevationOverlayEnabled.
    @ColorInt
    override fun compositeOverlayIfNeeded(
        @ColorInt backgroundColor: Int,
        elevation: Float
    ): Int =
        if (isThemeElevationOverlayEnabled && isThemeSurfaceColor(backgroundColor)) {
            compositeOverlay(backgroundColor, elevation)
        } else {
            backgroundColor
        }

    private fun isThemeSurfaceColor(@ColorInt color: Int): Boolean =
        ColorUtils.setAlphaComponent(color, 255) == themeSurfaceColor

    // @see ElevationOverlayProvider.compositeOverlay
    // Uses getThemeElevationOverlayColor() instead of elevationOverlayColor.
    @ColorInt
    override fun compositeOverlay(@ColorInt backgroundColor: Int, elevation: Float): Int {
        val overlayAlphaFraction = calculateOverlayAlphaFraction(elevation)
        val backgroundAlpha = Color.alpha(backgroundColor)
        val backgroundColorOpaque = ColorUtils.setAlphaComponent(backgroundColor, 255)
        val overlayColorOpaque = MaterialColors.layer(
            backgroundColorOpaque, themeElevationOverlayColor, overlayAlphaFraction
        )
        return ColorUtils.setAlphaComponent(overlayColorOpaque, backgroundAlpha)
    }

    override fun calculateOverlayAlphaFraction(elevation: Float): Float {
        return (elevation / liftedElevation).coerceIn(0f, 1f)
    }

    override fun isThemeElevationOverlayEnabled(): Boolean = true

    override fun getThemeElevationOverlayColor(): Int {
        return liftedAppBarSurfaceColor
    }
}
