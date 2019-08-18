/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.navigation.BookmarkDirectories;
import me.zhanghai.android.files.navigation.BookmarkDirectory;
import me.zhanghai.android.files.navigation.EditBookmarkDirectoryDialogFragment;

public class BookmarkDirectoriesFragment extends Fragment
        implements BookmarkDirectoryAdapter.Listener, EditBookmarkDirectoryDialogFragment.Listener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    private BookmarkDirectoryAdapter mAdapter;
    private RecyclerViewDragDropManager mDragDropManager;
    private RecyclerView.Adapter<?> mWrappedAdapter;

    @NonNull
    public static BookmarkDirectoriesFragment newInstance() {
        //noinspection deprecation
        return new BookmarkDirectoriesFragment();
    }

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public BookmarkDirectoriesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmark_directories_fragment, container, false);
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

        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.VERTICAL,
                false));
        mAdapter = new BookmarkDirectoryAdapter(this);
        mDragDropManager = new RecyclerViewDragDropManager();
        mDragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable)
                AppCompatResources.getDrawable(activity, R.drawable.ms9_composite_shadow_z2));
        mWrappedAdapter = mDragDropManager.createWrappedAdapter(mAdapter);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setItemAnimator(new DraggableItemAnimator());
        mDragDropManager.attachRecyclerView(mRecyclerView);

        Settings.BOOKMARK_DIRECTORIES.observe(this, this::onBookmarkDirectoriesChanged);
    }

    @Override
    public void onPause() {
        super.onPause();

        mDragDropManager.cancelDrag();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mDragDropManager.release();
        WrapperAdapterUtils.releaseAll(mWrappedAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                // This recreates MainActivity but we cannot have singleTop as launch mode along
                // with document launch mode.
                //AppCompatActivity activity = (AppCompatActivity) requireActivity();
                //activity.onSupportNavigateUp();
                requireActivity().finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onBookmarkDirectoriesChanged(
            @NonNull List<BookmarkDirectory> bookmarkDirectories) {
        mAdapter.replace(bookmarkDirectories);
    }

    @Override
    public void editBookmarkDirectory(@NonNull BookmarkDirectory bookmarkDirectory) {
        EditBookmarkDirectoryDialogFragment.show(bookmarkDirectory, this);
    }

    @Override
    public void moveBookmarkDirectory(int fromPosition, int toPosition) {
        BookmarkDirectories.move(fromPosition, toPosition);
    }

    @Override
    public void replaceBookmarkDirectory(@NonNull BookmarkDirectory bookmarkDirectory) {
        BookmarkDirectories.replace(bookmarkDirectory);
    }

    @Override
    public void removeBookmarkDirectory(@NonNull BookmarkDirectory bookmarkDirectory) {
        BookmarkDirectories.remove(bookmarkDirectory);
    }

}
