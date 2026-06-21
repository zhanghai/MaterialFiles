/*
 * Copyright (c) 2026 Material Files contributors
 * SPDX‑License‑Identifier: GPL-3.0-or-later
 */

package me.zhanghai.android.files.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.putArgs

class SftpHostKeyChangedDialogActivity : AppActivity() {
    private val args by args<SftpHostKeyChangedDialogFragment.Args>()

    private lateinit var fragment: SftpHostKeyChangedDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            fragment = SftpHostKeyChangedDialogFragment().putArgs(args)
            supportFragmentManager.commit {
                add(fragment, SftpHostKeyChangedDialogFragment::class.java.name)
            }
        } else {
            fragment = supportFragmentManager.findFragmentByTag(
                SftpHostKeyChangedDialogFragment::class.java.name
            ) as SftpHostKeyChangedDialogFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isFinishing) {
            fragment.onFinish()
        }
    }
}
