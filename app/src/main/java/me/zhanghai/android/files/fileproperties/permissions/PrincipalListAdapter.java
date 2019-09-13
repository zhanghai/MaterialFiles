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

import java.util.List;
import java.util.Objects;

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
import me.zhanghai.android.files.util.SelectionLiveData;
import me.zhanghai.android.files.util.ViewUtils;

public class PrincipalListAdapter
        extends SimpleAdapter<PrincipalItem, PrincipalListAdapter.ViewHolder> {

    @NonNull
    private final Fragment mFragment;
    @NonNull
    private final SelectionLiveData<Integer> mSelectionLiveData;

    public PrincipalListAdapter(@NonNull Fragment fragment,
                                @NonNull SelectionLiveData<Integer> selectionLiveData) {
        mFragment = fragment;
        mSelectionLiveData = selectionLiveData;
    }

    @Override
    protected boolean getHasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ViewUtils.inflate(R.layout.principal_item, parent));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        PrincipalItem principal = getItem(position);
        holder.itemLayout.setChecked(Objects.equals(mSelectionLiveData.getValue(), principal.id));
        if (!payloads.isEmpty()) {
            return;
        }
        holder.itemLayout.setOnClickListener(view -> mSelectionLiveData.setValue(principal.id));
        Drawable icon = AppCompatResources.getDrawable(holder.iconImage.getContext(),
                R.drawable.person_icon_control_normal_24dp);
        ApplicationInfo applicationInfo = CollectionUtils.firstOrNull(principal.applicationInfos);
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
        String principalString = principal.name != null ? holder.principalText.getContext().getString(
                R.string.file_properties_permissions_principal_format, principal.name, principal.id)
                : String.valueOf(principal.id);
        holder.principalText.setText(principalString);
        String label = !principal.applicationLabels.isEmpty() ? CollectionUtils.first(
                principal.applicationLabels) : holder.labelText.getResources().getString(
                R.string.file_properties_permissions_set_principal_system);
        holder.labelText.setText(label);
    }

    public int findPositionByPrincipalId(int id) {
        return findPositionById(id);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item)
        public CheckableForegroundLinearLayout itemLayout;
        @BindView(R.id.icon)
        public ImageView iconImage;
        @BindView(R.id.principal)
        public TextView principalText;
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
