/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.app.ShareCompat
import me.zhanghai.android.files.app.appClassLoader
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.compat.removeFlagsCompat
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.intentType
import kotlin.reflect.KClass

fun <T : Context> KClass<T>.createIntent(): Intent = Intent(application, java)

fun CharSequence.createSendTextIntent(htmlText: String? = null): Intent =
    // The context parameter here is only used for passing calling activity information and starting
    // chooser activity, neither of which we care about.
    ShareCompat.IntentBuilder(application)
        .setType(MimeType.TEXT_PLAIN.value)
        .setText(this)
        .apply { htmlText?.let { setHtmlText(it) } }
        .intent
        // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET is unnecessarily added by ShareCompat.IntentBuilder.
        .apply {
            @Suppress("DEPRECATION")
            removeFlagsCompat(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        }

fun KClass<Intent>.createLaunchApp(packageName: String): Intent? =
    packageManager.getLaunchIntentForPackage(packageName)

fun KClass<Intent>.createPickImage(allowMultiple: Boolean = false): Intent =
    MimeType.IMAGE_ANY.createPickFileIntent(allowMultiple)

fun KClass<Intent>.createPickOrCaptureImageWithChooser(
    allowPickMultiple: Boolean = false,
    captureOutputUri: Uri
): Intent =
    createPickImage(allowPickMultiple)
        .withChooser(captureOutputUri.createCaptureImage())

fun KClass<Intent>.createSyncSettings(
    authorities: Array<out String>? = null,
    accountTypes: Array<out String>? = null
): Intent =
    Intent(Settings.ACTION_SYNC_SETTINGS).apply {
        if (!authorities.isNullOrEmpty()) {
            putExtra(Settings.EXTRA_AUTHORITIES, authorities)
        }
        if (!accountTypes.isNullOrEmpty()) {
            putExtra(Settings.EXTRA_ACCOUNT_TYPES, accountTypes)
        }
    }

fun KClass<Intent>.createSyncSettingsWithAuthorities(vararg authorities: String) =
    createSyncSettings(authorities = authorities)

fun KClass<Intent>.createSyncSettingsWithAccountType(vararg accountTypes: String) =
    createSyncSettings(accountTypes = accountTypes)

fun KClass<Intent>.createViewAppInMarket(packageName: String): Intent =
    Uri.parse("market://details?id=$packageName").createViewIntent()

// @see com.android.documentsui.inspector.InspectorController.createGeoIntent
// @see https://developer.android.com/guide/components/intents-common.html#Maps
fun KClass<Intent>.createViewLocation(latitude: Float, longitude: Float, label: String): Intent =
    Uri.parse("geo:0,0?q=$latitude,$longitude(${Uri.encode(label)})").createViewIntent()

fun <T : Parcelable> Intent.getParcelableExtraSafe(key: String): T? {
    setExtrasClassLoader(appClassLoader)
    return getParcelableExtra(key)
}

fun Intent.getParcelableArrayExtraSafe(key: String): Array<Parcelable>? {
    setExtrasClassLoader(appClassLoader)
    return getParcelableArrayExtra(key)
}

fun <T : Parcelable?> Intent.getParcelableArrayListExtraSafe(key: String): ArrayList<T>? {
    setExtrasClassLoader(appClassLoader)
    return getParcelableArrayListExtra(key)
}

fun Intent.withChooser(title: CharSequence? = null, vararg initialIntents: Intent): Intent =
    Intent.createChooser(this, title).apply {
        putExtra(Intent.EXTRA_INITIAL_INTENTS, initialIntents)
    }

fun Intent.withChooser(vararg initialIntents: Intent) = withChooser(null, *initialIntents)

fun Uri.createEditIntent(mimeType: MimeType): Intent =
    Intent(Intent.ACTION_EDIT)
        // Calling setType() will clear data.
        .setDataAndType(this, mimeType.intentType)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

fun MimeType.createPickFileIntent(allowMultiple: Boolean = false) =
    Intent(Intent.ACTION_OPEN_DOCUMENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType(value)
        .apply {
            if (allowMultiple) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }

fun Collection<MimeType>.createPickFileIntent(allowMultiple: Boolean = false): Intent =
    (singleOrNull() ?: MimeType.ANY).createPickFileIntent(allowMultiple)
        .apply {
            if (size > 1) {
                putExtra(Intent.EXTRA_MIME_TYPES, map { it.value }.toTypedArray())
            }
        }

fun Uri.createSendImageIntent(text: CharSequence? = null): Intent =
    createSendStreamIntent(MimeType.IMAGE_ANY).apply {
        text?.let {
            // For maximum compatibility.
            putExtra(Intent.EXTRA_TEXT, it)
            putExtra(Intent.EXTRA_TITLE, it)
            putExtra(Intent.EXTRA_SUBJECT, it)
            // HACK: WeChat moments respects this extra only.
            putExtra("Kdescription", it)
        }
    }

fun Uri.createSendStreamIntent(mimeType: MimeType): Intent =
    listOf(this).createSendStreamIntent(listOf(mimeType))

fun Collection<Uri>.createSendStreamIntent(mimeTypes: Collection<MimeType>): Intent =
    // Use ShareCompat.IntentBuilder for its migrateExtraStreamToClipData() because
    // Intent.migrateExtraStreamToClipData() won't promote child ClipData and flags to the chooser
    // intent, breaking third party share sheets.
    // The context parameter here is only used for passing calling activity information and starting
    // chooser activity, neither of which we care about.
    ShareCompat.IntentBuilder(application)
        .setType(mimeTypes.intentType)
        .apply { forEach { addStream(it) } }
        .intent
        // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET is unnecessarily added by ShareCompat.IntentBuilder.
        .apply {
            @Suppress("DEPRECATION")
            removeFlagsCompat(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        }

fun Uri.createViewIntent(): Intent = Intent(Intent.ACTION_VIEW, this)

fun Uri.createViewIntent(mimeType: MimeType): Intent =
    Intent(Intent.ACTION_VIEW)
        // Calling setType() will clear data.
        .setDataAndType(this, mimeType.intentType)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

@Suppress("DEPRECATION")
fun Uri.createInstallPackageIntent(): Intent =
    Intent(Intent.ACTION_INSTALL_PACKAGE)
        .setDataAndType(this, MimeType.APK.value)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

fun Uri.createCaptureImage(): Intent =
    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        .putExtra(MediaStore.EXTRA_OUTPUT, this)
