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
import android.util.Pair;

import java.util.List;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.file.MimeTypes;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.FragmentUtils;
import me.zhanghai.android.materialfilemanager.util.ListBuilder;

public class OpenFileAsDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = OpenFileAsDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    private static final List<Pair<Integer, String>> FILE_TYPES =
            ListBuilder.<Pair<Integer, String>>newArrayList()
                    .add(new Pair<>(R.string.file_open_as_type_text, "text/plain"))
                    .add(new Pair<>(R.string.file_open_as_type_image, "image/*"))
                    .add(new Pair<>(R.string.file_open_as_type_audio, "audio/*"))
                    .add(new Pair<>(R.string.file_open_as_type_video, "video/*"))
                    .add(new Pair<>(R.string.file_open_as_type_directory,
                            MimeTypes.DIRECTORY_MIME_TYPE))
                    .add(new Pair<>(R.string.file_open_as_type_any, "*/*"))
                    .buildUnmodifiable();

    @NonNull
    private File mExtraFile;

    @NonNull
    public static OpenFileAsDialogFragment newInstance(@NonNull File file) {
        //noinspection deprecation
        OpenFileAsDialogFragment fragment = new OpenFileAsDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    public static void show(@NonNull File file, @NonNull Fragment fragment) {
        OpenFileAsDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(File)} instead.
     */
    public OpenFileAsDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        CharSequence[] items = Functional.map(FILE_TYPES, item -> getString(item.first))
                .toArray(new CharSequence[0]);
        return new AlertDialog.Builder(requireContext(), getTheme())
                .setTitle(getString(R.string.file_open_as_title_format, mExtraFile.getName()))
                .setItems(items, (dialog, which) -> {
                    getListener().openFileAs(mExtraFile, FILE_TYPES.get(which).second);
                })
                .create();
    }

    @NonNull
    private Listener getListener() {
        return (Listener) getParentFragment();
    }

    public interface Listener {
        void openFileAs(@NonNull File file, @NonNull String mimeType);
    }
}
