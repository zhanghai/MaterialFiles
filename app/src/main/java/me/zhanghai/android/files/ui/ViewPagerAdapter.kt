/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

abstract class ViewPagerAdapter : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): View =
        onCreateView(container, position)

    protected abstract fun onCreateView(container: ViewGroup, position: Int): View

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        onDestroyView(container, position, item as View)
        container.removeView(item)
    }

    protected abstract fun onDestroyView(container: ViewGroup, position: Int, view: View)

    override fun isViewFromObject(view: View, item: Any): Boolean = view === item

    override fun getItemPosition(item: Any): Int = getViewPosition(item as View)

    protected open fun getViewPosition(view: View): Int = POSITION_UNCHANGED
}
