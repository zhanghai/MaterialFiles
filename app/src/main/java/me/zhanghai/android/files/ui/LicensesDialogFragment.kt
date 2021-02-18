/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.NoticesXmlParser
import de.psdev.licensesdialog.model.Notices
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.ParcelableState
import me.zhanghai.android.files.util.getState
import me.zhanghai.android.files.util.putState
import me.zhanghai.android.files.util.show

class LicensesDialogFragment : AppCompatDialogFragment() {
    private lateinit var notices: Notices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notices = if (savedInstanceState != null) {
            savedInstanceState.getState<State>().notices
        } else {
            NoticesXmlParser.parse(resources.openRawResource(R.raw.licenses))
                .apply { addNotice(LicensesDialog.LICENSES_DIALOG_NOTICE) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putState(State(notices))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(R.string.about_licenses_title)
            .apply { setLicensesView(notices) }
            .setNegativeButton(R.string.close, null)
            .create()

    companion object {
        fun show(fragment: Fragment) {
            LicensesDialogFragment().show(fragment)
        }
    }

    @Parcelize
    private class State(val notices: Notices) : ParcelableState
}
