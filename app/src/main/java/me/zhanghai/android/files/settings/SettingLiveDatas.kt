/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.content.SharedPreferences
import android.os.Parcel
import androidx.annotation.AnyRes
import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes
import androidx.annotation.DimenRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.core.content.edit
import me.zhanghai.android.files.app.appClassLoader
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.util.Base64
import me.zhanghai.android.files.util.asBase64
import me.zhanghai.android.files.util.getBoolean
import me.zhanghai.android.files.util.getFloat
import me.zhanghai.android.files.util.getInteger
import me.zhanghai.android.files.util.getStringArray
import me.zhanghai.android.files.util.toBase64
import me.zhanghai.android.files.util.toByteArray
import me.zhanghai.android.files.util.use

class StringSettingLiveData(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @StringRes defaultValueRes: Int
) : SettingLiveData<String>(nameSuffix, keyRes, keySuffix, defaultValueRes) {
    constructor(@StringRes keyRes: Int, @StringRes defaultValueRes: Int) : this(
        null, keyRes, null, defaultValueRes
    )

    init {
        init()
    }

    override fun getDefaultValue(@StringRes defaultValueRes: Int): String =
        application.getString(defaultValueRes)

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: String
    ): String = sharedPreferences.getString(key, defaultValue)!!

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: String) {
        sharedPreferences.edit { putString(key, value) }
    }
}

class StringSetSettingLiveData(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @ArrayRes defaultValueRes: Int
) : SettingLiveData<Set<String>>(nameSuffix, keyRes, keySuffix, defaultValueRes) {
    constructor(@StringRes keyRes: Int, @ArrayRes defaultValueRes: Int) : this(
        null, keyRes, null, defaultValueRes
    )

    init {
        init()
    }

    override fun getDefaultValue(@StringRes defaultValueRes: Int): Set<String> =
        application.getStringArray(defaultValueRes).toSet()

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: Set<String>
    ): Set<String> = sharedPreferences.getStringSet(key, defaultValue)!!

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: Set<String>) {
        sharedPreferences.edit { putStringSet(key, value) }
    }
}

class IntegerSettingLiveData(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @IntegerRes defaultValueRes: Int
) : SettingLiveData<Int>(nameSuffix, keyRes, keySuffix, defaultValueRes) {
    constructor(@StringRes keyRes: Int, @IntegerRes defaultValueRes: Int) : this(
        null, keyRes, null, defaultValueRes
    )

    init {
        init()
    }

    override fun getDefaultValue(@IntegerRes defaultValueRes: Int): Int =
        application.getInteger(defaultValueRes)

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: Int
    ): Int = sharedPreferences.getInt(key, defaultValue)

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: Int) {
        sharedPreferences.edit { putInt(key, value) }
    }
}

class LongSettingLiveData(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @StringRes defaultValueRes: Int
) : SettingLiveData<Long>(nameSuffix, keyRes, keySuffix, defaultValueRes) {
    constructor(@StringRes keyRes: Int, @StringRes defaultValueRes: Int) : this(
        null, keyRes, null, defaultValueRes
    )

    init {
        init()
    }

    override fun getDefaultValue(@StringRes defaultValueRes: Int): Long =
        application.getString(defaultValueRes).toLong()

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: Long
    ): Long =
        sharedPreferences.getLong(key, defaultValue)

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: Long) {
        sharedPreferences.edit { putLong(key, value) }
    }
}

class FloatSettingLiveData(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @DimenRes defaultValueRes: Int
) : SettingLiveData<Float>(nameSuffix, keyRes, keySuffix, defaultValueRes) {
    constructor(@StringRes keyRes: Int, @DimenRes defaultValueRes: Int) : this(
        null, keyRes, null, defaultValueRes
    )

    init {
        init()
    }

    override fun getDefaultValue(@DimenRes defaultValueRes: Int): Float =
        application.getFloat(defaultValueRes)

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: Float
    ): Float = sharedPreferences.getFloat(key, defaultValue)

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: Float) {
        sharedPreferences.edit { putFloat(key, value) }
    }
}

class BooleanSettingLiveData(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @BoolRes defaultValueRes: Int
) : SettingLiveData<Boolean>(nameSuffix, keyRes, keySuffix, defaultValueRes) {
    constructor(@StringRes keyRes: Int, @BoolRes defaultValueRes: Int) : this(
        null, keyRes, null, defaultValueRes
    )

    init {
        init()
    }

    override fun getDefaultValue(@BoolRes defaultValueRes: Int): Boolean =
        application.getBoolean(defaultValueRes)

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: Boolean
    ): Boolean = sharedPreferences.getBoolean(key, defaultValue)

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }
}

// Use string resource for default value so that we can support ListPreference.
class EnumSettingLiveData<E : Enum<E>>(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @StringRes defaultValueRes: Int,
    enumClass: Class<E>
) : SettingLiveData<E>(nameSuffix, keyRes, keySuffix, defaultValueRes) {
    private val enumValues = enumClass.enumConstants!!

    constructor(
        @StringRes keyRes: Int,
        @StringRes defaultValueRes: Int,
        enumClass: Class<E>
    ) : this(null, keyRes, null, defaultValueRes, enumClass)

    init {
        init()
    }

    override fun getDefaultValue(@StringRes defaultValueRes: Int): E =
        enumValues[application.getString(defaultValueRes).toInt()]

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: E
    ): E {
        val valueOrdinal = sharedPreferences.getString(key, null)?.toInt() ?: return defaultValue
        return if (valueOrdinal in enumValues.indices) enumValues[valueOrdinal] else defaultValue
    }

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: E) {
        sharedPreferences.edit { putString(key, value.ordinal.toString()) }
    }
}

class ResourceIdSettingLiveData(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @AnyRes defaultValue: Int
) : SettingLiveData<Int>(nameSuffix, keyRes, keySuffix, defaultValue) {
    constructor(@StringRes keyRes: Int, @AnyRes defaultValue: Int) : this(
        null, keyRes, null, defaultValue
    )

    init {
        init()
    }

    @AnyRes
    override fun getDefaultValue(@AnyRes defaultValueRes: Int): Int = defaultValueRes

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        @AnyRes defaultValue: Int
    ): Int {
        val valueString = sharedPreferences.getString(key, null) ?: return defaultValue
        val value = application.resources.getIdentifier(valueString, null, application.packageName)
        return if (value != 0) value else defaultValue
    }

    override fun putValue(sharedPreferences: SharedPreferences, key: String, @AnyRes value: Int) {
        sharedPreferences.edit { putString(key, application.resources.getResourceName(value)) }
    }
}

class ParcelValueSettingLiveData<T>(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    private val defaultValue: T
) : SettingLiveData<T>(nameSuffix, keyRes, keySuffix, 0) {
    constructor(@StringRes keyRes: Int, defaultValue: T) : this(null, keyRes, null, defaultValue)

    init {
        init()
    }

    override fun getDefaultValue(@AnyRes defaultValueRes: Int): T = defaultValue

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: T
    ): T =
        try {
            sharedPreferences.getString(key, null)?.asBase64()?.toParcelValue()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: defaultValue

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: T) {
        sharedPreferences.edit { putString(key, value?.toParcelBase64()?.value) }
    }

    private fun Base64.toParcelValue(): T {
        val bytes = toByteArray()
        return Parcel.obtain().use { parcel ->
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            @Suppress("UNCHECKED_CAST")
            parcel.readValue(appClassLoader) as T
        }
    }

    private fun T.toParcelBase64(): Base64 {
        val bytes = Parcel.obtain().use { parcel ->
            parcel.writeValue(this)
            parcel.marshall()
        }
        return bytes.toBase64()
    }
}
