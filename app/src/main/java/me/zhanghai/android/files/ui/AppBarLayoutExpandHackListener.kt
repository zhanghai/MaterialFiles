/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import me.zhanghai.android.files.util.AnimationConfig

class AppBarLayoutExpandHackListener(
    private val recyclerView: RecyclerView
) : AppBarLayout.OnOffsetChangedListener {
    private val offsetAnimationMaxEndTime = (AnimationUtils.currentAnimationTimeMillis()
        + AnimationConfig.getAnimDuration(MAX_OFFSET_ANIMATION_DURATION.toLong()))

    private var lastVerticalOffset: Int? = null

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (verticalOffset == 0
            || AnimationUtils.currentAnimationTimeMillis() > offsetAnimationMaxEndTime) {
            // AppBarLayout crashes with IndexOutOfBoundsException when a non-last listener removes
            // itself, so we have to do the removal asynchronously.
            appBarLayout.postOnAnimation { appBarLayout.removeOnOffsetChangedListener(this) }
        }
        val lastVerticalOffset = lastVerticalOffset
        this.lastVerticalOffset = verticalOffset
        if (lastVerticalOffset != null) {
            recyclerView.scrollBy(0, verticalOffset - lastVerticalOffset)
        }
    }

    companion object {
        // @see AppBarLayout.BaseBehavior.MAX_OFFSET_ANIMATION_DURATION
        private const val MAX_OFFSET_ANIMATION_DURATION = 600
    }
}
