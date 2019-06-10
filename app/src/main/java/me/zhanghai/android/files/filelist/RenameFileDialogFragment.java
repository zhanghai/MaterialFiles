/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.app.Dialog;
import android.os.Bundle;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.FileNameUtils;
import me.zhanghai.android.files.util.FragmentUtils;

public class RenameFileDialogFragment extends FileNameDialogFragment {

    private static final String KEY_PREFIX = RenameFileDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    @NonNull
    private FileItem mExtraFile;

    @NonNull
    public static RenameFileDialogFragment newInstance(@NonNull FileItem file) {
        //noinspection deprecation
        RenameFileDialogFragment fragment = new RenameFileDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    public static void show(@NonNull FileItem file, @NonNull Fragment fragment) {
        RenameFileDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(FileItem)} instead.
     */
    public RenameFileDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (savedInstanceState == null) {
            String name = FileUtils.getName(mExtraFile);
            mNameEdit.setText(name);
            int selectionEnd;
            if (mExtraFile.getAttributes().isDirectory()) {
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
    @StringRes
    protected int getTitleRes() {
        return R.string.file_rename_title;
    }

    @Override
    protected boolean isNameUnchanged(@NonNull String name) {
        return Objects.equals(name, FileUtils.getName(mExtraFile));
    }

    @Override
    protected void onOk(@NonNull String name) {
        getListener().renameFile(mExtraFile, name);
    }

    @NonNull
    @Override
    protected Listener getListener() {
        return (Listener) getParentFragment();
    }

    public interface Listener extends FileNameDialogFragment.Listener {
        void renameFile(@NonNull FileItem file, @NonNull String newName);
    }
}
