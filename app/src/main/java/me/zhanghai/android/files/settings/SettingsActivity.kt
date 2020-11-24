/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.annotation.StyleRes
import androidx.fragment.app.add
import androidx.fragment.app.commit
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.theme.custom.CustomThemeHelper.OnThemeChangedListener
import me.zhanghai.android.files.theme.night.NightModeHelper.OnNightModeChangedListener
import me.zhanghai.android.files.util.BundleParceler
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.getArgsOrNull
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.startActivitySafe

class SettingsActivity : AppActivity(), OnThemeChangedListener, OnNightModeChangedListener {
    private var isRestarting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val args = intent.extras?.getArgsOrNull<Args>()
        val savedInstanceState = savedInstanceState ?: args?.savedInstanceState
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            supportFragmentManager.commit { add<SettingsFragment>(android.R.id.content) }
        }
    }

    override fun onThemeChanged(@StyleRes theme: Int) {
        // ActivityCompat.recreate() may call ActivityRecreator.recreate() without calling
        // Activity.recreate(), so we cannot simply override it. To work around this, we just
        // manually call restart().
        restart()
    }

    override fun onNightModeChangedFromHelper(nightMode: Int) {
        // ActivityCompat.recreate() may call ActivityRecreator.recreate() without calling
        // Activity.recreate(), so we cannot simply override it. To work around this, we just
        // manually call restart().
        restart()
    }

    private fun restart() {
        val savedInstanceState = Bundle().apply {
            onSaveInstanceState(this)
        }
        finish()
        val intent = SettingsActivity::class.createIntent().putArgs(Args(savedInstanceState))
        startActivitySafe(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        isRestarting = true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return isRestarting || super.dispatchKeyEvent(event)
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyShortcutEvent(event: KeyEvent): Boolean {
        return isRestarting || super.dispatchKeyShortcutEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return isRestarting || super.dispatchTouchEvent(event)
    }

    override fun dispatchTrackballEvent(event: MotionEvent): Boolean {
        return isRestarting || super.dispatchTrackballEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        return isRestarting || super.dispatchGenericMotionEvent(event)
    }

    @Parcelize
    class Args(val savedInstanceState: @WriteWith<BundleParceler> Bundle?) : ParcelableArgs
}
