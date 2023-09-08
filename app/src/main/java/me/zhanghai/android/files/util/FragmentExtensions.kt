/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.AnyRes
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.InterpolatorRes
import androidx.annotation.PluralsRes
import androidx.fragment.app.Fragment
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.checkSelfPermissionCompat
import me.zhanghai.android.files.compat.getColorCompat
import me.zhanghai.android.files.compat.getColorStateListCompat
import me.zhanghai.android.files.compat.getDrawableCompat

fun Fragment.checkSelfPermission(permission: String): Int =
    requireContext().checkSelfPermissionCompat(permission)

fun Fragment.finish() {
    requireActivity().finish()
}

fun Fragment.getAnimation(@AnimRes id: Int): Animation = requireContext().getAnimation(id)

fun Fragment.getBoolean(@BoolRes id: Int): Boolean = requireContext().getBoolean(id)

@ColorInt
fun Fragment.getColor(@ColorRes id: Int): Int = requireContext().getColorCompat(id)

fun Fragment.getColorStateList(@ColorRes id: Int): ColorStateList =
    requireContext().getColorStateListCompat(id)

@Dimension
fun Fragment.getDimension(@DimenRes id: Int): Float = requireContext().getDimension(id)

@Dimension(unit = Dimension.DP)
fun Fragment.getDimensionDp(@DimenRes id: Int): Float = requireContext().getDimensionDp(id)

@Dimension
fun Fragment.getDimensionPixelOffset(@DimenRes id: Int): Int =
    requireContext().getDimensionPixelOffset(id)

@Dimension
fun Fragment.getDimensionPixelSize(@DimenRes id: Int): Int =
    requireContext().getDimensionPixelSize(id)

fun Fragment.getDrawable(@DrawableRes id: Int): Drawable = requireContext().getDrawableCompat(id)

fun Fragment.getFloat(@DimenRes id: Int): Float = requireContext().getFloat(id)

fun Fragment.getInteger(@IntegerRes id: Int): Int = requireContext().getInteger(id)

fun Fragment.getInterpolator(@InterpolatorRes id: Int): Interpolator =
    requireContext().getInterpolator(id)

fun Fragment.getQuantityString(@PluralsRes id: Int, quantity: Int): String =
    requireContext().getQuantityString(id, quantity)

fun Fragment.getQuantityString(
    @PluralsRes id: Int,
    quantity: Int,
    vararg formatArgs: Any?
): String = requireContext().getQuantityString(id, quantity, *formatArgs)

fun Fragment.getQuantityText(@PluralsRes id: Int, quantity: Int): CharSequence =
    requireContext().getQuantityText(id, quantity)

fun Fragment.getStringArray(@ArrayRes id: Int) = requireContext().getStringArray(id)

fun Fragment.getTextArray(@ArrayRes id: Int): Array<CharSequence> =
    requireContext().getTextArray(id)

fun Fragment.getBooleanByAttr(@AttrRes attr: Int) = requireContext().getBooleanByAttr(attr)

@ColorInt
fun Fragment.getColorByAttr(@AttrRes attr: Int) = requireContext().getColorByAttr(attr)

fun Fragment.getColorStateListByAttr(@AttrRes attr: Int) =
    requireContext().getColorStateListByAttr(attr)

fun Fragment.getDimensionByAttr(@AttrRes attr: Int) = requireContext().getDimensionByAttr(attr)

fun Fragment.getDimensionPixelOffsetByAttr(@AttrRes attr: Int) =
    requireContext().getDimensionPixelOffsetByAttr(attr)

fun Fragment.getDimensionPixelSizeByAttr(@AttrRes attr: Int): Int =
    requireContext().getDimensionPixelSizeByAttr(attr)

fun Fragment.getDrawableByAttr(@AttrRes attr: Int) = requireContext().getDrawableByAttr(attr)

fun Fragment.getFloatByAttr(@AttrRes attr: Int) = requireContext().getFloatByAttr(attr)

@AnyRes
fun Fragment.getResourceIdByAttr(@AttrRes attr: Int): Int =
    requireContext().getResourceIdByAttr(attr)

@Dimension
fun Fragment.dpToDimension(@Dimension(unit = Dimension.DP) dp: Float) =
    requireContext().dpToDimension(dp)

@Dimension
fun Fragment.dpToDimension(@Dimension(unit = Dimension.DP) dp: Int) =
    requireContext().dpToDimension(dp)

@Dimension
fun Fragment.dpToDimensionPixelOffset(@Dimension(unit = Dimension.DP) dp: Float) =
    requireContext().dpToDimensionPixelOffset(dp)

@Dimension
fun Fragment.dpToDimensionPixelOffset(@Dimension(unit = Dimension.DP) dp: Int) =
    requireContext().dpToDimensionPixelOffset(dp)

@Dimension
fun Fragment.dpToDimensionPixelSize(@Dimension(unit = Dimension.DP) dp: Float) =
    requireContext().dpToDimensionPixelSize(dp)

@Dimension
fun Fragment.dpToDimensionPixelSize(@Dimension(unit = Dimension.DP) dp: Int) =
    requireContext().dpToDimensionPixelSize(dp)

@Dimension(unit = Dimension.DP)
fun Fragment.dimensionToDp(@Dimension dimension: Float): Float =
    requireContext().dimensionToDp(dimension)

@Dimension(unit = Dimension.DP)
fun Fragment.dimensionToDp(@Dimension dimension: Int): Float =
    requireContext().dimensionToDp(dimension)

fun Fragment.setResult(resultCode: Int, resultData: Intent? = null) =
    requireActivity().setResult(resultCode, resultData)

val Fragment.shortAnimTime
    get() = requireContext().shortAnimTime

val Fragment.mediumAnimTime
    get() = requireContext().mediumAnimTime

val Fragment.longAnimTime
    get() = requireContext().longAnimTime

fun Fragment.showToast(textRes: Int, duration: Int = Toast.LENGTH_SHORT) =
    requireContext().showToast(textRes, duration)

fun Fragment.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    requireContext().showToast(text, duration)

fun Fragment.startActivitySafe(intent: Intent, options: Bundle? = null) {
    try {
        startActivity(intent, options)
    } catch (e: ActivityNotFoundException) {
        showToast(R.string.activity_not_found)
    }
}

fun Fragment.startActivityForResultSafe(intent: Intent, requestCode: Int, options: Bundle? = null) {
    try {
        startActivityForResult(intent, requestCode, options)
    } catch (e: ActivityNotFoundException) {
        showToast(R.string.activity_not_found)
    }
}
