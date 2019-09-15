/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.filejob.FileJobService;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.ViewUtils;

public class SetSeLinuxContextDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_PREFIX = SetSeLinuxContextDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    private FileItem mExtraFile;

    @BindView(R.id.selinux_context)
    EditText mSeLinuxContextEdit;
    @BindView(R.id.recursive)
    CheckBox mRecursiveCheck;

    @NonNull
    public static SetSeLinuxContextDialogFragment newInstance(@NonNull FileItem file) {
        //noinspection deprecation
        SetSeLinuxContextDialogFragment fragment = new SetSeLinuxContextDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    public static void show(@NonNull FileItem file, @NonNull Fragment fragment) {
        SetSeLinuxContextDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(FileItem)} instead.
     */
    public SetSeLinuxContextDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = AlertDialogBuilderCompat.create(requireContext(), getTheme())
                .setTitle(R.string.file_properties_permissions_set_selinux_context_title);
        Context context = builder.getContext();
        View contentView = ViewUtils.inflate(Settings.MATERIAL_DESIGN_2.getValue() ?
                R.layout.set_selinux_context_dialog_md2 : R.layout.set_selinux_context_dialog,
                context);
        ButterKnife.bind(this, contentView);
        if (savedInstanceState == null) {
            mSeLinuxContextEdit.setText(getExtraSeLinuxContext().toString());
        }
        AlertDialog dialog = builder
                .setView(contentView)
                .setPositiveButton(android.R.string.ok, (dialog2, which) -> setSeLinuxContext())
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.file_properties_permissions_set_selinux_context_restore,
                        ((dialog2, which) -> restoreSeLinuxContext()))
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    private void setSeLinuxContext() {
        ByteString seLinuxContext = ByteString.fromString(mSeLinuxContextEdit.getText().toString());
        boolean recursive = mRecursiveCheck.isChecked();
        if (!recursive) {
            if (Objects.equals(seLinuxContext, getExtraSeLinuxContext())) {
                return;
            }
        }
        FileJobService.setSeLinuxContext(mExtraFile.getPath(), seLinuxContext, recursive,
                requireContext());
    }

    @NonNull
    private ByteString getExtraSeLinuxContext() {
        PosixFileAttributes attributes = (PosixFileAttributes) mExtraFile.getAttributes();
        return attributes.seLinuxContext();
    }

    private void restoreSeLinuxContext() {
        boolean recursive = mRecursiveCheck.isChecked();
        FileJobService.restoreSeLinuxContext(mExtraFile.getPath(), recursive, requireContext());
    }
}
