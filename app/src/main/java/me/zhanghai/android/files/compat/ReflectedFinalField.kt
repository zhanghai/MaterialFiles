/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import androidx.annotation.RequiresApi
import me.zhanghai.android.files.util.andInv
import me.zhanghai.java.reflected.ReflectedClass
import me.zhanghai.java.reflected.ReflectedField
import java.lang.reflect.Field
import java.lang.reflect.Modifier

//@MaxApi(Build.VERSION_CODES.LOLLIPOP_MR1)
private val fieldArtFieldField = ReflectedField(Field::class.java, "artField")

//@MaxApi(Build.VERSION_CODES.LOLLIPOP_MR1)
private val artFieldClass = ReflectedClass<Any>("java.lang.reflect.ArtField")
//@MaxApi(Build.VERSION_CODES.LOLLIPOP_MR1)
private val artFieldAccessFlagsField = ReflectedField(artFieldClass, "accessFlags")

@RequiresApi(Build.VERSION_CODES.M)
private val fieldAccessFlagsField = ReflectedField(Field::class.java, "accessFlags")

class ReflectedFinalField<T> : ReflectedField<T> {
    constructor(declaringClass: Class<T>, fieldName: String) : super(declaringClass, fieldName)

    constructor(
        declaringClass: ReflectedClass<T>,
        fieldName: String
    ) : super(declaringClass, fieldName)

    constructor(
        declaringClassName: String,
        fieldName: String
    ) : super(declaringClassName, fieldName)

    override fun onGet(): Field =
        super.onGet().also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fieldAccessFlagsField.setInt(
                    it, fieldAccessFlagsField.getInt(it) andInv Modifier.FINAL
                )
            } else {
                val artField = fieldArtFieldField.getObject<Any>(it)
                artFieldAccessFlagsField.setInt(
                    artField, artFieldAccessFlagsField.getInt(artField) andInv Modifier.FINAL
                )
            }
        }
}
