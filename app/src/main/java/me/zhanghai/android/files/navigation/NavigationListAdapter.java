/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.ui.CheckableLinearLayout;
import me.zhanghai.android.files.ui.SimpleAdapter;
import me.zhanghai.android.files.util.ViewUtils;

public class NavigationListAdapter extends SimpleAdapter<NavigationItem,
        RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_DIVIDER = 1;

    private static final Object PAYLOAD_CHECKED_CHANGED = new Object();

    @NonNull
    private final NavigationItem.Listener mListener;

    public NavigationListAdapter(@NonNull NavigationItem.Listener listener) {
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
                if (Settings.MATERIAL_DESIGN_2.getValue()) {
                    Context context = holder.itemLayout.getContext();
                    holder.itemLayout.setBackground(createItemBackgroundMd2(context));
                    // FIXME: Use a ForegroundLinearLayout.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.itemLayout.setForeground(createItemForegroundMd2(context));
                    }
                } else {
                    holder.itemLayout.setBackground(AppCompatResources.getDrawable(
                            holder.itemLayout.getContext(), R.drawable.navigation_item_background));
                }
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

    @NonNull
    private static Drawable createItemBackgroundMd2(@NonNull Context context) {
        // @see com.google.android.material.navigation.NavigationView#createDefaultItemBackground(TintTypedArray)
        return createItemShapeDrawableMd2(AppCompatResources.getColorStateList(context,
                R.color.mtrl_navigation_item_background_color), context);
    }

    @NonNull
    private static Drawable createItemForegroundMd2(@NonNull Context context) {
        Drawable mask = createItemShapeDrawableMd2(ColorStateList.valueOf(Color.WHITE), context);
        int controlHighlightColor = ViewUtils.getColorFromAttrRes(R.attr.colorControlHighlight, 0,
                context);
        return new RippleDrawable(ColorStateList.valueOf(controlHighlightColor), null, mask);
    }

    @NonNull
    private static Drawable createItemShapeDrawableMd2(@NonNull ColorStateList fillColor,
                                                       @NonNull Context context) {
        // @see com.google.android.material.navigation.NavigationView#createDefaultItemBackground(TintTypedArray)
        MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(
                ShapeAppearanceModel.builder(context, R.style.ShapeAppearance_Google_Navigation, 0)
                        .build());
        materialShapeDrawable.setFillColor(fillColor);
        int insetRight = ViewUtils.dpToPxSize(8, context);
        return new InsetDrawable(materialShapeDrawable, 0, 0, insetRight, 0);
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
                itemHolder.itemLayout.setOnLongClickListener(view -> item.onLongClick(mListener));
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
