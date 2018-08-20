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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.compat.Predicate;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;

public class ConfirmDeleteFilesDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = ConfirmDeleteFilesDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILES = KEY_PREFIX + "FILES";

    private ArrayList<File> mFiles;

    private static ConfirmDeleteFilesDialogFragment newInstance(ArrayList<File> files) {
        //noinspection deprecation
        ConfirmDeleteFilesDialogFragment fragment = new ConfirmDeleteFilesDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelableArrayList(EXTRA_FILES, files);
        return fragment;
    }

    public static ConfirmDeleteFilesDialogFragment newInstance(File file) {
        return newInstance(Collections.singletonList(file));
    }

    public static ConfirmDeleteFilesDialogFragment newInstance(List<File> files) {
        return newInstance(new ArrayList<>(files));
    }

    public static void show(File file, Fragment fragment) {
        ConfirmDeleteFilesDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }

    public static void show(List<File> files, Fragment fragment) {
        ConfirmDeleteFilesDialogFragment.newInstance(files)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(File)} instead.
     */
    public ConfirmDeleteFilesDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFiles = getArguments().getParcelableArrayList(EXTRA_FILES);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message;
        if (mFiles.size() == 1) {
            File file = mFiles.get(0);
            int messageRes = file.isDirectory() ? R.string.file_delete_message_directory_format
                    : R.string.file_delete_message_file_format;
            message = getString(messageRes, file.getName());
        } else {
            boolean allDirectories = Functional.every(mFiles, File::isDirectory);
            boolean allFiles = Functional.every(mFiles, file -> !file.isDirectory());
            int messageRes = allDirectories ?
                    R.string.file_delete_message_multiple_directories_format
                    : allFiles ? R.string.file_delete_message_multiple_files_format
                    : R.string.file_delete_message_multiple_mixed_format;
            message = getString(messageRes, mFiles.size());
        }
        return new AlertDialog.Builder(requireContext(), getTheme())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> getListener()
                        .deleteFiles(mFiles))
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private Listener getListener() {
        return (Listener) getParentFragment();
    }

    public interface Listener {
        void deleteFiles(List<File> files);
    }
}
