#!/bin/bash
set -e

THEME_SUFFIXES=(
    ''
    .TransparentStatusBar
    .Translucent
    .Immersive
)
PRIMARY_COLORS=(
    color_primary
    material_red_500
    material_pink_500
    material_purple_500
    material_deep_purple_500
    material_indigo_500
    material_blue_500
    material_light_blue_500
    material_cyan_500
    material_teal_500
    material_green_500
    material_light_green_500
    material_lime_500
    material_yellow_500
    material_amber_500
    material_orange_500
    material_deep_orange_500
    material_brown_500
    material_grey_500
    material_blue_grey_500
)
DARK_PRIMARY_COLORS=(
    color_primary_dark
    material_red_700
    material_pink_700
    material_purple_700
    material_deep_purple_700
    material_indigo_700
    material_blue_700
    material_light_blue_700
    material_cyan_700
    material_teal_700
    material_green_700
    material_light_green_700
    material_lime_700
    material_yellow_700
    material_amber_700
    material_orange_700
    material_deep_orange_700
    material_brown_700
    material_grey_700
    material_blue_grey_700
)
ACCENT_COLORS=(
    color_accent
    material_red_a200
    material_pink_a200
    material_purple_a200
    material_deep_purple_a200
    material_indigo_a200
    material_blue_a200
    material_light_blue_500
    material_cyan_500
    material_teal_500
    material_green_500
    material_light_green_500
    material_lime_500
    material_yellow_500
    material_amber_500
    material_orange_500
    material_deep_orange_500
    material_brown_500
    material_grey_500
    material_blue_grey_500
)
CUSTOM_THEMES_XML='../app/src/main/res/values/themes_custom.xml'
CUSTOM_THEME_COLORS_JAVA='../app/src/main/java/me/zhanghai/android/files/theme/custom/CustomThemeColors.java'

cat >"${CUSTOM_THEMES_XML}" <<EOF
<?xml version="1.0" encoding="utf-8"?>

<resources>
EOF
for theme_suffix in "${THEME_SUFFIXES[@]}"; do
    for primary_color_index in "${!PRIMARY_COLORS[@]}"; do
        primary_color="${PRIMARY_COLORS[primary_color_index]}"
        dark_primary_color="${DARK_PRIMARY_COLORS[primary_color_index]}"
        cat >>"${CUSTOM_THEMES_XML}" <<EOF

    <style name="Theme.MaterialFiles${theme_suffix}.${primary_color}">
        <item name="colorPrimary">@color/${primary_color}</item>
        <item name="colorPrimaryDark">@color/${dark_primary_color}</item>
    </style>
EOF
        for accent_color in "${ACCENT_COLORS[@]}"; do
            cat >>"${CUSTOM_THEMES_XML}" <<EOF
    <style name="Theme.MaterialFiles${theme_suffix}.${primary_color}.${accent_color}">
        <item name="colorAccent">@color/${accent_color}</item>
    </style>
EOF
        done
    done
done
cat >>"${CUSTOM_THEMES_XML}" <<EOF
</resources>
EOF

cat >"${CUSTOM_THEME_COLORS_JAVA}" <<EOF
package me.zhanghai.android.files.theme.custom;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import me.zhanghai.android.files.R;

public class CustomThemeColors {

    private CustomThemeColors() {}

    public enum Primary implements CustomThemeColor {

EOF
for primary_color_index in "${!PRIMARY_COLORS[@]}"; do
    primary_color="${PRIMARY_COLORS[primary_color_index]}"
    if [[ "${primary_color_index}" -lt "$(("${#PRIMARY_COLORS[@]}" - 1))" ]]; then
        separator=',';
    else
        separator=';';
    fi
    cat >>"${CUSTOM_THEME_COLORS_JAVA}" <<EOF
        ${primary_color^^}(R.color.${primary_color}, "${primary_color}")${separator}
EOF
done
cat >>"${CUSTOM_THEME_COLORS_JAVA}" <<EOF

        @ColorRes
        private final int mResourceId;
        @NonNull
        private final String mResourceEntryName;

        Primary(@ColorRes int resourceId, @NonNull String resourceEntryName) {
            mResourceId = resourceId;
            mResourceEntryName = resourceEntryName;
        }

        @ColorRes
        @Override
        public int getResourceId() {
            return mResourceId;
        }

        @NonNull
        @Override
        public String getResourceEntryName() {
            return mResourceEntryName;
        }
    }

    public enum Accent implements CustomThemeColor {

EOF
for accent_color_index in "${!ACCENT_COLORS[@]}"; do
    accent_color="${ACCENT_COLORS[accent_color_index]}"
    if [[ "${accent_color_index}" -lt "$(("${#ACCENT_COLORS[@]}" - 1))" ]]; then
        separator=',';
    else
        separator=';';
    fi
    cat >>"${CUSTOM_THEME_COLORS_JAVA}" <<EOF
        ${accent_color^^}(R.color.${accent_color}, "${accent_color}")${separator}
EOF
done
cat >>"${CUSTOM_THEME_COLORS_JAVA}" <<EOF

        @ColorRes
        private final int mResourceId;
        @NonNull
        private final String mResourceEntryName;

        Accent(@ColorRes int resourceId, @NonNull String resourceEntryName) {
            mResourceId = resourceId;
            mResourceEntryName = resourceEntryName;
        }

        @ColorRes
        @Override
        public int getResourceId() {
            return mResourceId;
        }

        @NonNull
        @Override
        public String getResourceEntryName() {
            return mResourceEntryName;
        }
    }
}
EOF
