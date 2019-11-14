/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.IntentCompat;
import me.zhanghai.android.files.file.MimeTypes;

public class IntentUtils {

    private static final String ACTION_INSTALL_SHORTCUT =
            "com.android.launcher.action.INSTALL_SHORTCUT";

    private static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    private static final String MIME_TYPE_IMAGE_ANY = "image/*";
    private static final String MIME_TYPE_ANY = "*/*";

    private IntentUtils() {}

    @NonNull
    public static Intent withChooser(@Nullable Intent intent) {
        return Intent.createChooser(intent, null);
    }

    @NonNull
    public static Intent makeCaptureImage(@NonNull Uri outputUri) {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
    }

    @NonNull
    public static Intent makeInstallPackage(@NonNull Uri uri) {
        return new Intent(Intent.ACTION_INSTALL_PACKAGE)
                .setDataAndType(uri, MimeTypes.APK_MIME_TYPE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    @NonNull
    public static Intent makeInstallShortcut(int iconRes, int nameRes,
                                             @NonNull Class<?> intentClass,
                                             @NonNull Context context) {
        return new Intent()
                .setAction(ACTION_INSTALL_SHORTCUT)
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(context.getApplicationContext(),
                        intentClass))
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(nameRes))
                .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(context, iconRes));
    }

    @Nullable
    public static Intent makeLaunchApp(@NonNull String packageName, @NonNull Context context) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    @NonNull
    public static Intent makeMediaScan(@NonNull Uri uri) {
        return new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                .setData(uri);
    }

    @NonNull
    public static Intent makeMediaScan(@NonNull File file) {
        return makeMediaScan(Uri.fromFile(file));
    }

    @NonNull
    public static Intent makePickFile(boolean allowMultiple) {
        return makePickFile(MIME_TYPE_ANY, null, allowMultiple);
    }

    @NonNull
    public static Intent makePickFile(@NonNull String mimeType, boolean allowMultiple) {
        return makePickFile(mimeType, new String[] { mimeType }, allowMultiple);
    }

    @NonNull
    public static Intent makePickFile(@Nullable String[] mimeTypes, boolean allowMultiple) {
        String mimeType = mimeTypes != null && mimeTypes.length == 1 ? mimeTypes[0] : MIME_TYPE_ANY;
        return makePickFile(mimeType, mimeTypes, allowMultiple);
    }

    @NonNull
    private static Intent makePickFile(@NonNull String mimeType, @Nullable String[] mimeTypes,
                                       boolean allowMultiple) {
        // If not using ACTION_OPEN_DOCUMENT, URI permission can be lost after ~10 seconds.
        String action = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT;
        Intent intent = new Intent(action)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(mimeType);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mimeTypes != null && mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (allowMultiple) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
        }
        return intent;
    }

    @NonNull
    public static Intent makePickImage(boolean allowMultiple) {
        return makePickFile(MIME_TYPE_IMAGE_ANY, allowMultiple);
    }

    @NonNull
    public static Intent makePickOrCaptureImageWithChooser(boolean allowPickMultiple,
                                                           @NonNull Uri captureOutputUri) {
        return withChooser(makePickImage(allowPickMultiple))
                .putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[] {
                        makeCaptureImage(captureOutputUri)
                });
    }

    // TODO: Use android.support.v4.app.ShareCompat ?

    @NonNull
    public static Intent makeSendText(@NonNull CharSequence text, @Nullable String htmlText) {
        Intent intent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, text);
        if (htmlText != null) {
            intent.putExtra(IntentCompat.EXTRA_HTML_TEXT, htmlText);
        }
        return intent.setType(MIME_TYPE_TEXT_PLAIN);
    }

    @NonNull
    public static Intent makeSendText(@NonNull CharSequence text) {
        return makeSendText(text, null);
    }

    @NonNull
    public static Intent makeSendImage(@NonNull Uri uri, @Nullable CharSequence text) {
        Intent intent = makeSendStream(uri, MIME_TYPE_IMAGE_ANY);
        if (text != null) {
            intent
                    // For maximum compatibility.
                    .putExtra(Intent.EXTRA_TEXT, text)
                    .putExtra(Intent.EXTRA_TITLE, text)
                    .putExtra(Intent.EXTRA_SUBJECT, text)
                    // HACK: WeChat moments respects this extra only.
                    .putExtra("Kdescription", text);
        }
        return intent;
    }

    @NonNull
    public static Intent makeSendImage(@NonNull Uri uri) {
        return makeSendImage(uri, null);
    }

    @NonNull
    public static Intent makeSendStream(@NonNull Uri stream, @NonNull String type) {
        return new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, stream)
                .setType(MimeTypes.getIntentType(type));
    }

    @NonNull
    public static Intent makeSendStream(@NonNull List<Uri> streams, @NonNull List<String> types) {
        if (streams.size() == 1) {
            return makeSendStream(streams.get(0), types.get(0));
        }
        return new Intent(Intent.ACTION_SEND_MULTIPLE)
                .putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(streams))
                .setType(MimeTypes.getIntentType(types));
    }

    @NonNull
    public static Intent makeSyncSettings(@Nullable String[] authorities,
                                          @Nullable String[] accountTypes) {
        Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
        if (!ArrayUtils.isEmpty(authorities)) {
            intent.putExtra(Settings.EXTRA_AUTHORITIES, authorities);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!ArrayUtils.isEmpty(accountTypes)) {
                intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, accountTypes);
            }
        }
        return intent;
    }

    @NonNull
    public static Intent makeSyncSettingsWithAuthority(@Nullable String authority) {
        return makeSyncSettings(authority != null ? new String[] { authority } : null, null);
    }

    @NonNull
    public static Intent makeSyncSettingsWithAccountType(@Nullable String accountType) {
        return makeSyncSettings(null, accountType != null ? new String[] { accountType } : null);
    }

    @NonNull
    public static Intent makeSyncSettings() {
        return makeSyncSettings(null, null);
    }

    @NonNull
    public static Intent makeView(@NonNull Uri uri) {
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    @NonNull
    public static Intent makeView(@NonNull Uri uri, @NonNull String type) {
        return new Intent(Intent.ACTION_VIEW)
                // Calling setType() will clear data.
                .setDataAndType(uri, MimeTypes.getIntentType(type))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    @NonNull
    public static Intent makeViewAppInMarket(@NonNull String packageName) {
        return makeView(Uri.parse("market://details?id=" + packageName));
    }
}
