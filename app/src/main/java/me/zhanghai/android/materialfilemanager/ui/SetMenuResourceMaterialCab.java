/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialcab.MaterialCab;

public class SetMenuResourceMaterialCab extends com.afollestad.materialcab.MaterialCab {

    private int mMenuRes;

    public SetMenuResourceMaterialCab(@NonNull AppCompatActivity context, int attacherId) {
        super(context, attacherId);
    }

    public static SetMenuResourceMaterialCab restoreState(Bundle source, AppCompatActivity context,
                                                          Callback callback) {
        return (SetMenuResourceMaterialCab) MaterialCab.restoreState(source, context, callback);
    }

    /**
     * @deprecated Use {@link #setMenuResource(int)} instead.
     */
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
