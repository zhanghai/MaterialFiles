/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
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
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.util.FileNameUtils;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class RenameFileDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = RenameFileDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    @BindView(R.id.name_layout)
    TextInputLayout mNameLayout;
    @BindView(R.id.name)
    EditText mNameEdit;

    private File mFile;

    public static RenameFileDialogFragment newInstance(File file) {
        //noinspection deprecation
        RenameFileDialogFragment fragment = new RenameFileDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    public static void show(File file, Fragment fragment) {
        RenameFileDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(File)} instead.
     */
    public RenameFileDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        View contentView = ViewUtils.inflate(R.layout.rename_file_dialog, context);
        ButterKnife.bind(this, contentView);
        if (savedInstanceState == null) {
            String name = mFile.getName();
            mNameEdit.setText(name);
            int selectionEnd;
            if (mFile.isDirectory()) {
                selectionEnd = name.length();
            } else {
                selectionEnd = FileNameUtils.indexOfExtensionSeparator(name);
                if (selectionEnd == -1) {
                    selectionEnd = name.length();
                }
            }
            mNameEdit.setSelection(0, selectionEnd);
        }
        ViewUtils.hideTextInputLayoutErrorOnTextChange(mNameEdit, mNameLayout);
        mNameEdit.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.hasNoModifiers())) {
                onOk();
                return true;
            }
            return false;
        });
        AlertDialog dialog = new AlertDialog.Builder(context, getTheme())
                .setTitle(R.string.file_rename_title)
                .setView(contentView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setOnShowListener(dialog2 -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> onOk()));
        return dialog;
    }

    private void onOk() {
        String name = mNameEdit.getText().toString();
        if (TextUtils.equals(name, mFile.getName())) {
            dismiss();
            return;
        }
        if (!FileNameUtils.isValidFileName(name)) {
            mNameLayout.setError(mNameLayout.getContext().getString(
                    R.string.file_rename_error_invalid_name));
            return;
        }
        Listener listener = getListener();
        if (listener.hasFileWithName(name)) {
            mNameLayout.setError(mNameLayout.getContext().getString(
                    R.string.file_rename_error_name_already_exists));
            return;
        }
        listener.renameFile(mFile, name);
        dismiss();
    }

    private Listener getListener() {
        return (Listener) getParentFragment();
    }

    public interface Listener {
        boolean hasFileWithName(String name);
        void renameFile(File file, String name);
    }
}
