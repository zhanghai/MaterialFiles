/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.graphics.drawable.RotateDrawable
import android.os.Build
import me.zhanghai.android.files.util.lazyReflectedField
import kotlin.reflect.KClass

fun KClass<RotateDrawable>.createCompat(): RotateDrawable =
    RotateDrawable().apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            isPivotXRelativeCompat = true
            pivotXCompat = 0.5f
            isPivotYRelativeCompat = true
            pivotYCompat = 0.5f
        }
    }

private val rotateDrawableMStateField by lazyReflectedField(RotateDrawable::class.java, "mState")
private val ROTATE_STATE_CLASS_NAME = "${RotateDrawable::class.java.name}\$RotateState"
private val rotateStateMPivotXRelField by lazyReflectedField(ROTATE_STATE_CLASS_NAME, "mPivotXRel")

var RotateDrawable.isPivotXRelativeCompat: Boolean
    get() = isPivotXRelative
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            isPivotXRelative = value
        } else {
            if (isPivotXRelative != value) {
                rotateStateMPivotXRelField.setBoolean(rotateDrawableMStateField.get(this), value)
                invalidateSelf()
            }
        }
    }

private val rotateStateMPivotXField by lazyReflectedField(ROTATE_STATE_CLASS_NAME, "mPivotX")

var RotateDrawable.pivotXCompat: Float
    get() = pivotX
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            pivotX = value
        } else {
            if (pivotX != value) {
                rotateStateMPivotXField.setFloat(rotateDrawableMStateField.get(this), value)
                invalidateSelf()
            }
        }
    }

private val rotateStateMPivotYRelField by lazyReflectedField(ROTATE_STATE_CLASS_NAME, "mPivotYRel")

var RotateDrawable.isPivotYRelativeCompat: Boolean
    get() = isPivotYRelative
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            isPivotYRelative = value
        } else {
            if (isPivotYRelative != value) {
                rotateStateMPivotYRelField.setBoolean(rotateDrawableMStateField.get(this), value)
                invalidateSelf()
            }
        }
    }

private val rotateStateMPivotYField by lazyReflectedField(ROTATE_STATE_CLASS_NAME, "mPivotY")

var RotateDrawable.pivotYCompat: Float
    get() = pivotY
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            pivotY = value
        } else {
            if (pivotY != value) {
                rotateStateMPivotYField.setFloat(rotateDrawableMStateField.get(this), value)
                invalidateSelf()
            }
        }
    }
