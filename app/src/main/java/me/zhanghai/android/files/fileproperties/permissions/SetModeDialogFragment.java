/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat;
import me.zhanghai.android.files.compat.ListFormatterCompat;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.filejob.FileJobService;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.ui.DropDownView;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.ViewUtils;

public class SetModeDialogFragment extends AppCompatDialogFragment {

    private static final PosixFileModeBit[] OWNER_MODE_BITS = {
            PosixFileModeBit.OWNER_READ,
            PosixFileModeBit.OWNER_WRITE,
            PosixFileModeBit.OWNER_EXECUTE
    };
    private static final PosixFileModeBit[] GROUP_MODE_BITS = {
            PosixFileModeBit.GROUP_READ,
            PosixFileModeBit.GROUP_WRITE,
            PosixFileModeBit.GROUP_EXECUTE
    };
    private static final PosixFileModeBit[] OTHERS_MODE_BITS = {
            PosixFileModeBit.OTHERS_READ,
            PosixFileModeBit.OTHERS_WRITE,
            PosixFileModeBit.OTHERS_EXECUTE
    };
    private static final PosixFileModeBit[] SPECIAL_MODE_BITS = {
            PosixFileModeBit.SET_USER_ID,
            PosixFileModeBit.SET_GROUP_ID,
            PosixFileModeBit.STICKY
    };

    private static final String KEY_PREFIX = SetModeDialogFragment.class.getName() + '.';

    private static final String EXTRA_FILE = KEY_PREFIX + "FILE";

    @NonNull
    private FileItem mExtraFile;

    @BindView(R.id.owner)
    TextView mOwnerText;
    @BindView(R.id.owner_dropdown)
    DropDownView mOwnerDropDown;
    @BindView(R.id.group)
    TextView mGroupText;
    @BindView(R.id.group_dropdown)
    DropDownView mGroupDropDown;
    @BindView(R.id.others)
    TextView mOthersText;
    @BindView(R.id.others_dropdown)
    DropDownView mOthersDropDown;
    @BindView(R.id.special)
    TextView mSpecialText;
    @BindView(R.id.special_dropdown)
    DropDownView mSpecialDropDown;
    @BindView(R.id.recursive)
    CheckBox mRecursiveCheck;

    @NonNull
    private SetModeViewModel mViewModel;

    @NonNull
    private String[] mNormalModeBitNames;
    @NonNull
    private ModeBitListAdapter mOwnerAdapter;
    @NonNull
    private ModeBitListAdapter mGroupAdapter;
    @NonNull
    private ModeBitListAdapter mOthersAdapter;
    @NonNull
    private String[] mSpecialModeBitNames;
    @NonNull
    private ModeBitListAdapter mSpecialAdapter;

    @NonNull
    public static SetModeDialogFragment newInstance(@NonNull FileItem file) {
        //noinspection deprecation
        SetModeDialogFragment fragment = new SetModeDialogFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_FILE, file);
        return fragment;
    }

    public static void show(@NonNull FileItem file, @NonNull Fragment fragment) {
        SetModeDialogFragment.newInstance(file)
                .show(fragment.getChildFragmentManager(), null);
    }

    /**
     * @deprecated Use {@link #newInstance(FileItem)} instead.
     */
    public SetModeDialogFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = getArguments().getParcelable(EXTRA_FILE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = AlertDialogBuilderCompat.create(requireContext(), getTheme())
                .setTitle(R.string.file_properties_permissions_set_mode_title);
        Context context = builder.getContext();
        View contentView = ViewUtils.inflate(Settings.MATERIAL_DESIGN_2.getValue() ?
                R.layout.set_mode_dialog_md2 : R.layout.set_mode_dialog, context);
        ButterKnife.bind(this, contentView);

        mViewModel = new ViewModelProvider(this, new SetModeViewModel.Factory(getExtraMode()))
                .get(SetModeViewModel.class);

        mOwnerText.setOnClickListener(view -> mOwnerDropDown.show());
        Resources resources = getResources();
        boolean isDirectory = mExtraFile.getAttributes().isDirectory();
        mNormalModeBitNames = resources.getStringArray(isDirectory ?
                R.array.file_properties_permissions_set_mode_normal_mode_bits_directory
                : R.array.file_properties_permissions_set_mode_normal_mode_bits_file);
        mOwnerAdapter = new ModeBitListAdapter(OWNER_MODE_BITS, mNormalModeBitNames);
        mOwnerDropDown.setAdapter(mOwnerAdapter);
        mOwnerDropDown.setOnItemClickListener((parent, view, position, id) ->
                mViewModel.toggleModeBit(mOwnerAdapter.getItem(position)));
        mGroupText.setOnClickListener(view -> mGroupDropDown.show());
        mGroupAdapter = new ModeBitListAdapter(GROUP_MODE_BITS, mNormalModeBitNames);
        mGroupDropDown.setAdapter(mGroupAdapter);
        mGroupDropDown.setOnItemClickListener((parent, view, position, id) ->
                mViewModel.toggleModeBit(mGroupAdapter.getItem(position)));
        mOthersText.setOnClickListener(view -> mOthersDropDown.show());
        mOthersAdapter = new ModeBitListAdapter(OTHERS_MODE_BITS, mNormalModeBitNames);
        mOthersDropDown.setAdapter(mOthersAdapter);
        mOthersDropDown.setOnItemClickListener((parent, view, position, id) ->
                mViewModel.toggleModeBit(mOthersAdapter.getItem(position)));
        mSpecialText.setOnClickListener(view -> mSpecialDropDown.show());
        mSpecialModeBitNames = resources.getStringArray(
                R.array.file_properties_permissions_set_mode_special_mode_bits);
        mSpecialAdapter = new ModeBitListAdapter(SPECIAL_MODE_BITS, mSpecialModeBitNames);
        mSpecialDropDown.setAdapter(mSpecialAdapter);
        mSpecialDropDown.setOnItemClickListener((parent, view, position, id) ->
                mViewModel.toggleModeBit(mSpecialAdapter.getItem(position)));
        ViewUtils.setVisibleOrGone(mRecursiveCheck, isDirectory);

        mViewModel.getModeLiveData().observe(this, this::onModeChanged);

        return builder
                .setView(contentView)
                .setPositiveButton(android.R.string.ok, (dialog2, which) -> setMode())
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void onModeChanged(@NonNull Set<PosixFileModeBit> mode) {
        mOwnerText.setText(getModeBitsString(OWNER_MODE_BITS, mNormalModeBitNames));
        mOwnerAdapter.setMode(mode);
        mGroupText.setText(getModeBitsString(GROUP_MODE_BITS, mNormalModeBitNames));
        mGroupAdapter.setMode(mode);
        mOthersText.setText(getModeBitsString(OTHERS_MODE_BITS, mNormalModeBitNames));
        mOthersAdapter.setMode(mode);
        mSpecialText.setText(getModeBitsString(SPECIAL_MODE_BITS, mSpecialModeBitNames));
        mSpecialAdapter.setMode(mode);
    }

    @NonNull
    private String getModeBitsString(@NonNull PosixFileModeBit[] modeBits,
                                     @NonNull String[] modeBitNames) {
        Set<PosixFileModeBit> mode = mViewModel.getMode();
        List<String> checkedNames = new ArrayList<>();
        for (int i = 0; i < modeBits.length; ++i) {
            if (mode.contains(modeBits[i])) {
                checkedNames.add(modeBitNames[i]);
            }
        }
        return checkedNames.isEmpty() ? getString(R.string.none)
                : ListFormatterCompat.format(checkedNames);
    }

    private void setMode() {
        Set<PosixFileModeBit> mode = mViewModel.getMode();
        boolean recursive = mRecursiveCheck.isChecked();
        if (!recursive) {
            if (Objects.equals(mode, getExtraMode())) {
                return;
            }
        }
        FileJobService.setMode(mExtraFile.getPath(), mode, recursive, requireContext());
    }

    @NonNull
    private Set<PosixFileModeBit> getExtraMode() {
        PosixFileAttributes attributes = (PosixFileAttributes) mExtraFile.getAttributes();
        return attributes.mode();
    }
}
