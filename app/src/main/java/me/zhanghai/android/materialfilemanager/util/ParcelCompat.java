/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.os.Parcel;
import android.text.TextUtils;

public class ParcelCompat {

    private ParcelCompat() {}

    /**
     * @see androidx.core.os.ParcelCompat#readBoolean(Parcel)
     */
    public static boolean readBoolean(Parcel in) {
        return androidx.core.os.ParcelCompat.readBoolean(in);
    }

    /**
     * @see androidx.core.os.ParcelCompat#writeBoolean(Parcel, boolean)
     */
    public static void writeBoolean(Parcel out, boolean value) {
        androidx.core.os.ParcelCompat.writeBoolean(out, value);
    }

    /*
     * @see android.os.Parcel#readCharSequence()
     */
    public static CharSequence readCharSequence(Parcel in) {
        return TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
    }

    /*
     * @see android.os.Parcel#writeCharSequence(CharSequence)
     */
    public static void writeCharSequence(Parcel out, CharSequence value) {
        TextUtils.writeToParcel(value, out, 0);
    }
}
