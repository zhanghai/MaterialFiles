/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.databinding.ActivityFtpServerBiometricBinding
import me.zhanghai.android.files.util.BiometricAuthenticator

class FtpServerBiometricActivity : AppActivity() {
    private lateinit var binding: ActivityFtpServerBiometricBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFtpServerBiometricBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    finish()
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        binding.touchOutside.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.authenticateButton.setOnClickListener {
            startAuthentication()
        }

        // Automatically start authentication after a short delay to let the bottom sheet finish animating
        binding.root.postDelayed({
            if (!isFinishing) {
                startAuthentication()
            }
        }, 500)
    }

    private fun startAuthentication() {
        BiometricAuthenticator.authenticate(
            this,
            getString(R.string.ftp_server_biometric_title),
            getString(R.string.ftp_server_biometric_subtitle),
            onSuccess = {
                FtpServerService.toggle(this)
                finish()
            },
            onError = { _, _ ->
                // Keep the activity open to show the instructions and a button to retry
            }
        )
    }

    @Suppress("DEPRECATION")
    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
