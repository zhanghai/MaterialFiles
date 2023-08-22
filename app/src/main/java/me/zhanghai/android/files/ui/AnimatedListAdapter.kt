/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.getAnimation

abstract class AnimatedListAdapter<T, VH : RecyclerView.ViewHolder>(
    callback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(callback) {
    private var isAnimating = false

    private var animationStartOffset = 0

    private val stopAnimationHandler = Handler(Looper.getMainLooper())
    private val stopAnimationRunnable = Runnable { stopAnimation() }

    private val clearAnimationListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            clearAnimation()
        }
    }

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
        recyclerView.addOnScrollListener(clearAnimationListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        recyclerView.removeOnScrollListener(clearAnimationListener)
        this.recyclerView = null
    }

    override fun refresh() {
        resetAnimation()
        super.refresh()
    }

    override fun replace(list: List<T>, clear: Boolean) {
        if (clear) {
            resetAnimation()
        }
        super.replace(list, clear)
    }

    override fun clear() {
        resetAnimation()
        super.clear()
    }

    protected fun bindViewHolderAnimation(holder: VH) {
        holder.itemView.clearAnimation()
        if (isAnimating) {
            val animation = holder.itemView.context.getAnimation(R.anim.list_item)
                .apply { startOffset = animationStartOffset.toLong() }
            animationStartOffset += ANIMATION_STAGGER_MILLIS
            holder.itemView.startAnimation(animation)
            postStopAnimation()
        }
    }

    private fun stopAnimation() {
        stopAnimationHandler.removeCallbacks(stopAnimationRunnable)
        isAnimating = false
        animationStartOffset = 0
    }

    private fun postStopAnimation() {
        stopAnimationHandler.removeCallbacks(stopAnimationRunnable)
        stopAnimationHandler.post(stopAnimationRunnable)
    }

    private fun clearAnimation() {
        stopAnimation()
        recyclerView?.let {
            for (index in 0..<it.childCount) {
                it.getChildAt(index).clearAnimation()
            }
        }
    }

    private fun resetAnimation() {
        clearAnimation()
        isAnimating = isAnimationEnabled
    }

    protected open val isAnimationEnabled: Boolean
        get() = true

    companion object {
        private const val ANIMATION_STAGGER_MILLIS = 20
    }
}
