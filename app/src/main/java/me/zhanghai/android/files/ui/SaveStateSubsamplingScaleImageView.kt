/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.util.ParcelableState

class SaveStateSubsamplingScaleImageView : SubsamplingScaleImageView {
    private var pendingState: ImageViewState? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attr: AttributeSet?) : super(context, attr)

    fun setImageRestoringSavedState(imageSource: ImageSource) {
        setImage(imageSource, pendingState)
        pendingState = null
    }

    override fun onSaveInstanceState(): Parcelable = State(super.onSaveInstanceState(), state)

    override fun onRestoreInstanceState(state: Parcelable) {
        state as State
        super.onRestoreInstanceState(state.superState)
        pendingState = state.state
    }

    @Parcelize
    private class State(val superState: Parcelable?, val state: ImageViewState?) : ParcelableState
}
