/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.FileNameUtils;
import me.zhanghai.android.files.util.ViewUtils;

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
        View contentView = ViewUtils.inflate(getLayoutRes(), builder.getContext());
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

    @StringRes
    protected abstract int getTitleRes();

    @LayoutRes
    protected int getLayoutRes() {
        return R.layout.file_name_dialog;
    }

    private void onOk() {
        String name = getName();
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

    @NonNull
    protected String getName() {
        return mNameEdit.getText().toString();
    }

    protected boolean isNameUnchanged(@NonNull String name) {
        return false;
    }

    protected abstract void onOk(@NonNull String name);

    @NonNull
    protected Listener getListener() {
        return (Listener) requireParentFragment();
    }

    protected interface Listener {
        boolean hasFileWithName(@NonNull String name);
    }
}
