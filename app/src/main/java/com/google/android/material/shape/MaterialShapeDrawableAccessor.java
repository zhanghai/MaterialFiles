/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.google.android.material.shape;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.elevation.ElevationOverlayProvider;

public class MaterialShapeDrawableAccessor {
    private MaterialShapeDrawableAccessor() {}

    public static ElevationOverlayProvider getElevationOverlayProvider(
            @NonNull MaterialShapeDrawable drawable) {
        MaterialShapeDrawable.MaterialShapeDrawableState drawableState =
                (MaterialShapeDrawable.MaterialShapeDrawableState) drawable.getConstantState();
        return drawableState.elevationOverlayProvider;
    }

    public static void setElevationOverlayProvider(
            @NonNull MaterialShapeDrawable drawable,
            @Nullable ElevationOverlayProvider elevationOverlayProvider) {
        MaterialShapeDrawable.MaterialShapeDrawableState drawableState =
                (MaterialShapeDrawable.MaterialShapeDrawableState) drawable.getConstantState();
        drawableState.elevationOverlayProvider = elevationOverlayProvider;
    }

    public static void updateZ(@NonNull MaterialShapeDrawable drawable) {
        final float parentAbsoluteElevation = drawable.getParentAbsoluteElevation();
        drawable.setParentAbsoluteElevation(parentAbsoluteElevation + 1);
        drawable.setParentAbsoluteElevation(parentAbsoluteElevation);
    }
}
