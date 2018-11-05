/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.app.Dialog;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;

public class ConfirmDeleteFilesDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = ConfirmDeleteFilesDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILES = KEY_PREFIX + "FILES";

    @NonNull
    private Set<File> mExtraFiles;

    @NonNull
    private static ConfirmDeleteFilesDialogFragment newInstance(@NonNull Set<File> files) {
        //noinspection deprecation
        ConfirmDeleteFilesDialogFragment fragment = new ConfirmDeleteFilesDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelableArrayList(EXTRA_FILES, new ArrayList<>(files));
        return fragment;
    }

    public static void show(@NonNull Set<File> files, @NonNull Fragment fragment) {
        ConfirmDeleteFilesDialogFragment.newInstance(files)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(Set)} instead.
     */
    public ConfirmDeleteFilesDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFiles = new HashSet<>(getArguments().getParcelableArrayList(EXTRA_FILES));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String message;
        if (mExtraFiles.size() == 1) {
            File file = CollectionUtils.first(mExtraFiles);
            int messageRes = file.isDirectoryDoNotFollowSymbolicLinks() ?
                    R.string.file_delete_message_directory_format
                    : R.string.file_delete_message_file_format;
            message = getString(messageRes, file.getName());
        } else {
            boolean allDirectories = Functional.every(mExtraFiles,
                    File::isDirectoryDoNotFollowSymbolicLinks);
            boolean allFiles = Functional.every(mExtraFiles, file ->
                    !file.isDirectoryDoNotFollowSymbolicLinks());
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
        return (Listener) getParentFragment();
    }

    public interface Listener {
        void deleteFiles(@NonNull Set<File> files);
    }
}
