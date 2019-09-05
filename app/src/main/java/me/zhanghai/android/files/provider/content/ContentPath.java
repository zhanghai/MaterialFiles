/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content;

import android.net.Uri;
import android.os.Parcel;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileSystem;
import java8.nio.file.LinkOption;
import java8.nio.file.WatchEvent;
import java8.nio.file.WatchKey;
import java8.nio.file.WatchService;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringListPath;
import me.zhanghai.android.files.provider.content.resolver.Resolver;
import me.zhanghai.android.files.provider.content.resolver.ResolverException;

class ContentPath extends ByteStringListPath<ContentPath> {

    @NonNull
    private final ContentFileSystem mFileSystem;

    private final Uri mUri;

    public ContentPath(@NonNull ContentFileSystem fileSystem, @NonNull Uri uri) {
        super((byte) 0, true, Collections.singletonList(getDisplayName(uri)));

        mFileSystem = fileSystem;
        mUri = uri;
    }

    private ContentPath(@NonNull ContentFileSystem fileSystem, @NonNull List<ByteString> names) {
        super((byte) 0, false, names);

        mFileSystem = fileSystem;
        mUri = null;
    }

    @NonNull
    private static ByteString getDisplayName(@NonNull Uri uri) {
        String fileName = null;
        try {
            fileName = Resolver.getDisplayName(uri);
        } catch (ResolverException e) {
            e.printStackTrace();
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        if (fileName == null) {
            fileName = uri.toString();
        }
        return ByteString.fromString(fileName);
    }

    public Uri getUri() {
        return mUri;
    }

    @Override
    protected boolean isPathAbsolute(@NonNull ByteString path) {
        throw new AssertionError();
    }

    @Override
    protected ContentPath createPath(@NonNull ByteString path) {
        return new ContentPath(mFileSystem, Uri.parse(path.toString()));
    }

    @NonNull
    @Override
    protected ContentPath createPath(boolean absolute, @NonNull List<ByteString> names) {
        if (absolute) {
            if (names.size() != 1) {
                throw new IllegalArgumentException(names.toString());
            }
            return createPath(names.get(0));
        } else {
            return new ContentPath(mFileSystem, names);
        }
    }

    @Nullable
    @Override
    protected ByteString getUriSchemeSpecificPart() {
        throw new AssertionError();
    }

    @Nullable
    @Override
    protected ByteString getUriFragment() {
        throw new AssertionError();
    }

    @NonNull
    @Override
    protected ContentPath getDefaultDirectory() {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public FileSystem getFileSystem() {
        return mFileSystem;
    }

    @Nullable
    @Override
    public ContentPath getRoot() {
        return null;
    }

    @NonNull
    @Override
    public ContentPath normalize() {
        return this;
    }

    @NonNull
    @Override
    public URI toUri() {
        try {
            return new URI(mUri.toString());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    @NonNull
    @Override
    public ContentPath toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @NonNull
    @Override
    public ContentPath toRealPath(@NonNull LinkOption... options) {
        Objects.requireNonNull(options);
        return this;
    }

    @NonNull
    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public WatchKey register(@NonNull WatchService watcher, @NonNull WatchEvent.Kind<?>[] events,
                             @NonNull WatchEvent.Modifier... modifiers) {
        Objects.requireNonNull(watcher);
        Objects.requireNonNull(events);
        Objects.requireNonNull(modifiers);
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public ByteString toByteString() {
        if (mUri != null) {
            return ByteString.fromString(mUri.toString());
        }
        return super.toByteString();
    }

    @NonNull
    @Override
    @SuppressWarnings("deprecation")
    public String toString() {
        if (mUri != null) {
            return mUri.toString();
        }
        return super.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ContentPath that = (ContentPath) object;
        if (mUri != null || that.mUri != null) {
            return Objects.equals(mUri, that.mUri);
        }
        return super.equals(that);
    }

    @Override
    public int hashCode() {
        if (mUri != null) {
            return Objects.hashCode(mUri);
        }
        return super.hashCode();
    }


    public static final Creator<ContentPath> CREATOR = new Creator<ContentPath>() {
        @Override
        public ContentPath createFromParcel(Parcel source) {
            return new ContentPath(source);
        }
        @Override
        public ContentPath[] newArray(int size) {
            return new ContentPath[size];
        }
    };

    protected ContentPath(Parcel in) {
        super(in);

        mFileSystem = in.readParcelable(ContentFileSystem.class.getClassLoader());
        mUri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mFileSystem, flags);
        dest.writeParcelable(mUri, flags);
    }
}
