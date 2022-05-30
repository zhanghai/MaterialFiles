/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import coil.request.ImageRequest
import coil.transition.CrossfadeTransition

fun ImageRequest.Builder.fadeIn(durationMillis: Int): ImageRequest.Builder =
    apply {
        placeholder(android.R.color.transparent)
        transitionFactory(CrossfadeTransition.Factory(durationMillis, true))
    }
