/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.AccessDeniedException;
import java8.nio.file.FileSystemException;
import java8.nio.file.FileSystemLoopException;
import java8.nio.file.FileSystems;
import java8.nio.file.Files;
import java8.nio.file.NoSuchFileException;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.Paths;
import java8.nio.file.StandardOpenOption;
import me.zhanghai.android.files.BuildConfig;
import me.zhanghai.android.files.compat.ProxyFileDescriptorCallbackCompat;
import me.zhanghai.android.files.compat.StorageManagerCompat;
import me.zhanghai.android.files.compat.StorageVolumeCompat;
import me.zhanghai.android.files.provider.common.ForceableChannel;
import me.zhanghai.android.files.provider.common.InvalidFileNameException;
import me.zhanghai.android.files.provider.common.IsDirectoryException;
import me.zhanghai.android.files.provider.common.MoreFiles;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.java.functional.Functional;

public class FileProvider extends ContentProvider {

    private static final String[] COLUMNS = {
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE,
            MediaStore.MediaColumns.DATA
    };

    private HandlerThread mCallbackThread;
    private Handler mCallbackHandler;

    @Override
    public boolean onCreate() {
        mCallbackThread = new HandlerThread("FileProvider.CallbackThread");
        mCallbackThread.start();
        mCallbackHandler = new Handler(mCallbackThread.getLooper());
        return true;
    }

    @Override
    public void shutdown() {
        mCallbackHandler = null;
        mCallbackThread.quitSafely();
        mCallbackThread = null;
    }

    @Override
    public void attachInfo(@NonNull Context context, @NonNull ProviderInfo info) {
        super.attachInfo(context, info);

        if (info.exported) {
            throw new SecurityException("Provider must not be exported");
        }
        if (!info.grantUriPermissions) {
            throw new SecurityException("Provider must grant uri permissions");
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // ContentProvider has already checked granted permissions
        Path path = getPathForUri(uri);
        if (projection == null) {
            projection = COLUMNS;
        }
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (String column : projection) {
            if (TextUtils.isEmpty(column)) {
                continue;
            }
            switch (column) {
                case OpenableColumns.DISPLAY_NAME:
                    columns.add(column);
                    values.add(path.getFileName().toString());
                    break;
                case OpenableColumns.SIZE: {
                    long size;
                    try {
                        size = MoreFiles.size(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                        size = 0;
                    }
                    columns.add(column);
                    values.add(size);
                    break;
                }
                case MediaStore.MediaColumns.DATA: {
                    File file;
                    try {
                        file = path.toFile();
                    } catch (UnsupportedOperationException e) {
                        continue;
                    }
                    String absolutePath = file.getAbsolutePath();
                    columns.add(column);
                    values.add(absolutePath);
                    break;
                }
            }
        }
        MatrixCursor cursor = new MatrixCursor(columns.toArray(new String[columns.size()]), 1);
        cursor.addRow(values);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        Path path = getPathForUri(uri);
        return MimeTypes.getMimeType(path.toString());
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("No external inserts");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("No external updates");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("No external deletes");
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        // ContentProvider has already checked granted permissions
        Path path = getPathForUri(uri);
        int modeBits = ParcelFileDescriptor.parseMode(mode);
        if (isInsideStorageVolume(path)) {
            return ParcelFileDescriptor.open(path.toFile(), modeBits);
        }
        // Allowing other apps to write to files that require root access is dangerous.
        // TODO: Relax this restriction for other cases?
        if (modeBits != ParcelFileDescriptor.MODE_READ_ONLY) {
            throw toFileNotFoundException(new AccessDeniedException(mode));
        }
        Set<OpenOption> options = modeToOpenOptions(modeBits);
        SeekableByteChannel channel;
        try {
            channel = Files.newByteChannel(path, options);
        } catch (IOException e) {
            throw toFileNotFoundException(e);
        }
        StorageManager storageManager = ContextCompat.getSystemService(getContext(),
                StorageManager.class);
        try {
            return StorageManagerCompat.openProxyFileDescriptor(storageManager, modeBits,
                    new ChannelCallback(channel), mCallbackHandler);
        } catch (IOException e) {
            throw toFileNotFoundException(e);
        }
    }

    private boolean isInsideStorageVolume(@NonNull Path path) {
        if (path.getFileSystem() != FileSystems.getDefault()) {
            return false;
        }
        StorageManager storageManager = ContextCompat.getSystemService(getContext(),
                StorageManager.class);
        List<StorageVolume> storageVolumes = StorageManagerCompat.getStorageVolumes(storageManager);
        return Functional.some(storageVolumes, storageVolume ->
                path.startsWith(Paths.get(StorageVolumeCompat.getPath(storageVolume))));
    }

    @NonNull
    private static Set<OpenOption> modeToOpenOptions(int mode) {
        // May be "r" for read-only access, "rw" for read and write access, or "rwt" for read and
        // write access that truncates any existing file.
        if ((mode & ParcelFileDescriptor.MODE_APPEND) == ParcelFileDescriptor.MODE_APPEND) {
            throw new IllegalArgumentException("mode " + mode);
        }
        Set<OpenOption> options = new HashSet<>();
        if ((mode & ParcelFileDescriptor.MODE_READ_ONLY) == ParcelFileDescriptor.MODE_READ_ONLY
                || (mode & ParcelFileDescriptor.MODE_READ_WRITE)
                == ParcelFileDescriptor.MODE_READ_WRITE) {
            options.add(StandardOpenOption.READ);
        }
        if ((mode & ParcelFileDescriptor.MODE_WRITE_ONLY) == ParcelFileDescriptor.MODE_WRITE_ONLY
                || (mode & ParcelFileDescriptor.MODE_READ_WRITE)
                == ParcelFileDescriptor.MODE_READ_WRITE) {
            options.add(StandardOpenOption.WRITE);
        }
        if ((mode & ParcelFileDescriptor.MODE_CREATE) == ParcelFileDescriptor.MODE_CREATE) {
            options.add(StandardOpenOption.CREATE);
        }
        if ((mode & ParcelFileDescriptor.MODE_TRUNCATE) == ParcelFileDescriptor.MODE_TRUNCATE) {
            options.add(StandardOpenOption.TRUNCATE_EXISTING);
        }
        return options;
    }

    @NonNull
    private static FileNotFoundException toFileNotFoundException(@NonNull IOException exception) {
        if (exception instanceof FileNotFoundException) {
            return (FileNotFoundException) exception;
        } else {
            FileNotFoundException fileNotFoundException = new FileNotFoundException(
                    exception.getMessage());
            fileNotFoundException.initCause(exception);
            return fileNotFoundException;
        }
    }

    @NonNull
    public static Uri getUriForPath(@NonNull Path path) {
        String uriPath = Uri.encode(path.toUri().toString());
        return new Uri.Builder()
                .scheme("content")
                .authority(BuildConfig.FILE_PROVIDIER_AUTHORITY)
                .path(uriPath)
                .build();
    }

    @NonNull
    public static Path getPathForUri(@NonNull Uri uri) {
        String uriPath = Uri.decode(uri.getPath());
        // Strip the prepended slash. A slash is always prepended because our Uri path starts with
        // our URI scheme, which can never start with a slash; but our Uri has an authority so its
        // path must start with a slash.
        uriPath = uriPath.substring(1);
        URI pathUri;
        try {
            pathUri = new URI(uriPath);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return Paths.get(pathUri);
    }

    private static class ChannelCallback extends ProxyFileDescriptorCallbackCompat {

        @NonNull
        private final SeekableByteChannel mChannel;

        private long mOffset;

        private boolean mReleased;

        ChannelCallback(@NonNull SeekableByteChannel channel) {
            mChannel = channel;
        }

        @Override
        public long onGetSize() throws ErrnoException {
            ensureNotReleased();
            try {
                return mChannel.size();
            } catch (IOException e) {
                throw toErrnoException(e);
            }
        }

        @Override
        public int onRead(long offset, int size, byte[] data) throws ErrnoException {
            ensureNotReleased();
            if (mOffset != offset) {
                try {
                    mChannel.position(offset);
                } catch (IOException e) {
                    throw toErrnoException(e);
                }
                mOffset = offset;
            }
            try {
                ByteBuffer buffer = ByteBuffer.wrap(data, 0, size);
                int readSize = mChannel.read(buffer);
                mOffset += readSize;
                return readSize;
            } catch (IOException e) {
                throw toErrnoException(e);
            }
        }

        @Override
        public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
            ensureNotReleased();
            if (mOffset != offset) {
                try {
                    mChannel.position(offset);
                } catch (IOException e) {
                    throw toErrnoException(e);
                }
                mOffset = offset;
            }
            try {
                ByteBuffer buffer = ByteBuffer.wrap(data, 0, size);
                int wroteSize = mChannel.write(buffer);
                mOffset += wroteSize;
                return wroteSize;
            } catch (IOException e) {
                throw toErrnoException(e);
            }
        }

        @Override
        public void onFsync() throws ErrnoException {
            ensureNotReleased();
            if (ForceableChannel.isForceable(mChannel)) {
                try {
                    ForceableChannel.force(mChannel, true);
                } catch (IOException e) {
                    throw toErrnoException(e);
                }
            }
        }

        private void ensureNotReleased() throws ErrnoException {
            if (mReleased) {
                throw new ErrnoException(null, OsConstants.EBADF);
            }
        }

        @NonNull
        private static ErrnoException toErrnoException(@NonNull IOException exception) {
            if (exception instanceof FileSystemException
                    && exception.getCause() instanceof SyscallException) {
                SyscallException syscallException = (SyscallException) exception.getCause();
                return new ErrnoException(syscallException.getFunctionName(),
                        syscallException.getErrno(), exception);
            } else {
                int errno;
                if (exception instanceof AccessDeniedException) {
                    errno = OsConstants.EPERM;
                } else if (exception instanceof FileSystemLoopException) {
                    errno = OsConstants.ELOOP;
                } else if (exception instanceof InvalidFileNameException) {
                    errno = OsConstants.EINVAL;
                } else if (exception instanceof IsDirectoryException) {
                    errno = OsConstants.EISDIR;
                } else if (exception instanceof NoSuchFileException) {
                    errno = OsConstants.ENOENT;
                } else {
                    errno = OsConstants.EIO;
                }
                return new ErrnoException(exception.getMessage(), errno, exception);
            }
        }

        @Override
        public void onRelease() {
            if (mReleased) {
                return;
            }
            try {
                mChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mReleased = true;
        }
    }
}
