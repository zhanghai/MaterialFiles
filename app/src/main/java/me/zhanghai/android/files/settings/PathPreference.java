/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.takisoft.preferencex.PreferenceActivityResultListener;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.Objects;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.preference.Preference;
import java8.nio.file.Path;
import me.zhanghai.android.files.filelist.FileListActivity;
import me.zhanghai.android.files.filelist.FileUtils;
import me.zhanghai.android.files.navigation.NavigationRoot;
import me.zhanghai.android.files.navigation.NavigationRootMapLiveData;
import me.zhanghai.android.files.util.IntentPathUtils;

public abstract class PathPreference extends Preference
        implements PreferenceActivityResultListener {

    private Path mPath;
    private boolean mPathSet;

    public PathPreference(@NonNull Context context) {
        super(context);

        init();
    }

    public PathPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public PathPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                              @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public PathPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                              @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        // We must set a default value here, otherwise onSetInitialValue() may not be called in
        // Preference.dispatchSetInitialValue().
        setDefaultValue(new Object());
    }

    @Override
    public CharSequence getSummary() {
        CharSequence summary = super.getSummary();
        if (TextUtils.isEmpty(summary)) {
            return summary;
        }
        Path path = getPath();
        String pathString = "";
        if (path != null) {
            NavigationRoot navigationRoot = NavigationRootMapLiveData.getInstance().getValue().get(
                    path);
            pathString = navigationRoot != null ? navigationRoot.getName(getContext())
                    : FileUtils.getPathString(path);
        }
        return String.format(summary.toString(), pathString);
    }

    @Override
    public void onPreferenceClick(@NonNull PreferenceFragmentCompat fragment,
                                  @NonNull Preference preference) {
        Intent intent = FileListActivity.newPickDirectoryIntent(mPath, getContext());
        fragment.startActivityForResult(intent, getRequestCode());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != getRequestCode()) {
            return;
        }
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        Path path = IntentPathUtils.getExtraPath(data);
        setPath(path);
    }

    private int getRequestCode() {
        // @see FragmentActivity#checkForValidRequestCode()
        return getKey().hashCode() & 0x0000FFFF;
    }

    public void setPath(Path path) {
        // Always persist/notify the first time.
        boolean changed = !Objects.equals(mPath, path);
        if (changed || !mPathSet) {
            mPath = path;
            mPathSet = true;
            persistPath(path);
            if (changed) {
                notifyChanged();
            }
        }
    }

    public Path getPath() {
        return mPath;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setPath(getPersistedPath());
    }

    protected abstract Path getPersistedPath();

    protected abstract void persistPath(Path path);
}
