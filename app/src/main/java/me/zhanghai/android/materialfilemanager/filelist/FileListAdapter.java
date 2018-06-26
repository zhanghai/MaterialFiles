/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.util.SortedList;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.signature.ObjectKey;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Comparator;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.file.MimeTypes;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.glide.GlideApp;
import me.zhanghai.android.materialfilemanager.glide.IgnoreErrorDrawableImageViewTarget;
import me.zhanghai.android.materialfilemanager.ui.SortedListAdapter;
import me.zhanghai.android.materialfilemanager.util.StringCompat;
import me.zhanghai.android.materialfilemanager.util.TimeUtils;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class FileListAdapter extends SortedListAdapter<File, FileListAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private Comparator<File> mComparator;
    private final SortedList.Callback<File> mCallback = new SortedListAdapterCallback<File>(this) {
        @Override
        public int compare(File file1, File file2) {
            return mComparator.compare(file1, file2);
        }
        @Override
        public boolean areItemsTheSame(File oldItem, File newItem) {
            return oldItem == newItem || Objects.equals(oldItem.getPath(), newItem.getPath());
        }
        @Override
        public boolean areContentsTheSame(File oldItem, File newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };

    private Fragment mFragment;
    private Listener mListener;

    public FileListAdapter(Fragment fragment, Listener listener) {
        init(File.class, mCallback);
        mFragment = fragment;
        mListener = listener;
    }

    public void setComparator(Comparator<File> comparator) {
        mComparator = comparator;
        refresh();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(ViewUtils.inflate(R.layout.file_item, parent));
        holder.menu = new PopupMenu(holder.menuButton.getContext(), holder.menuButton);
        holder.menu.inflate(R.menu.file_item);
        holder.menuButton.setOnClickListener(view -> holder.menu.show());
        holder.menuButton.setOnTouchListener(holder.menu.getDragToOpenListener());
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = getItem(position);
        String mimeType = file.getMimeType();
        Drawable icon = AppCompatResources.getDrawable(holder.iconImage.getContext(),
                MimeTypes.getIconRes(mimeType));
        if (MimeTypes.supportsThumbnail(mimeType)) {
            GlideApp.with(mFragment)
                    .load(file.getPath())
                    .signature(new ObjectKey(file.getLastModified()))
                    .placeholder(icon)
                    .into(new IgnoreErrorDrawableImageViewTarget(holder.iconImage));
        } else {
            GlideApp.with(mFragment)
                    .clear(holder.iconImage);
            holder.iconImage.setImageDrawable(icon);
        }
        holder.nameText.setText(file.getName());
        String description;
        if (file.isDirectory()) {
            description = null;
        } else {
            String descriptionSeparator = holder.descriptionText.getContext().getString(
                    R.string.file_item_description_separator);
            String lastModified = TimeUtils.formatTime(file.getLastModified().toEpochMilli(),
                    holder.descriptionText.getContext());
            String size = Formatter.formatFileSize(holder.descriptionText.getContext(),
                    file.getSize());
            description = StringCompat.join(descriptionSeparator, lastModified, size);
        }
        holder.descriptionText.setText(description);
        holder.menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_open_as:
                    mListener.onOpenFileAs(file);
                    return true;
                case R.id.action_cut:
                    mListener.onCutFile(file);
                    return true;
                case R.id.action_copy:
                    mListener.onCopyFile(file);
                    return true;
                case R.id.action_delete:
                    mListener.onDeleteFile(file);
                    return true;
                case R.id.action_rename:
                    mListener.onRenameFile(file);
                    return true;
                case R.id.action_properties:
                    mListener.onOpenFileProperties(file);
                    return true;
                default:
                    return false;
            }
        });
        holder.itemView.setOnClickListener(view -> mListener.onOpenFile(file));
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        File file = getItem(position);
        String name = file.getName();
        if (TextUtils.isEmpty(name)) {
            return "";

        }
        return name.substring(0, 1).toUpperCase();
    }

    public interface Listener {
        void onOpenFile(File file);
        void onOpenFileAs(File file);
        void onCutFile(File file);
        void onCopyFile(File file);
        void onDeleteFile(File file);
        void onRenameFile(File file);
        void onOpenFileProperties(File file);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon)
        public ImageView iconImage;
        @BindView(R.id.name)
        public TextView nameText;
        @BindView(R.id.description)
        public TextView descriptionText;
        @BindView(R.id.menu)
        public ImageButton menuButton;

        public PopupMenu menu;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
