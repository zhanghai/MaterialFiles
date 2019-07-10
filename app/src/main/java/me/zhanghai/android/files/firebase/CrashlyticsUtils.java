/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.firebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import androidx.annotation.NonNull;
import io.fabric.sdk.android.Fabric;
import me.zhanghai.android.files.BuildConfig;

public class CrashlyticsUtils {

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    private CrashlyticsUtils() {}

    public static void init(@NonNull Context context) {
        if (BuildConfig.DEBUG) {
            return;
        }
        if (!verifyPackageName(context) || !verifySignature(context)) {
            // Please, don't spam.
            return;
        }
        Fabric.with(context, new Crashlytics(), new CrashlyticsNdk());
    }

    private static boolean verifyPackageName(@NonNull Context context) {
        return Objects.equals(context.getPackageName(), "me.zhanghai.android.files");
    }

    @SuppressLint("PackageManagerGetSignatures")
    private static boolean verifySignature(@NonNull Context context) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        if (packageInfo.signatures.length != 1) {
            return false;
        }
        return Objects.equals(computeCertificateFingerprint(packageInfo.signatures[0]),
                "87:3B:9B:60:C7:7C:F7:F3:CD:5F:AE:66:D0:FE:11:2C:4A:86:97:3E:11:8E:E8:A2:9C:34:6C"
                        + ":4C:67:3C:97:F0");
    }

    @NonNull
    private static String computeCertificateFingerprint(@NonNull Signature certificate) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        byte[] digest = messageDigest.digest(certificate.toByteArray());
        char[] chars = new char[3 * digest.length - 1];
        for (int i = 0; i < digest.length; ++i) {
            int b = digest[i] & 0xFF;
            chars[3 * i] = HEX_CHARS[b >>> 4];
            chars[3 * i + 1] = HEX_CHARS[b & 0x0F];
            if (i < digest.length - 1) {
                chars[3 * i + 2] = ':';
            }
        }
        return new String(chars);
    }
}
