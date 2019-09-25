/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat;
import me.zhanghai.android.files.navigation.file.DocumentTree;
import me.zhanghai.android.files.util.BundleUtils;
import me.zhanghai.android.files.util.FragmentUtils;

public class ConfirmRemoveDocumentTreeDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = ConfirmRemoveDocumentTreeDialogFragment.class.getName()
            + '.';

    private static final String EXTRA_TREE_URI = KEY_PREFIX + "TREE_URI";

    @NonNull
    private Uri mExtraTreeUri;

    @NonNull
    private static ConfirmRemoveDocumentTreeDialogFragment newInstance(@NonNull Uri treeUri) {
        //noinspection deprecation
        ConfirmRemoveDocumentTreeDialogFragment fragment = new ConfirmRemoveDocumentTreeDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_TREE_URI, treeUri);
        return fragment;
    }

    public static void show(@NonNull Uri treeUri, @NonNull Fragment fragment) {
        ConfirmRemoveDocumentTreeDialogFragment.newInstance(treeUri)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(Uri)} instead.
     */
    public ConfirmRemoveDocumentTreeDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraTreeUri = BundleUtils.getParcelable(getArguments(), EXTRA_TREE_URI);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        String displayName = DocumentTree.getDisplayName(mExtraTreeUri, context);
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
