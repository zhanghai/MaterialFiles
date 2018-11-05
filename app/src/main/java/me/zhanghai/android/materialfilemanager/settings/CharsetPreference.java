/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.settings;

import android.content.Context;
import android.util.AttributeSet;

import java.nio.charset.Charset;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.compat.Function;

public class CharsetPreference extends ListPreference {

    public CharsetPreference(@NonNull Context context) {
        super(context);

        init();
    }

    public CharsetPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CharsetPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                             int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public CharsetPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                             int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        Map<String, Charset> charsetMap = Charset.availableCharsets();
        CharSequence[] entries = charsetMap.keySet().toArray(new CharSequence[0]);
        setEntries(entries);
        CharSequence[] entryValues = Functional.map(charsetMap.values(),
                (Function<Charset, String>) Charset::displayName).toArray(new CharSequence[0]);
        setEntryValues(entryValues);
    }
}
