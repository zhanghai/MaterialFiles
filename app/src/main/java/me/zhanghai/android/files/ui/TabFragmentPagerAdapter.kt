/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabFragmentPagerAdapter(
    fragmentManager: FragmentManager,
    private vararg val tabs: Pair<CharSequence?, () -> Fragment>
) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment = tabs[position].second()

    override fun getCount(): Int = tabs.size

    override fun getPageTitle(position: Int): CharSequence? = tabs[position].first
}
