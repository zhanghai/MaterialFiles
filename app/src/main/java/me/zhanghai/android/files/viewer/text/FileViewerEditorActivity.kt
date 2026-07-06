/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import java8.nio.file.Path
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.util.extraPathList

class FileViewerEditorActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val paths = intent.extraPathList
        val position = intent.getIntExtra(EXTRA_POSITION, 0)

        if (paths.isEmpty()) {
            finish()
            return
        }

        val viewPager = ViewPager2(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            offscreenPageLimit = 1
            adapter = FileViewerEditorAdapter(this@FileViewerEditorActivity, paths)
            setCurrentItem(position, false)
        }
        setContentView(viewPager)
    }

    companion object {
        private const val EXTRA_POSITION =
            "me.zhanghai.android.files.viewer.text.FileViewerEditorActivity.extra.POSITION"

        fun putExtras(intent: Intent, paths: List<Path>, position: Int) {
            intent.extraPathList = paths
            intent.putExtra(EXTRA_POSITION, position)
        }
    }
}
