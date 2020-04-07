/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.image

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import java8.nio.file.Path
import kotlinx.android.parcel.Parcelize
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.extraPathList
import me.zhanghai.android.files.util.getArgsOrNull
import me.zhanghai.android.files.util.putArgs

class ImageViewerActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            val intent = intent
            val args = intent.extras?.getArgsOrNull<Args>()
            val fragment = ImageViewerFragment()
                .putArgs(ImageViewerFragment.Args(intent, args?.position))
            supportFragmentManager.commit { add(android.R.id.content, fragment) }
        }
    }

    companion object {
        fun putExtras(intent: Intent, paths: List<Path>, position: Int) {
            intent.extraPathList = paths
            intent.putArgs(Args(position))
        }
    }

    @Parcelize
    class Args(val position: Int?) : ParcelableArgs
}
