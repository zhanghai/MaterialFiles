/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.settings.SettingsLiveDatas;

public class CreateArchiveTypeRadioGroup extends RadioGroup {

    @Nullable
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CreateArchiveTypeRadioGroup(@NonNull Context context) {
        super(context);

        init();
    }

    public CreateArchiveTypeRadioGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        check(SettingsLiveDatas.CREATE_ARCHIVE_TYPE.getValue());
        super.setOnCheckedChangeListener((group, checkedId) -> {
            SettingsLiveDatas.CREATE_ARCHIVE_TYPE.putValue(checkedId);
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(group, checkedId);
            }
        });
    }

    @Override
    public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }
}
