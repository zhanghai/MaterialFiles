/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.io.File;

import me.zhanghai.android.materialfilemanager.file.MimeTypes;

public class IntentUtils {

    private static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    private static final String MIME_TYPE_IMAGE_ANY = "image/*";
    private static final String MIME_TYPE_ANY = "*/*";

    private IntentUtils() {}

    public static Intent withChooser(Intent intent) {
        return Intent.createChooser(intent, null);
    }

    public static Intent makeLaunchApp(String packageName, Context context) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    public static Intent makeMediaScan(Uri uri) {
        return new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                .setData(uri);
    }

    public static Intent makeMediaScan(File file) {
        return makeMediaScan(Uri.fromFile(file));
    }

    private static Intent makePickFile(String mimeType, String[] mimeTypes, boolean allowMultiple) {
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

    public static Intent makePickFile(boolean allowMultiple) {
        return makePickFile(MIME_TYPE_ANY, null, allowMultiple);
    }

    public static Intent makePickFile(String mimeType, boolean allowMultiple) {
        return makePickFile(mimeType, new String[] { mimeType }, allowMultiple);
    }

    public static Intent makePickFile(String[] mimeTypes, boolean allowMultiple) {
        String mimeType = mimeTypes != null && mimeTypes.length == 1 ? mimeTypes[0] : MIME_TYPE_ANY;
        return makePickFile(mimeType, mimeTypes, allowMultiple);
    }

    public static Intent makePickImage(boolean allowMultiple) {
        return makePickFile(MIME_TYPE_IMAGE_ANY, allowMultiple);
    }

    // TODO: Use android.support.v4.app.ShareCompat ?

    // NOTE: Before Build.VERSION_CODES.JELLY_BEAN htmlText will be no-op.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static Intent makeSendText(CharSequence text, String htmlText) {
        Intent intent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && htmlText != null) {
            intent.putExtra(Intent.EXTRA_HTML_TEXT, htmlText);
        }
        return intent.setType(MIME_TYPE_TEXT_PLAIN);
    }

    public static Intent makeSendText(CharSequence text) {
        return makeSendText(text, null);
    }

    public static Intent makeSendImage(Uri uri, CharSequence text) {
        return makeSendStream(uri, MIME_TYPE_IMAGE_ANY)
                // For maximum compatibility.
                .putExtra(Intent.EXTRA_TEXT, text)
                .putExtra(Intent.EXTRA_TITLE, text)
                .putExtra(Intent.EXTRA_SUBJECT, text)
                // HACK: WeChat moments respects this extra only.
                .putExtra("Kdescription", text);
    }

    public static Intent makeSendImage(Uri uri) {
        return makeSendImage(uri, null);
    }

    public static Intent makeSendStream(Uri stream, String type) {
        return new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, stream)
                .setType(getIntentType(type));
    }

    public static Intent makeView(Uri uri) {
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    public static Intent makeView(Uri uri, String type) {
        return new Intent(Intent.ACTION_VIEW)
                // Calling setType() will clear data.
                .setDataAndType(uri, getIntentType(type))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    private static String getIntentType(String type) {
        return MimeTypes.getIntentType(type);
    }

    public static Intent makeViewAppInMarket(String packageName) {
        return makeView(Uri.parse("market://details?id=" + packageName));
    }
}
