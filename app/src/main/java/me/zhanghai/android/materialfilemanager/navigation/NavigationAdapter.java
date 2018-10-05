/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.navigation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.ui.CheckableLinearLayout;
import me.zhanghai.android.materialfilemanager.ui.SimpleAdapter;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class NavigationAdapter extends SimpleAdapter<NavigationAdapter.Item,
        RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_DIVIDER = 1;

    public NavigationAdapter() {
        setHasStableIds(true);

        // TODO: Debugging
        add(new Item(R.drawable.file_icon_white_24dp, "Root", "56.89 MB free of 1.45 GB"));
        add(new Item(R.drawable.file_icon_white_24dp, "Internal Storage", "465.21 MB free of 744.89 MB"));
        add(new Item(R.drawable.directory_icon_white_24dp, "Directory", null));
        add(new Item(R.drawable.file_icon_white_24dp, "File", null));
        add(new Item(R.drawable.directory_icon_white_24dp, "Directory", null));
        add(new Item(R.drawable.file_icon_white_24dp, "File", null));
        add(new Item(R.drawable.directory_icon_white_24dp, "Directory", null));
        add(new Item(R.drawable.file_icon_white_24dp, "File", null));
        add(null);
        add(new Item(R.drawable.directory_icon_white_24dp, "Directory", "Description"));
        add(new Item(R.drawable.file_icon_white_24dp, "File", "Description"));
        add(new Item(R.drawable.directory_icon_white_24dp, "Directory", "Description"));
        add(new Item(R.drawable.file_icon_white_24dp, "File", "Description"));
        add(new Item(R.drawable.directory_icon_white_24dp, "Directory", "Description"));
        add(new Item(R.drawable.file_icon_white_24dp, "File", "Description"));
        add(null);
        add(new Item(R.drawable.directory_icon_white_24dp, "Directory", null));
        add(new Item(R.drawable.file_icon_white_24dp, "File", null));
        add(new Item(R.drawable.directory_icon_white_24dp, "Directory", null));
        add(new Item(R.drawable.file_icon_white_24dp, "File", null));
        add(null);
        add(new Item(R.drawable.directory_icon_white_24dp, "Directory", null));
        add(new Item(R.drawable.file_icon_white_24dp, "File", null));
    }

    @Override
    public long getItemId(int position) {
        Item item = getItem(position);
        if (item != null) {
            return item.title.hashCode();
        } else {
            return Collections.frequency(getList().subList(0, position), null);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Item item = getItem(position);
        if (item != null) {
            return VIEW_TYPE_ITEM;
        } else {
            return VIEW_TYPE_DIVIDER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_ITEM: {
                ItemHolder holder = new ItemHolder(ViewUtils.inflate(R.layout.navigation_item,
                        parent));
                Context context = holder.itemView.getContext();
                Drawable navigationItemBackground = NavigationItemBackground.create(context);
                holder.itemLayout.setBackground(navigationItemBackground);
                holder.itemLayout.setOnClickListener(view -> {
                    // TODO
                });
                holder.iconImage.setImageTintList(NavigationItemColor.create(
                        holder.iconImage.getImageTintList(), holder.iconImage.getContext()));
                holder.titleText.setTextColor(NavigationItemColor.create(
                        holder.titleText.getTextColors(), holder.titleText.getContext()));
                holder.subtitleText.setTextColor(NavigationItemColor.create(
                        holder.subtitleText.getTextColors(), holder.subtitleText.getContext()));
                return holder;
            }
            case VIEW_TYPE_DIVIDER:
                return new DividerHolder(ViewUtils.inflate(R.layout.navigation_divider_item,
                        parent));
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_ITEM: {
                ItemHolder itemHolder = (ItemHolder) holder;
                itemHolder.itemLayout.setChecked(/* TODO */ position == 1);
                Item item = getItem(position);
                itemHolder.iconImage.setImageResource(item.iconRes);
                itemHolder.titleText.setText(item.title);
                itemHolder.subtitleText.setText(item.subtitle);
                break;
            }
            case VIEW_TYPE_DIVIDER:
                // Do nothing
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static class Item {

        public int iconRes;
        public String title;
        public String subtitle;

        public Item(int iconRes, String title, String subtitle) {
            this.iconRes = iconRes;
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item)
        CheckableLinearLayout itemLayout;
        @BindView(R.id.icon)
        ImageView iconImage;
        @BindView(R.id.title)
        TextView titleText;
        @BindView(R.id.subtitle)
        TextView subtitleText;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    static class DividerHolder extends RecyclerView.ViewHolder {

        public DividerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
