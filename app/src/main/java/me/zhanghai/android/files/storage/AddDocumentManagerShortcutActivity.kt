/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.putArgs

class AddDocumentManagerShortcutActivity : AppActivity() {
    private val args by args<AddDocumentManagerShortcutFragment.Args>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            val fragment = AddDocumentManagerShortcutFragment().putArgs(args)
            supportFragmentManager.commit {
                add(fragment, AddDocumentManagerShortcutFragment::class.java.name)
            }
        }
    }
}
