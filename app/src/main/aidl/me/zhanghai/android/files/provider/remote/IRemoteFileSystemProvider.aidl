package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.remote.ParcelableAccessModes;
import me.zhanghai.android.files.provider.remote.ParcelableClass;
import me.zhanghai.android.files.provider.remote.ParcelableDirectoryStream;
import me.zhanghai.android.files.provider.remote.ParcelableDirectoryStreamFilter;
import me.zhanghai.android.files.provider.remote.ParcelableFileAttributes;
import me.zhanghai.android.files.provider.remote.ParcelableIoException;
import me.zhanghai.android.files.provider.remote.ParcelableLinkOptions;
import me.zhanghai.android.files.provider.remote.ParcelablePath;
import me.zhanghai.android.files.provider.remote.ParcelableRemoteFileStore;

interface IRemoteFileSystemProvider {

    ParcelableDirectoryStream newDirectoryStream(in ParcelablePath remoteDirectory,
            in ParcelableDirectoryStreamFilter parcelableFilter,
            out ParcelableIoException ioException);

    ParcelablePath readSymbolicLink(in ParcelablePath remoteLink,
            out ParcelableIoException ioException);

    boolean isSameFile(in ParcelablePath remotePath, in ParcelablePath remotePath2,
            out ParcelableIoException ioException);

    boolean isHidden(in ParcelablePath remotePath, out ParcelableIoException ioException);

    ParcelableRemoteFileStore getFileStore(in ParcelablePath remotePath,
            out ParcelableIoException ioException);

    void checkAccess(in ParcelablePath remotePath, in ParcelableAccessModes parcelableModes,
            out ParcelableIoException ioException);

    ParcelableFileAttributes readAttributes(in ParcelablePath remotePath,
            in ParcelableClass parcelableType, in ParcelableLinkOptions parcelableOptions,
            out ParcelableIoException ioException);
}
