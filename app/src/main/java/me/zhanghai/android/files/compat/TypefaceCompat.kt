/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.graphics.Typeface
import android.os.Build
import android.util.LongSparseArray
import android.util.SparseArray
import me.zhanghai.java.reflected.ReflectedField
import me.zhanghai.java.reflected.ReflectedMethod
import kotlin.reflect.KClass

private val defaultField = ReflectedFinalField(Typeface::class.java, "DEFAULT")

var KClass<Typeface>.DEFAULT_COMPAT: Typeface
    get() = defaultField.getObject(null)
    set(value) {
        defaultField.setObject(null, value)
    }

private val defaultBoldField = ReflectedFinalField(Typeface::class.java, "DEFAULT_BOLD")

var KClass<Typeface>.DEFAULT_BOLD_COMPAT: Typeface
    get() = defaultBoldField.getObject(null)
    set(value) {
        defaultBoldField.setObject(null, value)
    }

private val sansSerifField = ReflectedFinalField(Typeface::class.java, "SANS_SERIF")

var KClass<Typeface>.SANS_SERIF_COMPAT: Typeface
    get() = sansSerifField.getObject(null)
    set(value) {
        sansSerifField.setObject(null, value)
    }

private val defaultsField = ReflectedField(Typeface::class.java, "sDefaults")

val KClass<Typeface>.defaultsCompat: Array<Typeface>
    get() = defaultsField.getObject(null)

@RestrictedHiddenApi
private val setDefaultMethod = ReflectedMethod(
    Typeface::class.java, "setDefault", Typeface::class.java
)

fun KClass<Typeface>.setDefaultCompat(typeface: Typeface) {
    setDefaultMethod.invoke<Unit>(null, typeface)
}

private val systemFontMapField = ReflectedField(Typeface::class.java, "sSystemFontMap")

val KClass<Typeface>.systemFontMapCompat: MutableMap<String, Typeface>
    get() {
        val systemFontMap = systemFontMapField.getObject<MutableMap<String, Typeface>>(null)
        // TODO: Replace with Build.VERSION_CODES.S .
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.P until (Build.VERSION_CODES.R + 1)) {
            val mField = systemFontMap.javaClass.getDeclaredField("m")
            mField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return mField.get(systemFontMap) as MutableMap<String, Typeface>
        } else {
            return systemFontMap
        }
    }

//@MaxApi(Build.VERSION_CODES.O_MR1)
private val typefaceCacheField = ReflectedField(Typeface::class.java, "sTypefaceCache")

//@MaxApi(Build.VERSION_CODES.O_MR1)
val KClass<Typeface>.typefaceCacheCompat: LongSparseArray<SparseArray<Typeface>>
    get() = typefaceCacheField.getObject(null)
