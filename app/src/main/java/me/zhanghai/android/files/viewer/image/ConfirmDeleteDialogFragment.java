/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.image;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.FragmentUtils;

public class ConfirmDeleteDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = ConfirmDeleteDialogFragment.class.getName() + '.';

    private static final String EXTRA_PATH = KEY_PREFIX + "PATH";

    @NonNull
    private Path mExtraPath;

    @NonNull
    private static ConfirmDeleteDialogFragment newInstance(@NonNull Path path) {
        //noinspection deprecation
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_PATH, (Parcelable) path);
        return fragment;
    }

    public static void show(@NonNull Path path, @NonNull Fragment fragment) {
        ConfirmDeleteDialogFragment.newInstance(path)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(Path)} instead.
     */
    public ConfirmDeleteDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraPath = getArguments().getParcelable(EXTRA_PATH);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext(), getTheme())
                .setMessage(getString(R.string.image_viewer_delete_message_format,
                        mExtraPath.getFileName()))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> getListener().delete(
                        mExtraPath))
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @NonNull
    private Listener getListener() {
        return (Listener) requireParentFragment();
    }

    public interface Listener {
        void delete(@NonNull Path path);
    }
}
