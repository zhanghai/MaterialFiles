/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.viewer.image;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.nio.file.Path;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FileProvider;
import me.zhanghai.android.files.ui.ViewPagerTransformers;
import me.zhanghai.android.files.util.AppUtils;
import me.zhanghai.android.files.util.FragmentUtils;
import me.zhanghai.android.files.util.IntentPathUtils;
import me.zhanghai.android.files.util.IntentUtils;
import me.zhanghai.android.systemuihelper.SystemUiHelper;

public class ImageViewerFragment extends Fragment {

    private static final String KEY_PREFIX = ImageViewerFragment.class.getName() + '.';

    private static final String EXTRA_POSITION = KEY_PREFIX + "POSITION";

    private Intent mIntent;
    private List<Path> mExtraPaths;
    private int mExtraPosition;

    @BindInt(android.R.integer.config_mediumAnimTime)
    int mToolbarAnimationDuration;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    private SystemUiHelper mSystemUiHelper;
    private ImageViewerAdapter mAdapter;

    public static void putArguments(@NonNull Intent intent, @NonNull List<Path> paths,
                                    int position) {
        IntentPathUtils.putExtraPathList(intent, paths);
        intent.putExtra(EXTRA_POSITION, position);
    }

    @NonNull
    public static ImageViewerFragment newInstance(@NonNull Intent intent) {
        //noinspection deprecation
        ImageViewerFragment fragment = new ImageViewerFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putParcelable(Intent.EXTRA_INTENT, intent);
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(Intent)} instead.
     */
    public ImageViewerFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntent = getArguments().getParcelable(Intent.EXTRA_INTENT);
        mExtraPaths = IntentPathUtils.getExtraPathList(mIntent);
        mExtraPosition = mIntent.getIntExtra(EXTRA_POSITION, 0);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.image_viewer_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mExtraPaths.isEmpty()) {
            // TODO: Show a toast.
            finish();
            return;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);

        mSystemUiHelper = new SystemUiHelper(activity, SystemUiHelper.LEVEL_IMMERSIVE,
                SystemUiHelper.FLAG_IMMERSIVE_STICKY, visible -> mToolbar.animate()
                .alpha(visible ? 1 : 0)
                .translationY(visible ? 0 : -mToolbar.getBottom())
                .setDuration(mToolbarAnimationDuration)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start());
        // This will set up window flags.
        mSystemUiHelper.show();

        mAdapter = new ImageViewerAdapter(mExtraPaths, view -> mSystemUiHelper.toggle());
        mViewPager.setAdapter(mAdapter);
        // TODO: Rotation?
        mViewPager.setCurrentItem(mExtraPosition);
        mViewPager.setPageTransformer(true, ViewPagerTransformers.DEPTH);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateTitle();
            }
        });
        updateTitle();
    }

    private void updateTitle() {
        Path path = getCurrentPath();
        requireActivity().setTitle(path.getFileName().toString());
        int size = mExtraPaths.size();
        mToolbar.setSubtitle(size > 1 ? getString(R.string.image_viewer_subtitle_format,
                mViewPager.getCurrentItem() + 1, size) : null);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.image_viewer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                share();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void finish() {
        requireActivity().finish();
    }

    private void share() {
        Path path = getCurrentPath();
        Uri uri = FileProvider.getUriForPath(path);
        Intent intent = IntentUtils.makeSendImage(uri);
        IntentPathUtils.putExtraPath(intent, path);
        AppUtils.startActivityWithChooser(intent, requireContext());
    }

    @NonNull
    private Path getCurrentPath() {
        return mExtraPaths.get(mViewPager.getCurrentItem());
    }
}
