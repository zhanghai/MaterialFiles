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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.ui.CheckableItemBackground;
import me.zhanghai.android.materialfilemanager.ui.CheckableLinearLayout;
import me.zhanghai.android.materialfilemanager.ui.SimpleAdapter;
import me.zhanghai.android.materialfilemanager.util.ViewUtils;

public class NavigationListAdapter extends SimpleAdapter<NavigationItem,
        RecyclerView.ViewHolder> {

    private static final Object PAYLOAD_CHECKED_CHANGED = new Object();

    private NavigationItem.Listener mListener;

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_DIVIDER = 1;

    public NavigationListAdapter(NavigationItem.Listener listener) {
        mListener = listener;
        setHasStableIds(true);
    }

    public void notifyCheckedChanged() {
        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_CHECKED_CHANGED);
    }

    @Override
    public long getItemId(int position) {
        NavigationItem item = getItem(position);
        if (item != null) {
            return item.getId();
        } else {
            return Collections.frequency(getList().subList(0, position), null);
        }
    }

    @Override
    public int getItemViewType(int position) {
        NavigationItem item = getItem(position);
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
                Drawable navigationItemBackground = CheckableItemBackground.create(context);
                holder.itemLayout.setBackground(navigationItemBackground);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_ITEM: {
                NavigationItem item = getItem(position);
                ItemHolder itemHolder = (ItemHolder) holder;
                itemHolder.itemLayout.setChecked(item.isChecked(mListener));
                if (!payloads.isEmpty()) {
                    return;
                }
                itemHolder.itemLayout.setOnClickListener(view -> item.onClick(mListener));
                itemHolder.iconImage.setImageDrawable(item.getIcon(
                        itemHolder.iconImage.getContext()));
                itemHolder.titleText.setText(item.getTitle(itemHolder.titleText.getContext()));
                itemHolder.subtitleText.setText(item.getSubtitle(itemHolder.subtitleText
                        .getContext()));
                break;
            }
            case VIEW_TYPE_DIVIDER:
                // Do nothing
                break;
            default:
                throw new IllegalArgumentException();
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
