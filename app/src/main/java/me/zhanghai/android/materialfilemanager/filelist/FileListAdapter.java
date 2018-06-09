/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.signature.ObjectKey;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.file.MimeTypes;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.glide.GlideApp;
import me.zhanghai.android.materialfilemanager.util.StringCompat;
import me.zhanghai.android.materialfilemanager.util.TimeUtils;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class FileListAdapter extends ListAdapter<File, FileListAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private static final DiffUtil.ItemCallback<File> sDiffCallback =
            new DiffUtil.ItemCallback<File>() {
                @Override
                public boolean areItemsTheSame(File oldItem, File newItem) {
                    return Objects.equals(oldItem.getPath(), newItem.getPath())
                            // TODO: For moving files
                            || oldItem == newItem;
                }
                @Override
                public boolean areContentsTheSame(File oldItem, File newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private Fragment mFragment;
    private Listener mListener;

    public FileListAdapter(Fragment fragment, Listener listener) {
        super(sDiffCallback);

        mFragment = fragment;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ViewUtils.inflate(R.layout.file_item, parent));
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
                    .signature(new ObjectKey(file.getModified()))
                    .placeholder(icon)
                    .into(holder.iconImage);
        } else {
            GlideApp.with(mFragment)
                    .clear(holder.iconImage);
            holder.iconImage.setImageDrawable(icon);
        }
        holder.nameText.setText(file.getName());
        String descriptionSeparator = holder.descriptionText.getContext().getString(
                R.string.file_description_separator);
        String modified = TimeUtils.formatTime(file.getModified().toEpochMilli(),
                holder.descriptionText.getContext());
        String size = Formatter.formatFileSize(holder.descriptionText.getContext(), file.getSize());
        String description = StringCompat.join(descriptionSeparator, modified, size);
        holder.descriptionText.setText(description);
        holder.itemView.setOnClickListener(view -> mListener.onFileSelected(file));
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
        void onFileSelected(File file);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon)
        ImageView iconImage;
        @BindView(R.id.name)
        TextView nameText;
        @BindView(R.id.description)
        TextView descriptionText;
        @BindView(R.id.more)
        ImageButton moreButton;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
