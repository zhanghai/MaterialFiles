/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filelist.FileListActivity;
import me.zhanghai.android.files.filelist.FileUtils;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.IntentPathUtils;
import me.zhanghai.android.files.util.ViewUtils;

public class EditBookmarkDirectoryDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = EditBookmarkDirectoryDialogFragment.class.getName()
            + '.';

    private static final String EXTRA_BOOKMARK_DIRECTORY = KEY_PREFIX + "BOOKMARK_DIRECTORY";

    private static final String STATE_PATH = KEY_PREFIX + "PATH";

    private static final int REQUEST_CODE_PICK_DIRECTORY = 1;

    @NonNull
    private BookmarkDirectory mExtraBookmarkDirectory;

    private Path mPath;

    @BindView(R.id.name)
    EditText mNameEdit;
    @BindView(R.id.path)
    Button mPathButton;

    @NonNull
    private static EditBookmarkDirectoryDialogFragment newInstance(
            @NonNull BookmarkDirectory bookmarkDirectory) {
        //noinspection deprecation
        EditBookmarkDirectoryDialogFragment fragment = new EditBookmarkDirectoryDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_BOOKMARK_DIRECTORY, bookmarkDirectory);
        return fragment;
    }

    public static void show(@NonNull BookmarkDirectory bookmarkDirectory,
                            @NonNull Fragment fragment) {
        EditBookmarkDirectoryDialogFragment.newInstance(bookmarkDirectory)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(BookmarkDirectory)} instead.
     */
    public EditBookmarkDirectoryDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraBookmarkDirectory = getArguments().getParcelable(EXTRA_BOOKMARK_DIRECTORY);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), getTheme());
        View contentView = ViewUtils.inflate(R.layout.edit_bookmark_directory_dialog,
                builder.getContext());
        ButterKnife.bind(this, contentView);
        if (savedInstanceState == null) {
            String name = mExtraBookmarkDirectory.getName();
            mNameEdit.setText(name);
            mNameEdit.setSelection(0, name.length());
            mPath = mExtraBookmarkDirectory.getPath();
        } else {
            mPath = savedInstanceState.getParcelable(STATE_PATH);
        }
        updatePathButton();
        mPathButton.setOnClickListener(view -> {
            Intent intent = FileListActivity.newPickDirectoryIntent(mPath, requireContext());
            startActivityForResult(intent, REQUEST_CODE_PICK_DIRECTORY);
        });
        AlertDialog dialog = builder
                .setTitle(R.string.navigation_edit_bookmark_directory_title)
                .setView(contentView)
                .setPositiveButton(android.R.string.ok, (dialog2, which) -> {
                    String name = mNameEdit.getText().toString();
                    if (name.isEmpty()) {
                        name = null;
                    }
                    BookmarkDirectory bookmarkDirectory = new BookmarkDirectory(
                            mExtraBookmarkDirectory, name, mPath);
                    getListener().replaceBookmarkDirectory(bookmarkDirectory);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.remove, ((dialog2, which) ->
                        getListener().removeBookmarkDirectory(mExtraBookmarkDirectory)))
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_PATH, (Parcelable) mPath);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK_DIRECTORY:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    mPath = IntentPathUtils.getExtraPath(data);
                    updatePathButton();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updatePathButton() {
        mPathButton.setText(FileUtils.getPathString(mPath));
    }

    @NonNull
    private Listener getListener() {
        return (Listener) requireParentFragment();
    }

    public interface Listener {
        void replaceBookmarkDirectory(@NonNull BookmarkDirectory bookmarkDirectory);
        void removeBookmarkDirectory(@NonNull BookmarkDirectory bookmarkDirectory);
    }
}
