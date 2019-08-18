/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.lifecycle.Observer;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import me.zhanghai.android.files.compat.ListFormatterCompat;
import me.zhanghai.android.files.navigation.StandardDirectoriesLiveData;
import me.zhanghai.android.files.navigation.StandardDirectory;
import me.zhanghai.java.functional.Functional;

public class StandardDirectoriesPreference extends Preference {

    private final Observer<List<StandardDirectory>> mObserver = this::onStandardDirectoriesChanged;

    @Nullable
    private CharSequence mSummaryEmpty;

    public StandardDirectoriesPreference(@NonNull Context context) {
        super(context);

        init();
    }

    public StandardDirectoriesPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public StandardDirectoriesPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                         @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public StandardDirectoriesPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                         @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        setPersistent(false);
        mSummaryEmpty = getSummary();
    }

    @Override
    public void onAttached() {
        super.onAttached();

        StandardDirectoriesLiveData.getInstance().observeForever(mObserver);
    }

    @Override
    public void onDetached() {
        super.onDetached();

        StandardDirectoriesLiveData.getInstance().removeObserver(mObserver);
    }

    private void onStandardDirectoriesChanged(
            @NonNull List<StandardDirectory> standardDirectories) {
        Context context = getContext();
        List<String> titles = Functional.map(Functional.filter(standardDirectories,
                StandardDirectory::isEnabled), standardDirectory -> standardDirectory.getTitle(
                context));
        CharSequence summary = !titles.isEmpty() ? ListFormatterCompat.format(titles)
                : mSummaryEmpty;
        setSummary(summary);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        TextView summaryText = (TextView) holder.findViewById(android.R.id.summary);
        summaryText.setSingleLine(true);
        summaryText.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    protected void onClick() {
        Context context = getContext();
        context.startActivity(StandardDirectoriesActivity.newIntent(context));
    }
}
