/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.typeface

import android.graphics.Typeface
import android.os.Build
import me.zhanghai.android.files.compat.DEFAULT_BOLD_COMPAT
import me.zhanghai.android.files.compat.DEFAULT_COMPAT
import me.zhanghai.android.files.compat.FontStyleCompat
import me.zhanghai.android.files.compat.SANS_SERIF_COMPAT
import me.zhanghai.android.files.compat.defaultsCompat
import me.zhanghai.android.files.compat.setDefaultCompat
import me.zhanghai.android.files.compat.systemFontMapCompat
import me.zhanghai.android.files.compat.typefaceCacheCompat

object TypefaceHelper {
    private const val FAMILY_NAME_SANS_SERIF = "sans-serif"

    private val WEIGHT_VARIANT_SUFFIXES = mapOf(
        FontStyleCompat.FONT_WEIGHT_THIN to "-thin",
        FontStyleCompat.FONT_WEIGHT_EXTRA_LIGHT to "-extra-light",
        FontStyleCompat.FONT_WEIGHT_LIGHT to "-light",
        FontStyleCompat.FONT_WEIGHT_MEDIUM to "-medium",
        FontStyleCompat.FONT_WEIGHT_SEMI_BOLD to "-semi-bold",
        FontStyleCompat.FONT_WEIGHT_EXTRA_BOLD to "-extra-bold",
        FontStyleCompat.FONT_WEIGHT_BLACK to "-black"
    )

    fun isDefaultTypeface(familyName: String): Boolean =
        Typeface.create(familyName, Typeface.DEFAULT.style) == Typeface.DEFAULT

    fun replaceDefaultAndSansSerifTypefaces(newFamilyName: String) {
        val systemFontMap = Typeface::class.systemFontMapCompat
        val newTypeface = systemFontMap[newFamilyName]!!
        systemFontMap[FAMILY_NAME_SANS_SERIF] = newTypeface

        Typeface::class.setDefaultCompat(newTypeface)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Typeface::class.typefaceCacheCompat.clear()
        }

        Typeface::class.DEFAULT_COMPAT = newTypeface
        val newBoldTypeface = Typeface.create(null as String?, Typeface.BOLD)
        Typeface::class.DEFAULT_BOLD_COMPAT = newBoldTypeface
        Typeface::class.SANS_SERIF_COMPAT = newTypeface

        val defaults = Typeface::class.defaultsCompat
        defaults[Typeface.NORMAL] = newTypeface
        defaults[Typeface.BOLD] = newBoldTypeface
        defaults[Typeface.ITALIC] = Typeface.create(null as String?, Typeface.ITALIC)
        defaults[Typeface.BOLD_ITALIC] = Typeface.create(null as String?, Typeface.BOLD_ITALIC)

        replaceWeightVariantTypefaces(systemFontMap, FAMILY_NAME_SANS_SERIF, newFamilyName)
    }

    private fun replaceWeightVariantTypefaces(
        systemFontMap: MutableMap<String, Typeface>,
        oldFamilyName: String,
        newFamilyName: String
    ) {
        for ((weight, weightVariantSuffix) in WEIGHT_VARIANT_SUFFIXES) {
            val oldWeightVariantFamilyName = oldFamilyName + weightVariantSuffix
            if (oldWeightVariantFamilyName !in systemFontMap) {
                continue
            }
            val newWeightVariantTypeface =
                getOrCreateWeightVariantTypeface(systemFontMap, newFamilyName, weight)
            if (newWeightVariantTypeface != null) {
                systemFontMap[oldWeightVariantFamilyName] = newWeightVariantTypeface
            }
        }
    }

    private fun getOrCreateWeightVariantTypeface(
        systemFontMap: MutableMap<String, Typeface>,
        familyName: String,
        weight: Int
    ): Typeface? {
        systemFontMap[familyName + WEIGHT_VARIANT_SUFFIXES[weight]]?.let { return it }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface.create(systemFontMap[familyName], weight, false)
                .takeIf { it.weight == weight }
                ?.let { return it }
        }
        return null
    }
}
