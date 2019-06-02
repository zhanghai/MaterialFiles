/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.ViewUtils;

public class TextEditorFragment extends Fragment {

    private static final String KEY_PREFIX = TextEditorFragment.class.getName() + '.';

    private static final String EXTRA_PATH = KEY_PREFIX + "PATH";

    private Path mExtraPath;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.progress)
    ProgressBar mProgress;
    @BindView(R.id.error)
    TextView mErrorView;
    @BindView(R.id.text)
    EditText mTextEdit;

    private TextEditorViewModel mViewModel;

    @NonNull
    public static TextEditorFragment newInstance(@NonNull Path path) {
        //noinspection deprecation
        TextEditorFragment fragment = new TextEditorFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(EXTRA_PATH, (Parcelable) path);
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(Path)} instead.
     */
    public TextEditorFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtraPath = getArguments().getParcelable(EXTRA_PATH);

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

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(mToolbar);

        mViewModel = ViewModelProviders.of(this).get(TextEditorViewModel.class);
        // TODO: Move reload-prevent here so that we can also handle save-as, etc. Or maybe just get
        //  rid of the mPathLiveData in TextEditorViewModel.
        mViewModel.setPath(mExtraPath);
        mViewModel.getFileContentLiveData().observe(this, this::onFileContentChanged);

        // TODO: Request storage permission if not granted.
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                AppCompatActivity activity = (AppCompatActivity) requireActivity();
                activity.finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onBackPressed() {
        // TODO
        return false;
    }

    private void onFileContentChanged(@NonNull FileContentData fileContentData) {
        requireActivity().setTitle(fileContentData.path.getFileName().toString());
        switch (fileContentData.state) {
            case LOADING: {
                ViewUtils.fadeIn(mProgress);
                ViewUtils.fadeOut(mErrorView);
                ViewUtils.fadeOut(mTextEdit);
                //mTextEdit.setText(null);
                break;
            }
            case ERROR:
                fileContentData.exception.printStackTrace();
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeIn(mErrorView);
                mErrorView.setText(fileContentData.exception.toString());
                ViewUtils.fadeOut(mTextEdit);
                //mTextEdit.setText(null);
                break;
            case SUCCESS: {
                ViewUtils.fadeOut(mProgress);
                ViewUtils.fadeOut(mErrorView);
                ViewUtils.fadeIn(mTextEdit);
                mTextEdit.setText(new String(fileContentData.content));
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
    }
}
