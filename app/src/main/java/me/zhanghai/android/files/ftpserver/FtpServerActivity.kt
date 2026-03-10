/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import android.os.Bundle
import android.view.View
import androidx.fragment.app.add
import androidx.fragment.app.commit
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.util.BiometricAuthenticator

class FtpServerActivity : AppActivity() {
    private var authenticated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)

        if (savedInstanceState != null) {
            authenticated = savedInstanceState.getBoolean(STATE_AUTHENTICATED, false)
        }

        if (!authenticated) {
            authenticate()
        } else if (savedInstanceState == null) {
            setupFragment()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_AUTHENTICATED, authenticated)
    }

    private fun authenticate() {
        BiometricAuthenticator.authenticate(
            this,
            getString(R.string.ftp_server_biometric_title),
            getString(R.string.ftp_server_biometric_subtitle),
            onSuccess = {
                authenticated = true
                setupFragment()
            },
            onError = { _, _ ->
                finish()
            }
        )
    }

    private fun setupFragment() {
        if (supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            supportFragmentManager.commit {
                add<FtpServerFragment>(android.R.id.content)
            }
        }
    }

    companion object {
        private const val STATE_AUTHENTICATED = "authenticated"
    }
}
