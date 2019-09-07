/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Pair;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat;
import me.zhanghai.android.files.file.FileProvider;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.util.AppUtils;
import me.zhanghai.android.files.util.IntentPathUtils;
import me.zhanghai.android.files.util.IntentUtils;
import me.zhanghai.android.files.util.ListBuilder;
import me.zhanghai.java.functional.Functional;

public class OpenFileAsDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = OpenFileAsDialogFragment.class.getName() + '.';

    private static final String EXTRA_PATH = KEY_PREFIX + "PATH";

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
    private Path mExtraPath;

    public static void putArguments(@NonNull Intent intent, @NonNull Path path) {
        intent.putExtra(EXTRA_PATH, (Parcelable) path);
    }

    @NonNull
    public static OpenFileAsDialogFragment newInstance(@NonNull Intent intent) {
        //noinspection deprecation
        OpenFileAsDialogFragment fragment = new OpenFileAsDialogFragment();
        fragment.setArguments(intent.getExtras());
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(Intent)} instead.
     */
    public OpenFileAsDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraPath = getArguments().getParcelable(EXTRA_PATH);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        CharSequence[] items = Functional.map(FILE_TYPES, item -> getString(item.first))
                .toArray(new CharSequence[0]);
        return AlertDialogBuilderCompat.create(requireContext(), getTheme())
                .setTitle(getString(R.string.file_open_as_title_format,
                        mExtraPath.getFileName().toString()))
                .setItems(items, (dialog, which) -> openAs(FILE_TYPES.get(which).second))
                .create();
    }

    private void openAs(@NonNull String mimeType) {
        Uri uri = FileProvider.getUriForPath(mExtraPath);
        Intent intent = IntentUtils.makeView(uri, mimeType)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        IntentPathUtils.putExtraPath(intent, mExtraPath);
        Activity activity = requireActivity();
        AppUtils.startActivityWithChooser(intent, activity);
        activity.finish();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        requireActivity().finish();
    }
}
