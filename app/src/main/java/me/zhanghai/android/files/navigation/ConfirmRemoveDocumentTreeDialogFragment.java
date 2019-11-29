/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageVolume;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat;
import me.zhanghai.android.files.compat.StorageVolumeCompat;
import me.zhanghai.android.files.navigation.file.DocumentTree;
import me.zhanghai.android.files.util.BundleUtils;
import me.zhanghai.android.files.util.FragmentUtils;

public class ConfirmRemoveDocumentTreeDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = ConfirmRemoveDocumentTreeDialogFragment.class.getName()
            + '.';

    private static final String EXTRA_TREE_URI = KEY_PREFIX + "TREE_URI";
    private static final String EXTRA_STORAGE_VOLUME = KEY_PREFIX + "STORAGE_VOLUME";

    @NonNull
    private Uri mExtraTreeUri;
    @Nullable
    private StorageVolume mExtraStorageVolume;

    @NonNull
    // For casting StorageVolume to Parcelable which actually works.
    @SuppressLint("NewApi")
    private static ConfirmRemoveDocumentTreeDialogFragment newInstance(
            @NonNull Uri treeUri, @Nullable StorageVolume storageVolume) {
        //noinspection deprecation
        ConfirmRemoveDocumentTreeDialogFragment fragment =
                new ConfirmRemoveDocumentTreeDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_TREE_URI, treeUri)
                .putParcelable(EXTRA_STORAGE_VOLUME, storageVolume);
        return fragment;
    }

    public static void show(@NonNull Uri treeUri, @Nullable StorageVolume storageVolume,
                            @NonNull Fragment fragment) {
        ConfirmRemoveDocumentTreeDialogFragment.newInstance(treeUri, storageVolume)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(Uri, StorageVolume)} instead.
     */
    public ConfirmRemoveDocumentTreeDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        mExtraTreeUri = BundleUtils.getParcelable(arguments, EXTRA_TREE_URI);
        mExtraStorageVolume = BundleUtils.getParcelable(arguments, EXTRA_STORAGE_VOLUME);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        String displayName = mExtraStorageVolume != null ?
                StorageVolumeCompat.getDescription(mExtraStorageVolume, context)
                : DocumentTree.getDisplayName(mExtraTreeUri, context);
        return AlertDialogBuilderCompat.create(context, getTheme())
                .setMessage(getString(R.string.navigation_confirm_remove_document_tree_format,
                        displayName))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> getListener()
                        .removeDocumentTree(mExtraTreeUri))
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @NonNull
    private Listener getListener() {
        return (Listener) requireParentFragment();
    }

    public interface Listener {
        void removeDocumentTree(@NonNull Uri treeUri);
    }
}
