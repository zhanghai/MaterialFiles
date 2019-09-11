/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.glide.GlideApp;
import me.zhanghai.android.files.glide.IgnoreErrorDrawableImageViewTarget;
import me.zhanghai.android.files.ui.CheckableForegroundLinearLayout;
import me.zhanghai.android.files.ui.SimpleAdapter;
import me.zhanghai.android.files.util.CollectionUtils;
import me.zhanghai.android.files.util.ViewUtils;

public class UserListAdapter extends SimpleAdapter<UserItem, UserListAdapter.ViewHolder> {

    @NonNull
    private final Fragment mFragment;

    public UserListAdapter(@NonNull Fragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected boolean getHasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).uid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ViewUtils.inflate(R.layout.user_item, parent));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserItem user = getItem(position);
        holder.itemLayout.setChecked(false /* TODO */);
        holder.itemLayout.setOnClickListener(view -> { /* TODO */ });
        Drawable icon = AppCompatResources.getDrawable(holder.iconImage.getContext(),
                R.drawable.person_icon_control_normal_24dp);
        ApplicationInfo applicationInfo = CollectionUtils.firstOrNull(user.applicationInfos);
        if (applicationInfo != null) {
            GlideApp.with(mFragment)
                    .load(applicationInfo)
                    .placeholder(icon)
                    .into(new IgnoreErrorDrawableImageViewTarget(holder.iconImage));
        } else {
            GlideApp.with(mFragment)
                    .clear(holder.iconImage);
            holder.iconImage.setImageDrawable(icon);
        }
        String userString = user.name != null ? holder.userText.getContext().getString(
                R.string.file_properties_permissions_user_format, user.name, user.uid)
                : String.valueOf(user.uid);
        holder.userText.setText(userString);
        String label = !user.applicationLabels.isEmpty() ? CollectionUtils.first(
                user.applicationLabels) : holder.labelText.getResources().getString(
                R.string.file_properties_permissions_change_owner_system);
        holder.labelText.setText(label);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item)
        public CheckableForegroundLinearLayout itemLayout;
        @BindView(R.id.icon)
        public ImageView iconImage;
        @BindView(R.id.user)
        public TextView userText;
        @BindView(R.id.label)
        public TextView labelText;
        @BindView(R.id.radio)
        public RadioButton radioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
