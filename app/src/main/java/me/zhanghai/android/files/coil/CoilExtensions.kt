/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import coil.request.ImageRequest
import coil.transition.CrossfadeTransition
import me.zhanghai.android.files.util.AnimationConfig

fun ImageRequest.Builder.fadeIn(durationMillis: Int): ImageRequest.Builder =
    apply {
        placeholder(android.R.color.transparent)
        val animDuration = AnimationConfig.getAnimDuration(durationMillis)
        if (animDuration > 0) {
            transitionFactory(CrossfadeTransition.Factory(animDuration, true))
        } else {
            transitionFactory(CrossfadeTransition.Factory(0, false))
        }
    }
