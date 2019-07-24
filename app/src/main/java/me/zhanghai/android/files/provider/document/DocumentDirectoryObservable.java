/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

import java.io.IOException;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.AbstractDirectoryObservable;
import me.zhanghai.android.files.provider.content.resolver.Resolver;
import me.zhanghai.android.files.provider.content.resolver.ResolverException;
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver;

public class DocumentDirectoryObservable extends AbstractDirectoryObservable {

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

    public DocumentDirectoryObservable(@NonNull DocumentPath path, long intervalMillis)
            throws IOException {
        super(intervalMillis);

        Uri childrenUri;
        try {
            childrenUri = DocumentResolver.getDocumentChildrenUri(path);
        } catch (ResolverException e) {
            throw e.toFileSystemException(path.toString());
        }
        try {
            mCursor = Resolver.query(childrenUri, new String[0], null, null, null);
        } catch (ResolverException e) {
            throw e.toFileSystemException(path.toString());
        }
        mCursor.registerContentObserver(mContentObserver);
    }

    @Override
    protected void onCloseLocked() {
        mCursor.unregisterContentObserver(mContentObserver);
        mCursor.close();
    }
}
