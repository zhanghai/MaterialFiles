/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.files.R;

public class CreateDirectoryDialogFragment extends FileNameDialogFragment {

    @NonNull
    public static CreateDirectoryDialogFragment newInstance() {
        //noinspection deprecation
        return new CreateDirectoryDialogFragment();
    }

    public static void show(@NonNull Fragment fragment) {
        CreateDirectoryDialogFragment.newInstance()
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public CreateDirectoryDialogFragment() {}

    @Override
    @StringRes
    protected int getTitleRes() {
        return R.string.file_create_directory_title;
    }

    @Override
    protected void onOk(@NonNull String name) {
        getListener().createDirectory(name);
    }

    @NonNull
    @Override
    protected Listener getListener() {
        return (Listener) getParentFragment();
    }

    public interface Listener extends FileNameDialogFragment.Listener {
        void createDirectory(@NonNull String name);
    }
}
