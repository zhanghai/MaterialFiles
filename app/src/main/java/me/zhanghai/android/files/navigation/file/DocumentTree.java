/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation.file;

import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import java.util.List;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.compat.DocumentsContractCompat;
import me.zhanghai.android.files.navigation.DocumentTreesLiveData;
import me.zhanghai.java.functional.Functional;

public class DocumentTree {

    @NonNull
    public static List<Uri> getPersistedUris(@NonNull Context context) {
        return Functional.map(Functional.filter(
                context.getContentResolver().getPersistedUriPermissions(),
                uriPermission -> DocumentsContractCompat.isTreeUri(uriPermission.getUri())),
                UriPermission::getUri);
    }

    public static String getDisplayName(@NonNull Uri treeUri, @NonNull Context context) {
        Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri,
                DocumentsContract.getTreeDocumentId(treeUri));
        try (Cursor cursor = context.getContentResolver().query(documentUri,
                new String[] { DocumentsContract.Document.COLUMN_DISPLAY_NAME }, null, null,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int displayNameIndex = cursor.getColumnIndex(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                if (displayNameIndex != -1) {
                    String displayName = cursor.getString(displayNameIndex);
                    if (!TextUtils.isEmpty(displayName)) {
                        return displayName;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return treeUri.toString();
    }

    @NonNull
    public static Intent makeOpenIntent() {
        // TODO: Consider StorageVolume.createAccessIntent().
        return new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    }

    public static void takePersistablePermission(@NonNull Uri treeUri, @NonNull Context context) {
        if (takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION, context)
                || takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION,
                context)) {
            DocumentTreesLiveData.getInstance().loadValue();
        }
    }

    private static boolean takePersistableUriPermission(@NonNull Uri uri, int modeFlags,
                                                        @NonNull Context context) {
        try {
            context.getContentResolver().takePersistableUriPermission(uri, modeFlags);
            return true;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }
}
