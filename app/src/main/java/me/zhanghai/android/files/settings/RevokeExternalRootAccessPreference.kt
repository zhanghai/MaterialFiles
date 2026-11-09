/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.preference.Preference
import me.zhanghai.android.files.R
import me.zhanghai.android.files.documentsprovider.ExternalRootAccessGrants
import me.zhanghai.android.files.util.showToast

class RevokeExternalRootAccessPreference : Preference {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        isPersistent = false
        updateSummary()
    }

    override fun onClick() {
        ExternalRootAccessGrants.revokeAll()
        updateSummary()
        context.showToast(R.string.settings_revoke_external_root_access_done)
    }

    private fun updateSummary() {
        val count = ExternalRootAccessGrants.count
        summary = context.resources.getQuantityString(
            R.plurals.settings_revoke_external_root_access_summary_format, count, count
        )
        isEnabled = count > 0
    }
}
