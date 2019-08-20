/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;

import com.takisoft.preferencex.EditTextPreference;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

public class PasswordPreference extends EditTextPreference {

    public PasswordPreference(@NonNull Context context) {
        super(context);

        init();
    }

    public PasswordPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public PasswordPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                              @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public PasswordPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                              @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        if (getSummaryProvider() instanceof EditTextPreference.SimpleSummaryProvider) {
            setSummaryProvider(SimpleSummaryProvider.getInstance());
        }
    }

    public static class SimpleSummaryProvider implements SummaryProvider<EditTextPreference> {

        @Nullable
        private static SimpleSummaryProvider sInstance;

        @NonNull
        public static SimpleSummaryProvider getInstance() {
            if (sInstance == null) {
                sInstance = new SimpleSummaryProvider();
            }
            return sInstance;
        }

        @Nullable
        @Override
        public CharSequence provideSummary(@NonNull EditTextPreference preference) {
            String text = preference.getText();
            if (TextUtils.isEmpty(text)) {
                return EditTextPreference.SimpleSummaryProvider.getInstance().provideSummary(
                        preference);
            }
            return PasswordTransformationMethod.getInstance().getTransformation(text, null);
        }
    }
}
