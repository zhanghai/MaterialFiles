/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.util.AttributeSet;

import com.takisoft.preferencex.PreferenceFragmentCompat;
import com.takisoft.preferencex.SimpleMenuPreference;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filejob.FileJobService;

public class RootStrategyPreference extends SimpleMenuPreference {

    static {
        PreferenceFragmentCompat.registerPreferenceFragment(RootStrategyPreference.class,
                RootStrategyPreferenceDialogFragment.class);
    }

    public RootStrategyPreference(@NonNull Context context) {
        super(context);
    }

    public RootStrategyPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RootStrategyPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RootStrategyPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                  @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onClick() {
        int jobCount = FileJobService.getRunningJobCount();
        if (jobCount == 0) {
            super.onClick();
            return;
        }
        setDialogTitle(null);
        setDialogMessage(getContext().getResources().getQuantityString(
                R.plurals.settings_root_strategy_message_format, jobCount, jobCount));
        setPositiveButtonText(android.R.string.yes);
        setNegativeButtonText(R.string.maybe_later);
        getPreferenceManager().showDialog(this);
    }

    public void confirmClick() {
        super.onClick();
    }
}
