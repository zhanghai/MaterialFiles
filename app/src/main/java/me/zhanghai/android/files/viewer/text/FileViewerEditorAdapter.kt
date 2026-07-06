/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java8.nio.file.Path
import me.zhanghai.android.files.util.extraPath
import me.zhanghai.android.files.util.putArgs

class FileViewerEditorAdapter(
    activity: FragmentActivity,
    private val paths: List<Path>
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = paths.size

    override fun createFragment(position: Int): Fragment {
        val filePath = paths[position]
        val intent = Intent().apply { extraPath = filePath }
        return TextEditorFragment()
            .putArgs(TextEditorFragment.Args(intent, startInPreview = true))
    }
}
