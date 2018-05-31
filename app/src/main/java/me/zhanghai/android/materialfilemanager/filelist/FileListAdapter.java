/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class FileListAdapter extends ListAdapter<File, FileListAdapter.ViewHolder> {

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

    public FileListAdapter() {
        super(sDiffCallback);

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        // TODO
        return getItem(position).hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ViewUtils.inflate(R.layout.file_item, parent));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = getItem(position);
        holder.nameText.setText(file.getName());
        holder.descriptionText.setText(file.getType().name());
        holder.itemView.setOnClickListener(view -> {
            // TODO
        });
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
