/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat;
import me.zhanghai.android.files.util.FragmentUtils;

public class OpenApkDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = OpenApkDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    @NonNull
    private FileItem mExtraFile;

    @NonNull
    private static OpenApkDialogFragment newInstance(@NonNull FileItem file) {
        //noinspection deprecation
        OpenApkDialogFragment fragment = new OpenApkDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    public static void show(@NonNull FileItem file, @NonNull Fragment fragment) {
        OpenApkDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(FileItem)} instead.
     */
    public OpenApkDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return AlertDialogBuilderCompat.create(requireContext(), getTheme())
                .setMessage(R.string.file_open_apk_message)
                .setPositiveButton(R.string.install, (dialog, which) -> getListener().installApk(
                        mExtraFile))
                // While semantically incorrect, this places the two most expected actions side by
                // side.
                .setNegativeButton(R.string.view, (dialog, which) -> getListener().viewApk(
                        mExtraFile))
                .setNeutralButton(android.R.string.cancel, null)
                .create();
    }

    @NonNull
    private Listener getListener() {
        return (Listener) requireParentFragment();
    }

    public interface Listener {
        void installApk(@NonNull FileItem file);
        void viewApk(@NonNull FileItem file);
    }
}
