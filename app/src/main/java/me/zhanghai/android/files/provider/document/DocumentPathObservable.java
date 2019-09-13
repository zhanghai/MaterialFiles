/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.provider.common.AbstractPathObservable;
import me.zhanghai.android.files.provider.content.resolver.ResolverException;
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver;

public class DocumentPathObservable extends AbstractPathObservable {

    @NonNull
    private final Cursor mCursor;

    @NonNull
    private final ContentObserver mContentObserver = new ContentObserver(getHandler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        @Override
        public void onChange(boolean selfChange) {
            notifyObservers();
        }
    };

    public DocumentPathObservable(@NonNull DocumentPath path, long intervalMillis)
            throws IOException {
        super(intervalMillis);

        Uri uri;
        try {
            uri = getObservableUri(path);
        } catch (ResolverException e) {
            throw e.toFileSystemException(path.toString());
        }
        try {
            mCursor = DocumentResolver.query(uri, new String[0], null);
        } catch (ResolverException e) {
            throw e.toFileSystemException(path.toString());
        }
        mCursor.registerContentObserver(mContentObserver);
    }

    @NonNull
    private static Uri getObservableUri(@NonNull DocumentPath path) throws ResolverException {
        // Querying children for a regular file is fine for non-directory since API 29, but for
        // older APIs we'll have to work around by observing all children of its parent.
        // @see com.android.internal.content.FileSystemProvider#queryChildDocuments(String,
        //      String[], String)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            String mimeType = DocumentResolver.getMimeType(path);
            if (!Objects.equals(mimeType, MimeTypes.DIRECTORY_MIME_TYPE)) {
                return DocumentResolver.getDocumentChildrenUri(path.getParent());
            }
        }
        return DocumentResolver.getDocumentChildrenUri(path);
    }

    @Override
    protected void onCloseLocked() {
        mCursor.unregisterContentObserver(mContentObserver);
        mCursor.close();
    }
}
