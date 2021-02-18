#!/bin/bash
set -e

CUSTOM_COLORS=(
    color_primary
    material_red
    material_pink
    material_purple
    material_deep_purple
    material_indigo
    material_blue
    material_light_blue
    material_cyan
    material_teal
    material_green
    material_light_green
    material_lime
    material_yellow
    material_amber
    material_orange
    material_deep_orange
    material_brown
    material_grey
    material_blue_grey
)
CUSTOM_COLORS_LIGHT=(
    color_primary_light
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
CUSTOM_COLORS_DARK=(
    color_primary_dark
    material_red_300
    material_pink_300
    material_purple_300
    material_deep_purple_300
    material_indigo_300
    material_blue_300
    material_light_blue_300
    material_cyan_300
    material_teal_300
    material_green_300
    material_light_green_300
    material_lime_300
    material_yellow_300
    material_amber_300
    material_orange_300
    material_deep_orange_300
    material_brown_300
    material_grey_300
    material_blue_grey_300
)
CUSTOM_COLORS_XML='../app/src/main/res/values/colors_custom.xml'
CUSTOM_COLORS_NIGHT_XML='../app/src/main/res/values-night/colors_custom.xml'
THEME_SUFFIXES=(
    ''
    .Translucent
    .Immersive
)
CUSTOM_THEMES_XML='../app/src/main/res/values/themes_custom.xml'
CUSTOM_THEMES_NIGHT_XML='../app/src/main/res/values-night/themes_custom.xml'
CUSTOM_THEME_COLOR_KT='../app/src/main/java/me/zhanghai/android/files/theme/custom/ThemeColor.kt'

cat >"${CUSTOM_COLORS_XML}" <<EOF
<?xml version="1.0" encoding="utf-8"?>

<resources>
EOF
for color_index in "${!CUSTOM_COLORS[@]}"; do
    color="${CUSTOM_COLORS[color_index]}"
    if [[ "${color}" == 'color_primary' ]]; then
        continue
    fi
    light_color="${CUSTOM_COLORS_LIGHT[color_index]}"
    cat >>"${CUSTOM_COLORS_XML}" <<EOF
    <color name="${color}">@color/${light_color}</color>
EOF
done
cat >>"${CUSTOM_COLORS_XML}" <<EOF
</resources>
EOF

cat >"${CUSTOM_COLORS_NIGHT_XML}" <<EOF
<?xml version="1.0" encoding="utf-8"?>

<resources>
EOF
for color_index in "${!CUSTOM_COLORS[@]}"; do
    color="${CUSTOM_COLORS[color_index]}"
    if [[ "${color}" == 'color_primary' ]]; then
        continue
    fi
    dark_color="${CUSTOM_COLORS_DARK[color_index]}"
    cat >>"${CUSTOM_COLORS_NIGHT_XML}" <<EOF
    <color name="${color}">@color/${dark_color}</color>
EOF
done
cat >>"${CUSTOM_COLORS_NIGHT_XML}" <<EOF
</resources>
EOF

cat >"${CUSTOM_THEMES_XML}" <<EOF
<?xml version="1.0" encoding="utf-8"?>

<resources>

EOF
for color_index in "${!CUSTOM_COLORS[@]}"; do
    for theme_suffix in "${THEME_SUFFIXES[@]}"; do
        color="${CUSTOM_COLORS[color_index]}"
        if [[ "${theme_suffix}" == '.Immersive' ]]; then
            primary_color="${CUSTOM_COLORS_DARK[color_index]}"
        else
            primary_color="${color}"
        fi
        cat >>"${CUSTOM_THEMES_XML}" <<EOF
    <style name="Theme.MaterialFiles${theme_suffix}.${color}">
        <item name="colorPrimary">@color/${primary_color}</item>
    </style>
EOF
    done
done
cat >>"${CUSTOM_THEMES_XML}" <<EOF

EOF
for color_index in "${!CUSTOM_COLORS[@]}"; do
    for theme_suffix in "${THEME_SUFFIXES[@]}"; do
        color="${CUSTOM_COLORS[color_index]}"
        cat >>"${CUSTOM_THEMES_XML}" <<EOF
    <style name="Theme.MaterialFiles${theme_suffix}.${color}.Black" />
EOF
    done
done
cat >>"${CUSTOM_THEMES_XML}" <<EOF
</resources>
EOF

cat >"${CUSTOM_THEMES_NIGHT_XML}" <<EOF
<?xml version="1.0" encoding="utf-8"?>

<resources>

EOF
for color_index in "${!CUSTOM_COLORS[@]}"; do
    for theme_suffix in "${THEME_SUFFIXES[@]}"; do
        color="${CUSTOM_COLORS[color_index]}"
        cat >>"${CUSTOM_THEMES_NIGHT_XML}" <<EOF
    <style name="Theme.MaterialFiles${theme_suffix}.${color}.Black">
        <item name="colorPrimaryDark">@color/color_primary_dark_black</item>
        <item name="colorSurface">@android:color/black</item>
    </style>
EOF
    done
done
cat >>"${CUSTOM_THEMES_NIGHT_XML}" <<EOF
</resources>
EOF

cat >"${CUSTOM_THEME_COLOR_KT}" <<EOF
package me.zhanghai.android.files.theme.custom

import androidx.annotation.ColorRes
import me.zhanghai.android.files.R

enum class ThemeColor(@ColorRes val resourceId: Int) {
EOF
for color_index in "${!CUSTOM_COLORS[@]}"; do
    color="${CUSTOM_COLORS[color_index]}"
    if [[ "${color_index}" -lt "$(("${#CUSTOM_COLORS[@]}" - 1))" ]]; then
        separator=',';
    else
        separator=';';
    fi
    cat >>"${CUSTOM_THEME_COLOR_KT}" <<EOF
    ${color^^}(R.color.${color})${separator}
EOF
done
cat >>"${CUSTOM_THEME_COLOR_KT}" <<EOF
}
EOF
