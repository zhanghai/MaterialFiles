/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
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
import me.zhanghai.android.files.navigation.BookmarkDirectory;
import me.zhanghai.java.functional.Functional;

public class BookmarkDirectoriesPreference extends Preference {

    private final Observer<List<BookmarkDirectory>> mObserver = this::onBookmarkDirectoriesChanged;

    @Nullable
    private CharSequence mSummaryEmpty;

    public BookmarkDirectoriesPreference(@NonNull Context context) {
        super(context);

        init();
    }

    public BookmarkDirectoriesPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public BookmarkDirectoriesPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                         @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public BookmarkDirectoriesPreference(@NonNull Context context, @Nullable AttributeSet attrs,
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

        Settings.BOOKMARK_DIRECTORIES.observeForever(mObserver);
    }

    @Override
    public void onDetached() {
        super.onDetached();

        Settings.BOOKMARK_DIRECTORIES.removeObserver(mObserver);
    }

    private void onBookmarkDirectoriesChanged(
            @NonNull List<BookmarkDirectory> bookmarkDirectories) {
        List<String> names = Functional.map(bookmarkDirectories, BookmarkDirectory::getName);
        CharSequence summary = !names.isEmpty() ? ListFormatterCompat.format(names) : mSummaryEmpty;
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
        context.startActivity(BookmarkDirectoriesActivity.newIntent(context));
    }
}
