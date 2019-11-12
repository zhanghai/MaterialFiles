/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.text;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.ui.FastScrollLiftOnScrollHack;
import me.zhanghai.android.files.ui.ThemedFastScroller;
import me.zhanghai.android.files.util.BundleUtils;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.IntentPathUtils;
import me.zhanghai.android.files.util.StateData;
import me.zhanghai.android.files.util.ToastUtils;
import me.zhanghai.android.files.util.ViewUtils;

public class TextEditorFragment extends Fragment implements ConfirmReloadDialogFragment.Listener,
        ConfirmCloseDialogFragment.Listener {

    private Intent mIntent;
    private Path mExtraPath;

    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.progress)
    ProgressBar mProgress;
    @BindView(R.id.error)
    TextView mErrorView;
    @BindView(R.id.scroll)
    NestedScrollView mScrollView;
    @BindView(R.id.text)
    EditText mTextEdit;

    @Nullable
    private MenuItem mSaveMenuItem;

    private TextEditorViewModel mViewModel;

    private boolean mSettingText;

    public static void putArguments(@NonNull Intent intent, @NonNull Path path) {
        IntentPathUtils.putExtraPath(intent, path);
    }

    @NonNull
    public static TextEditorFragment newInstance(@NonNull Intent intent) {
        //noinspection deprecation
        TextEditorFragment fragment = new TextEditorFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(Intent.EXTRA_INTENT, intent);
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(Intent)} instead.
     */
    public TextEditorFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntent = BundleUtils.getParcelable(getArguments(), Intent.EXTRA_INTENT);
        mExtraPath = IntentPathUtils.getExtraPath(mIntent, true);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.text_editor_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mExtraPath == null) {
            // TODO: Show a toast.
            finish();
            return;
        }

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(mToolbar);

        mViewModel = ViewModelProviders.of(this).get(TextEditorViewModel.class);
        // TODO: Move reload-prevent here so that we can also handle save-as, etc. Or maybe just get
        //  rid of the mPathLiveData in TextEditorViewModel.
        mViewModel.setPath(mExtraPath);

        ThemedFastScroller.create(mScrollView);
        FastScrollLiftOnScrollHack.hack(mAppBarLayout);
        // Manually save and restore state in view model to avoid TransactionTooLargeException.
        mTextEdit.setSaveEnabled(false);
        Parcelable textEditSavedState = mViewModel.removeEditTextSavedState();
        if (textEditSavedState != null) {
            mTextEdit.onRestoreInstanceState(textEditSavedState);
        }
        mTextEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(@NonNull CharSequence text, int start, int count,
                                          int after) {}
            @Override
            public void onTextChanged(@NonNull CharSequence text, int start, int before,
                                      int count) {}
            @Override
            public void afterTextChanged(@NonNull Editable text) {
                if (mSettingText) {
                    return;
                }
                mViewModel.setTextChanged(true);
            }
        });

        mViewModel.getFileContentLiveData().observe(this, this::onFileContentChanged);
        mViewModel.getTextChangedLiveData().observe(this, this::onTextChangedChanged);
        mViewModel.getWriteFileStateLiveData().observe(this, this::onWriteFileStateChanged);

        // TODO: Request storage permission if not granted.
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        mViewModel.setEditTextSavedState(mTextEdit.onSaveInstanceState());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.text_editor, menu);
        mSaveMenuItem = menu.findItem(R.id.action_save);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        updateSaveMenuItem();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (!onFinish()) {
                    finish();
                }
                return true;
            }
            case R.id.action_save:
                save();
                return true;
            case R.id.action_reload:
                onReload();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onBackPressed() {
        return onFinish();
    }

    private boolean onFinish() {
        if (mViewModel.isTextChanged()) {
            ConfirmCloseDialogFragment.show(this);
            return true;
        }
        return false;
    }

    @Override
    public void finish() {
        requireActivity().finish();
    }

    private void onFileContentChanged(@NonNull FileContentData fileContentData) {
        updateTitle();
        switch (fileContentData.state) {
            case LOADING: {
                ViewUtils.fadeIn(mProgress);
                ViewUtils.fadeOut(mErrorView);
                ViewUtils.fadeOut(mTextEdit);
                //setText(null);
                break;
            }
            case ERROR:
                fileContentData.exception.printStackTrace();
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeIn(mErrorView);
                mErrorView.setText(fileContentData.exception.toString());
                ViewUtils.fadeOut(mTextEdit);
                //setText(null);
                break;
            case SUCCESS:
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeOut(mErrorView);
                ViewUtils.fadeIn(mTextEdit);
                if (!mViewModel.isTextChanged()) {
                    // TODO: Charset.
                    setText(new String(fileContentData.data));
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void setText(@Nullable String text) {
        mSettingText = true;
        mTextEdit.setText(text);
        mSettingText = false;
        mViewModel.setTextChanged(false);
    }

    private void onTextChangedChanged(boolean changed) {
        updateTitle();
    }

    private void updateTitle() {
        String fileName = mViewModel.getFileContentData().path.getFileName().toString();
        boolean changed = mViewModel.isTextChanged();
        String title = getString(changed ? R.string.text_editor_title_changed_format
                : R.string.text_editor_title_format, fileName);
        requireActivity().setTitle(title);
    }

    private void onReload() {
        if (mViewModel.isTextChanged()) {
            ConfirmReloadDialogFragment.show(this);
        } else {
            reload();
        }
    }

    @Override
    public void reload() {
        mViewModel.setTextChanged(false);
        mViewModel.reload();
    }

    private void save() {
        // TODO: Charset
        byte[] content = mTextEdit.getText().toString().getBytes();
        mViewModel.getWriteFileStateLiveData().write(mExtraPath, content, requireContext());
    }

    private void onWriteFileStateChanged(@NonNull StateData stateData) {
        WriteFileStateLiveData liveData = mViewModel.getWriteFileStateLiveData();
        switch (stateData.state) {
            case READY:
            case LOADING:
                updateSaveMenuItem();
                break;
            case ERROR:
                liveData.reset();
                break;
            case SUCCESS:
                ToastUtils.show(R.string.text_editor_save_success, requireContext());
                liveData.reset();
                mViewModel.setTextChanged(false);
                break;
        }
    }

    private void updateSaveMenuItem() {
        if (mSaveMenuItem == null) {
            return;
        }
        WriteFileStateLiveData liveData = mViewModel.getWriteFileStateLiveData();
        mSaveMenuItem.setEnabled(liveData.getValue().state == StateData.State.READY);
    }
}
