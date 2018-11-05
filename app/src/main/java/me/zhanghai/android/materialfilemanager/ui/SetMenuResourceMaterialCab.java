/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.os.Bundle;

import com.afollestad.materialcab.MaterialCab;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SetMenuResourceMaterialCab extends com.afollestad.materialcab.MaterialCab {

    @MenuRes
    private int mMenuRes;

    public SetMenuResourceMaterialCab(@NonNull AppCompatActivity context, int attacherId) {
        super(context, attacherId);
    }

    @NonNull
    public static SetMenuResourceMaterialCab restoreState(@NonNull Bundle source,
                                                          @Nullable AppCompatActivity context,
                                                          @NonNull Callback callback) {
        return (SetMenuResourceMaterialCab) MaterialCab.restoreState(source, context, callback);
    }

    /**
     * @deprecated Use {@link #setMenuResource(int)} instead.
     */
    @NonNull
    @Override
    public com.afollestad.materialcab.MaterialCab setMenu(@MenuRes int menuRes) {
        mMenuRes = menuRes;
        return super.setMenu(menuRes);
    }

    public void setMenuResource(@MenuRes int menuRes) {
        if (mMenuRes != menuRes) {
            //noinspection deprecation
            setMenu(menuRes);
        }
    }
}
