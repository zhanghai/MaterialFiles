/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.preference.DialogPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.util.ParcelableState
import me.zhanghai.android.files.util.getState
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.putState
import me.zhanghai.android.files.util.toBitmapDrawable

/**
 * @see androidx.preference.PreferenceDialogFragmentCompat
 */
abstract class MaterialPreferenceDialogFragmentCompat : AppCompatDialogFragment(),
    DialogInterface.OnClickListener {

    open val preference: DialogPreference by lazy {
        val fragment = targetFragment
        check(fragment is DialogPreference.TargetFragment) {
            "Target fragment must implement TargetFragment interface"
        }
        val key = requireArguments().getString(ARG_KEY)!!
        fragment.findPreference(key)!!
    }

    private var dialogTitle: CharSequence? = null
    private var positiveButtonText: CharSequence? = null
    private var negativeButtonText: CharSequence? = null
    private var dialogMessage: CharSequence? = null
    @LayoutRes
    private var dialogLayoutRes: Int = 0
    private var dialogIcon: BitmapDrawable? = null

    /** Which button was clicked.  */
    private var whichButtonClicked = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            dialogTitle = preference.dialogTitle
            positiveButtonText = preference.positiveButtonText
            negativeButtonText = preference.negativeButtonText
            dialogMessage = preference.dialogMessage
            dialogLayoutRes = preference.dialogLayoutResource
            dialogIcon = preference.dialogIcon?.toBitmapDrawable(resources)
        } else {
            val state = savedInstanceState.getState<State>()
            dialogTitle = state.dialogTitle
            positiveButtonText = state.positiveButtonText
            negativeButtonText = state.negativeButtonText
            dialogMessage = state.dialogMessage
            dialogLayoutRes = state.dialogLayoutRes
            dialogIcon = state.dialogIcon?.let { BitmapDrawable(resources, it) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putState(
            State(
                dialogTitle, positiveButtonText, negativeButtonText, dialogMessage, dialogLayoutRes,
                dialogIcon?.bitmap
            )
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        whichButtonClicked = DialogInterface.BUTTON_NEGATIVE
        val dialog = MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(dialogTitle)
            .setIcon(dialogIcon)
            .setPositiveButton(positiveButtonText, this)
            .setNegativeButton(negativeButtonText, this)
            .apply {
                val contentView = onCreateDialogView(context)
                if (contentView != null) {
                    onBindDialogView(contentView)
                    setView(contentView)
                } else {
                    setMessage(dialogMessage)
                }
                onPrepareDialogBuilder(this)
            }
            .create()
        if (needInputMethod()) {
            requestInputMethod(dialog)
        }
        return dialog
    }

    /**
     * Prepares the dialog builder to be shown when the preference is clicked.
     * Use this to set custom properties on the dialog.
     *
     *
     * Do not [AlertDialog.Builder.create] or [AlertDialog.Builder.show].
     */
    open fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {}

    /**
     * Returns whether the preference needs to display a soft input method when the dialog is
     * displayed. Default is false. Subclasses should override this method if they need the soft
     * input method brought up automatically.
     *
     *
     * Note: If your application targets P or above, ensure your subclass manually requests
     * focus (ideally in [.onBindDialogView]) for the input field in order to
     * correctly attach the input method to the field.
     *
     * @hide
     */
    open fun needInputMethod(): Boolean = false

    /**
     * Sets the required flags on the dialog window to enable input method window to show up.
     */
    private fun requestInputMethod(dialog: Dialog) {
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    /**
     * Creates the content view for the dialog (if a custom content view is required).
     * By default, it inflates the dialog layout resource if it is set.
     *
     * @return The content view for the dialog
     * @see DialogPreference.setLayoutResource
     */
    open fun onCreateDialogView(context: Context): View? =
        if (dialogLayoutRes != 0) context.layoutInflater.inflate(dialogLayoutRes, null) else null

    /**
     * Binds views in the content view of the dialog to data.
     *
     *
     * Make sure to call through to the superclass implementation.
     *
     * @param view The content view of the dialog, if it is custom
     */
    open fun onBindDialogView(view: View) {
        val dialogMessageView = view.findViewById<View>(android.R.id.message)
        if (dialogMessageView != null) {
            var newVisibility = View.GONE
            if (!TextUtils.isEmpty(dialogMessage)) {
                if (dialogMessageView is TextView) {
                    dialogMessageView.text = dialogMessage
                }
                newVisibility = View.VISIBLE
            }
            if (dialogMessageView.visibility != newVisibility) {
                dialogMessageView.visibility = newVisibility
            }
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        whichButtonClicked = which
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        onDialogClosed(whichButtonClicked == DialogInterface.BUTTON_POSITIVE)
    }

    abstract fun onDialogClosed(positiveResult: Boolean)

    companion object {
        /*protected */const val ARG_KEY = "key"
    }

    @Parcelize
    private class State(
        val dialogTitle: CharSequence?,
        val positiveButtonText: CharSequence?,
        val negativeButtonText: CharSequence?,
        val dialogMessage: CharSequence?,
        @LayoutRes val dialogLayoutRes: Int,
        val dialogIcon: Bitmap?
    ) : ParcelableState
}
