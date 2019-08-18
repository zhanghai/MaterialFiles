/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.ViewGroupCompat;
import me.zhanghai.android.files.filelist.FileUtils;
import me.zhanghai.android.files.navigation.BookmarkDirectory;
import me.zhanghai.android.files.ui.SimpleAdapter;
import me.zhanghai.android.files.util.ViewUtils;

public class BookmarkDirectoryAdapter
        extends SimpleAdapter<BookmarkDirectory, BookmarkDirectoryAdapter.ViewHolder>
        implements DraggableItemAdapter<BookmarkDirectoryAdapter.ViewHolder> {

    @NonNull
    private Listener mListener;

    public BookmarkDirectoryAdapter(@NonNull Listener listener) {
        mListener = listener;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ViewUtils.inflate(R.layout.bookmark_directory_item, parent));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookmarkDirectory bookmarkDirectory = getItem(position);
        // Need to remove the ripple before it's drawn onto the bitmap for dragging.
        //noinspection RedundantCast
        ((FrameLayout) holder.itemView).getForeground().mutate().setVisible(
                !holder.getDragState().isActive(), false);
        holder.itemView.setOnClickListener(view -> mListener.editBookmarkDirectory(
                bookmarkDirectory));
        holder.nameText.setText(bookmarkDirectory.getName());
        holder.pathText.setText(FileUtils.getPathString(bookmarkDirectory.getPath()));
    }

    @Override
    public boolean onCheckCanStartDrag(@NonNull ViewHolder holder, int position, int x, int y) {
        return ViewGroupCompat.isTransformedTouchPointInView((ViewGroup) holder.itemView, x, y,
                holder.dragHandleView, null);
    }

    @Nullable
    @Override
    public ItemDraggableRange onGetItemDraggableRange(@NonNull ViewHolder holder, int position) {
        return null;
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    @Override
    public void onItemDragStarted(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        notifyDataSetChanged();
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        mListener.moveBookmarkDirectory(fromPosition, toPosition);
    }

    public interface Listener {
        void editBookmarkDirectory(@NonNull BookmarkDirectory bookmarkDirectory);
        void moveBookmarkDirectory(int fromPosition, int toPosition);
    }

    static class ViewHolder extends AbstractDraggableItemViewHolder {

        @BindView(R.id.name)
        TextView nameText;
        @BindView(R.id.path)
        TextView pathText;
        @BindView(R.id.drag_handle)
        View dragHandleView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
