/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.AlertDialogBuilderCompat;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixPrincipal;
import me.zhanghai.android.files.util.BundleUtils;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.SelectionLiveData;
import me.zhanghai.android.files.util.ViewUtils;
import me.zhanghai.java.functional.Functional;

abstract class SetPrincipalDialogFragment extends AppCompatDialogFragment {

    private final String KEY_PREFIX = getClass().getName() + '.';

    private final String EXTRA_FILE = KEY_PREFIX + "FILE";

    private FileItem mExtraFile;

    @BindView(R.id.filter)
    EditText mFilterEdit;
    @BindView(R.id.progress)
    ProgressBar mProgress;
    @BindView(R.id.error)
    TextView mErrorText;
    @BindView(R.id.empty)
    View mEmptyView;
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.recursive)
    CheckBox mRecursiveCheck;

    private SetPrincipalViewModel mViewModel;

    private PrincipalListAdapter mAdapter;

    private Integer mPendingScrollToId;

    protected void putArguments(@NonNull FileItem file) {
        FragmentUtils.getArgumentsBuilder(this)
                .putParcelable(EXTRA_FILE, file);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraFile = BundleUtils.getParcelable(getArguments(), EXTRA_FILE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = AlertDialogBuilderCompat.create(requireContext(), getTheme())
                .setTitle(getTitleRes());
        Context context = builder.getContext();
        View contentView = ViewUtils.inflate(R.layout.set_principal_dialog, context);
        ButterKnife.bind(this, contentView);

        mViewModel = new ViewModelProvider(this).get(getViewModelClass());
        SelectionLiveData<Integer> selectionLiveData = mViewModel.getSelectionLiveData();
        if (selectionLiveData.getValue() == null) {
            int id = getExtraPrincipalId();
            selectionLiveData.setValue(id);
            mPendingScrollToId = id;
        }

        mFilterEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(@NonNull CharSequence text, int start, int count,
                                          int after) {}
            @Override
            public void onTextChanged(@NonNull CharSequence text, int start, int before,
                                      int count) {}
            @Override
            public void afterTextChanged(@NonNull Editable text) {
                mViewModel.setFilter(text.toString());
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = createAdapter(selectionLiveData);
        mRecyclerView.setAdapter(mAdapter);
        ViewUtils.setVisibleOrGone(mRecursiveCheck, mExtraFile.getAttributes().isDirectory());

        LifecycleOwner viewLifecycleOwner = getViewLifecycleOwner();
        mViewModel.getFilteredPrincipalListLiveData().observe(viewLifecycleOwner,
                this::onFilteredPrincipalListChanged);
        selectionLiveData.observe(viewLifecycleOwner, mAdapter);

        return builder
                .setView(contentView)
                .setPositiveButton(android.R.string.ok, (dialog2, which) -> setPrincipal())
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @StringRes
    protected abstract int getTitleRes();

    @NonNull
    protected abstract Class<? extends SetPrincipalViewModel> getViewModelClass();

    @NonNull
    protected abstract PrincipalListAdapter createAdapter(
            @NonNull SelectionLiveData<Integer> selectionLiveData);

    private void onFilteredPrincipalListChanged(@NonNull PrincipalListData principalListData) {
        switch (principalListData.state) {
            case LOADING:
                ViewUtils.fadeIn(mProgress);
                ViewUtils.fadeOut(mErrorText);
                ViewUtils.fadeOut(mEmptyView);
                mAdapter.clear();
                break;
            case ERROR:
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeIn(mErrorText);
                mErrorText.setText(principalListData.exception.toString());
                ViewUtils.fadeOut(mEmptyView);
                mAdapter.clear();
                break;
            case SUCCESS:
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeOut(mErrorText);
                ViewUtils.fadeToVisibility(mEmptyView, principalListData.data.isEmpty());
                mAdapter.replace(principalListData.data);
                if (mPendingScrollToId != null) {
                    int position = mAdapter.findPositionByPrincipalId(mPendingScrollToId);
                    if (position != RecyclerView.NO_POSITION) {
                        mRecyclerView.scrollToPosition(position);
                    }
                    mPendingScrollToId = null;
                }
                break;
            default:
                throw new AssertionError();
        }
    }

    private void setPrincipal() {
        int id = mViewModel.getSelectionLiveData().getValue();
        boolean recursive = mRecursiveCheck.isChecked();
        if (!recursive) {
            if (id == getExtraPrincipalId()) {
                return;
            }
        }
        PrincipalListData principalListData = mViewModel.getPrincipalListData();
        if (principalListData.data == null) {
            return;
        }
        PrincipalItem principal = Functional.find(principalListData.data, principal_ ->
                principal_.id == id);
        if (principal == null) {
            return;
        }
        setPrincipal(mExtraFile.getPath(), principal, recursive);
    }

    private int getExtraPrincipalId() {
        PosixFileAttributes attributes = (PosixFileAttributes) mExtraFile.getAttributes();
        return getExtraPrincipal(attributes).getId();
    }

    @NonNull
    protected abstract PosixPrincipal getExtraPrincipal(@NonNull PosixFileAttributes attributes);

    protected abstract void setPrincipal(@NonNull Path path, @NonNull PrincipalItem principal,
                                         boolean recursive);
}
