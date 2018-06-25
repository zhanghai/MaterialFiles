/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.support.v4.app.Fragment;

import me.zhanghai.android.materialfilemanager.R;

public class CreateFileDialogFragment extends FileNameDialogFragment {

    public static CreateFileDialogFragment newInstance() {
        //noinspection deprecation
        return new CreateFileDialogFragment();
    }

    public static void show(Fragment fragment) {
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
    protected void onOk(String name) {
        getListener().createFile(name);
    }

    @Override
    protected Listener getListener() {
        return (Listener) getParentFragment();
    }

    public interface Listener extends FileNameDialogFragment.Listener {
        void createFile(String name);
    }
}
