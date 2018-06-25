/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.util.FileNameUtils;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;

public class RenameFileDialogFragment extends FileNameDialogFragment {

    private static final String KEY_PREFIX = RenameFileDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

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
        Dialog dialog = super.onCreateDialog(savedInstanceState);
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
        return dialog;
    }

    @Override
    protected int getTitleRes() {
        return R.string.file_rename_title;
    }

    @Override
    protected boolean isNameUnchanged(String name) {
        return TextUtils.equals(name, mFile.getName());
    }

    @Override
    protected void onOk(String name) {
        getListener().renameFile(mFile, name);
    }

    @Override
    protected Listener getListener() {
        return (Listener) getParentFragment();
    }

    public interface Listener extends FileNameDialogFragment.Listener {
        void renameFile(File file, String name);
    }
}
