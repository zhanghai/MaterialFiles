package me.zhanghai.android.files.theme.custom

import androidx.annotation.ColorRes
import me.zhanghai.android.files.R

enum class CustomThemeColor(
    @ColorRes val resourceId: Int,
    val resourceEntryName: String
) {
    COLOR_PRIMARY(R.color.color_primary_classic, "color_primary"),
    MATERIAL_RED_500(R.color.material_red_500, "material_red_500"),
    MATERIAL_PINK_500(R.color.material_pink_500, "material_pink_500"),
    MATERIAL_PURPLE_500(R.color.material_purple_500, "material_purple_500"),
    MATERIAL_DEEP_PURPLE_500(R.color.material_deep_purple_500, "material_deep_purple_500"),
    MATERIAL_INDIGO_500(R.color.material_indigo_500, "material_indigo_500"),
    MATERIAL_BLUE_500(R.color.material_blue_500, "material_blue_500"),
    MATERIAL_LIGHT_BLUE_500(R.color.material_light_blue_500, "material_light_blue_500"),
    MATERIAL_CYAN_500(R.color.material_cyan_500, "material_cyan_500"),
    MATERIAL_TEAL_500(R.color.material_teal_500, "material_teal_500"),
    MATERIAL_GREEN_500(R.color.material_green_500, "material_green_500"),
    MATERIAL_LIGHT_GREEN_500(R.color.material_light_green_500, "material_light_green_500"),
    MATERIAL_LIME_500(R.color.material_lime_500, "material_lime_500"),
    MATERIAL_YELLOW_500(R.color.material_yellow_500, "material_yellow_500"),
    MATERIAL_AMBER_500(R.color.material_amber_500, "material_amber_500"),
    MATERIAL_ORANGE_500(R.color.material_orange_500, "material_orange_500"),
    MATERIAL_DEEP_ORANGE_500(R.color.material_deep_orange_500, "material_deep_orange_500"),
    MATERIAL_BROWN_500(R.color.material_brown_500, "material_brown_500"),
    MATERIAL_GREY_500(R.color.material_grey_500, "material_grey_500"),
    MATERIAL_BLUE_GREY_500(R.color.material_blue_grey_500, "material_blue_grey_500");
}
