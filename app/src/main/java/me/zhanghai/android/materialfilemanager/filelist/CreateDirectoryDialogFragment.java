/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.support.v4.app.Fragment;

import me.zhanghai.android.materialfilemanager.R;

public class CreateDirectoryDialogFragment extends FileNameDialogFragment {

    public static CreateDirectoryDialogFragment newInstance() {
        //noinspection deprecation
        return new CreateDirectoryDialogFragment();
    }

    public static void show(Fragment fragment) {
        CreateDirectoryDialogFragment.newInstance()
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public CreateDirectoryDialogFragment() {}

    @Override
    protected int getTitleRes() {
        return R.string.file_create_directory_title;
    }

    @Override
    protected void onOk(String name) {
        getListener().createDirectory(name);
    }

    @Override
    protected Listener getListener() {
        return (Listener) getParentFragment();
    }

    public interface Listener extends FileNameDialogFragment.Listener {
        void createDirectory(String name);
    }
}
