/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.appcompat.widget.Toolbar
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import me.zhanghai.android.files.util.shortAnimTime

class CrossfadeSubtitleToolbar : Toolbar {
    private val subtitleAnimator = ObjectAnimator.ofFloat(null, View.ALPHA, 1f, 0f, 1f).apply {
        duration = (2 * context.shortAnimTime).toLong()
        interpolator = FastOutSlowInInterpolator()
        val listener = AnimatorListener()
        addUpdateListener(listener)
        addListener(listener)
    }

    private var nextSubtitle: CharSequence? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    override fun getSubtitle(): CharSequence? = nextSubtitle ?: super.getSubtitle()

    override fun setSubtitle(subtitle: CharSequence?) {
        if (getSubtitle() == subtitle) {
            return
        }
        nextSubtitle = subtitle
        ensureSubtitleAnimatorTarget()
        if (subtitleAnimator.target == null) {
            // Subtitle text view not available (yet), just delegate to super.
            super.setSubtitle(subtitle)
            return
        }
        if (!subtitleAnimator.isRunning) {
            subtitleAnimator.start()
        }
    }

    private fun ensureSubtitleAnimatorTarget() {
        if (subtitleAnimator.target != null) {
            return
        }
        val subtitleTextView = try {
            Toolbar::class.java.getDeclaredField("mSubtitleTextView")
                .apply { isAccessible = true }
                .get(this) as TextView?
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: return
        subtitleAnimator.target = subtitleTextView
    }

    private inner class AnimatorListener : AnimatorListenerAdapter(), AnimatorUpdateListener {
        private var isTextUpdated = false

        override fun onAnimationUpdate(animator: ValueAnimator) {
            if (animator.animatedFraction < 0.5) {
                isTextUpdated = false
            } else {
                ensureTextUpdated()
            }
        }

        override fun onAnimationEnd(animator: Animator) {
            ensureTextUpdated()
            if (nextSubtitle != null) {
                isTextUpdated = false
                animator.start()
            }
        }

        private fun ensureTextUpdated() {
            if (!isTextUpdated) {
                if (nextSubtitle != null) {
                    super@CrossfadeSubtitleToolbar.setSubtitle(nextSubtitle)
                    nextSubtitle = null
                }
                isTextUpdated = true
            }
        }
    }
}
