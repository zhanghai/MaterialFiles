/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.permissions;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import java.util.Collections;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.util.ViewUtils;

public class ModeBitListAdapter extends BaseAdapter {

    @NonNull
    private final PosixFileModeBit[] mModeBits;
    @NonNull
    private final String[] mModeBitNames;

    @NonNull
    private Set<PosixFileModeBit> mMode = Collections.emptySet();

    public ModeBitListAdapter(@NonNull PosixFileModeBit[] modeBits,
                              @NonNull String[] modeBitNames) {
        mModeBits = modeBits;
        mModeBitNames = modeBitNames;
    }

    public void setMode(@NonNull Set<PosixFileModeBit> mode) {
        mMode = mode;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mModeBits.length;
    }

    @NonNull
    @Override
    public PosixFileModeBit getItem(int position) {
        return mModeBits[position];
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).ordinal();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = ViewUtils.inflate(R.layout.mode_bit_item, parent);
            view.setTag(new ViewHolder(view));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.mModeBitCheck.setText(mModeBitNames[position]);
        PosixFileModeBit modeBit = getItem(position);
        holder.mModeBitCheck.setChecked(mMode.contains(modeBit));
        return view;
    }

    static class ViewHolder {

        @BindView(R.id.mode_bit)
        public CheckBox mModeBitCheck;

        public ViewHolder(@NonNull View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
