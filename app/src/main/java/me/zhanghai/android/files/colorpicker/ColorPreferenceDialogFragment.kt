/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.colorpicker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.GridView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.ui.MaterialPreferenceDialogFragmentCompat
import me.zhanghai.android.files.util.ParcelableState
import me.zhanghai.android.files.util.getState
import me.zhanghai.android.files.util.putState
import me.zhanghai.android.files.util.withTheme

class ColorPreferenceDialogFragment : MaterialPreferenceDialogFragmentCompat() {
    override val preference: BaseColorPreference
        get() = super.preference as BaseColorPreference

    private lateinit var colors: IntArray
    private var checkedColor = 0
    private var defaultColor = 0

    private lateinit var paletteGrid: GridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val preference = preference
            colors = preference.entryValues
            checkedColor = preference.value
            defaultColor = preference.defaultValue
        } else {
            val state = savedInstanceState.getState<State>()
            colors = state.colors
            checkedColor = state.checkedColor
            defaultColor = state.defaultColor
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val checkedPosition = paletteGrid.checkedItemPosition
        val checkedColor = if (checkedPosition != -1) colors[checkedPosition] else checkedColor
        outState.putState(State(colors, checkedColor, defaultColor))
    }

    override fun onCreateDialogView(context: Context): View? =
        super.onCreateDialogView(context.withTheme(theme))

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        paletteGrid = ViewCompat.requireViewById(view, R.id.palette)
        paletteGrid.adapter = ColorPaletteAdapter(colors)
        val checkedPosition = colors.indexOf(checkedColor)
        if (checkedPosition != -1) {
            paletteGrid.setItemChecked(checkedPosition, true)
        }
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)

        if (defaultColor in colors) {
            builder.setNeutralButton(R.string.default_, null)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        (super.onCreateDialog(savedInstanceState) as AlertDialog).apply {
            if (defaultColor in colors) {
                // Override the listener here so that we won't close the dialog.
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        paletteGrid.setItemChecked(colors.indexOf(defaultColor), true)
                    }
                }
            }
        }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (!positiveResult) {
            return
        }
        val checkedPosition = paletteGrid.checkedItemPosition
        if (checkedPosition == -1) {
            return
        }
        val checkedColor = colors[checkedPosition]
        preference.value = checkedColor
    }

    @Parcelize
    private class State(
        val colors: IntArray,
        val checkedColor: Int,
        val defaultColor: Int
    ) : ParcelableState
}
