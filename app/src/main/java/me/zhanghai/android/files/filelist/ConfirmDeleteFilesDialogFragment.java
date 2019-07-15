/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.app.Dialog;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.CollectionUtils;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.java.functional.Functional;

public class ConfirmDeleteFilesDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = ConfirmDeleteFilesDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILES = KEY_PREFIX + "FILES";

    @NonNull
    private LinkedHashSet<FileItem> mExtraFiles;

    @NonNull
    private static ConfirmDeleteFilesDialogFragment newInstance(
            @NonNull LinkedHashSet<FileItem> files) {
        //noinspection deprecation
        ConfirmDeleteFilesDialogFragment fragment = new ConfirmDeleteFilesDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelableArrayList(EXTRA_FILES, new ArrayList<>(files));
        return fragment;
    }

    public static void show(@NonNull LinkedHashSet<FileItem> files, @NonNull Fragment fragment) {
        ConfirmDeleteFilesDialogFragment.newInstance(files)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(LinkedHashSet)} instead.
     */
    public ConfirmDeleteFilesDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFiles = new LinkedHashSet<>(getArguments().getParcelableArrayList(EXTRA_FILES));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String message;
        if (mExtraFiles.size() == 1) {
            FileItem file = CollectionUtils.first(mExtraFiles);
            int messageRes = file.getAttributesNoFollowLinks().isDirectory() ?
                    R.string.file_delete_message_directory_format
                    : R.string.file_delete_message_file_format;
            message = getString(messageRes, FileUtils.getName(file));
        } else {
            boolean allDirectories = Functional.every(mExtraFiles, file ->
                    file.getAttributesNoFollowLinks().isDirectory());
            boolean allFiles = Functional.every(mExtraFiles, file ->
                    !file.getAttributesNoFollowLinks().isDirectory());
            int messageRes = allDirectories ?
                    R.string.file_delete_message_multiple_directories_format
                    : allFiles ? R.string.file_delete_message_multiple_files_format
                    : R.string.file_delete_message_multiple_mixed_format;
            message = getString(messageRes, mExtraFiles.size());
        }
        return new AlertDialog.Builder(requireContext(), getTheme())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> getListener()
                        .deleteFiles(mExtraFiles))
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @NonNull
    private Listener getListener() {
        return (Listener) requireParentFragment();
    }

    public interface Listener {
        void deleteFiles(@NonNull LinkedHashSet<FileItem> files);
    }
}
