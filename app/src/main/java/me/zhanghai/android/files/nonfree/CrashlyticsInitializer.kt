/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.nonfree

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.google.firebase.crashlytics.FirebaseCrashlytics
import me.zhanghai.android.files.BuildConfig
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.util.getPackageInfoOrNull
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object CrashlyticsInitializer {
    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    fun initialize() {
        if (BuildConfig.DEBUG) {
            return
        }
        if (!verifyPackageName() || !verifySignature()) {
            // Please, don't spam.
            return
        }
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }

    private fun verifyPackageName(): Boolean {
        return application.packageName == "me.zhanghai.android.files"
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun verifySignature(): Boolean {
        val packageInfo = packageManager.getPackageInfoOrNull(
            application.packageName, PackageManager.GET_SIGNATURES
        ) ?: return false
        return packageInfo.signatures.size == 1
            && computeCertificateFingerprint(packageInfo.signatures[0]) == ("87:3B:9B:60:C7:7C:F7"
            + ":F3:CD:5F:AE:66:D0:FE:11:2C:4A:86:97:3E:11:8E:E8:A2:9C:34:6C:4C:67:3C:97:F0")
    }

    private fun computeCertificateFingerprint(certificate: Signature): String {
        val messageDigest = try {
            MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError(e)
        }
        val digest = messageDigest.digest(certificate.toByteArray())
        val chars = CharArray(3 * digest.size - 1)
        for (index in digest.indices) {
            val byte = digest[index].toInt() and 0xFF
            chars[3 * index] = HEX_CHARS[byte ushr 4]
            chars[3 * index + 1] = HEX_CHARS[byte and 0x0F]
            if (index < digest.size - 1) {
                chars[3 * index + 2] = ':'
            }
        }
        return String(chars)
    }
}
