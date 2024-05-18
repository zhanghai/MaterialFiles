/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.token

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing

object Easings {
    val Standard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val Emphasized = Standard
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val StandardDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    val StandardAccelerate = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
    val Linear = LinearEasing
}
