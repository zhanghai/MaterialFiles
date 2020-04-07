/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.NoticesXmlParser
import de.psdev.licensesdialog.model.Notices
import kotlinx.android.parcel.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.ParcelableState
import me.zhanghai.android.files.util.getState
import me.zhanghai.android.files.util.putState
import me.zhanghai.android.files.util.show
import me.zhanghai.android.files.util.valueCompat

class LicensesDialogFragment : AppCompatDialogFragment() {
    private lateinit var notices: Notices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notices = savedInstanceState?.getState<State>()?.notices
            ?: NoticesXmlParser.parse(resources.openRawResource(R.raw.licenses))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putState(State(notices))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        LicensesDialog.Builder(requireContext())
            .setThemeResourceId(theme)
            .setTitle(R.string.about_licenses_title)
            // setIncludeOwnLicense(true) will modify our notices instance.
            .setNotices(notices.copy())
            .setIncludeOwnLicense(true)
            .setNoticesCssStyle(
                if (Settings.MATERIAL_DESIGN_2.valueCompat) {
                    R.string.about_licenses_html_style_md2
                } else {
                    R.string.about_licenses_html_style
                }
            )
            .setCloseText(R.string.close)
            .build()
            .create()

    private fun Notices.copy(): Notices =
        Notices().apply { this@copy.notices.forEach { addNotice(it) } }

    companion object {
        fun show(fragment: Fragment) {
            LicensesDialogFragment().show(fragment)
        }
    }

    @Parcelize
    private class State(val notices: Notices) : ParcelableState
}
