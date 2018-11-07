/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.files.R;

public class CreateFileDialogFragment extends FileNameDialogFragment {

    @NonNull
    public static CreateFileDialogFragment newInstance() {
        //noinspection deprecation
        return new CreateFileDialogFragment();
    }

    public static void show(@NonNull Fragment fragment) {
        CreateFileDialogFragment.newInstance()
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public CreateFileDialogFragment() {}

    @Override
    protected int getTitleRes() {
        return R.string.file_create_file_title;
    }

    @Override
    protected void onOk(@NonNull String name) {
        getListener().createFile(name);
    }

    @NonNull
    @Override
    protected Listener getListener() {
        return (Listener) getParentFragment();
    }

    public interface Listener extends FileNameDialogFragment.Listener {
        void createFile(@NonNull String name);
    }
}
