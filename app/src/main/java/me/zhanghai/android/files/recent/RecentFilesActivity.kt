package me.zhanghai.android.files.recent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.util.createIntent

class RecentFilesActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(android.R.id.content, RecentFilesFragment())
            }
        }
    }

    companion object {
        fun createIntent(): Intent =
            RecentFilesActivity::class.createIntent()
    }
}
