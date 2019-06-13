/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.files.R;

public class ConfirmReloadDialogFragment extends AppCompatDialogFragment {

    @NonNull
    private static ConfirmReloadDialogFragment newInstance() {
        //noinspection deprecation
        return new ConfirmReloadDialogFragment();
    }

    public static void show(@NonNull Fragment fragment) {
        ConfirmReloadDialogFragment.newInstance()
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public ConfirmReloadDialogFragment() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext(), getTheme())
                .setMessage(R.string.text_editor_reload_message)
                .setPositiveButton(R.string.keep_editing, null)
                .setNegativeButton(R.string.reload, (dialog, which) -> getListener().reload())
                .create();
    }

    @NonNull
    private Listener getListener() {
        return (Listener) requireParentFragment();
    }

    public interface Listener {
        void reload();
    }
}
