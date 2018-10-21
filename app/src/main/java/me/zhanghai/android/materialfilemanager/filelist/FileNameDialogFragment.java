/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.util.FileNameUtils;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public abstract class FileNameDialogFragment extends AppCompatDialogFragment {

    @BindView(R.id.name_layout)
    protected TextInputLayout mNameLayout;
    @BindView(R.id.name)
    protected EditText mNameEdit;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), getTheme())
                .setTitle(getTitleRes());
        View contentView = ViewUtils.inflate(R.layout.file_name_dialog, builder.getContext());
        ButterKnife.bind(this, contentView);
        ViewUtils.hideTextInputLayoutErrorOnTextChange(mNameEdit, mNameLayout);
        mNameEdit.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.hasNoModifiers())) {
                onOk();
                return true;
            }
            return false;
        });
        AlertDialog dialog = builder
                .setView(contentView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        // Override the listener here so that we have control over when to close the dialog.
        dialog.setOnShowListener(dialog2 -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> onOk()));
        return dialog;
    }

    protected abstract int getTitleRes();

    private void onOk() {
        String name = mNameEdit.getText().toString();
        if (isNameUnchanged(name)) {
            dismiss();
            return;
        }
        if (TextUtils.isEmpty(name)) {
            mNameLayout.setError(mNameLayout.getContext().getString(
                    R.string.file_name_error_empty));
            return;
        }
        if (!FileNameUtils.isValidFileName(name)) {
            mNameLayout.setError(mNameLayout.getContext().getString(
                    R.string.file_name_error_invalid));
            return;
        }
        Listener listener = getListener();
        if (listener.hasFileWithName(name)) {
            mNameLayout.setError(mNameLayout.getContext().getString(
                    R.string.file_name_error_already_exists));
            return;
        }
        onOk(name);
        dismiss();
    }

    protected boolean isNameUnchanged(@NonNull String name) {
        return false;
    }

    protected abstract void onOk(@NonNull String name);

    @NonNull
    protected Listener getListener() {
        return (Listener) getParentFragment();
    }

    protected interface Listener {
        boolean hasFileWithName(@NonNull String name);
    }
}
