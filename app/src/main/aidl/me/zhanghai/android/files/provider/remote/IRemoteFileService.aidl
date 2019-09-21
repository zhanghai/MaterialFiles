package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.remote.IRemoteFileSystem;
import me.zhanghai.android.files.provider.remote.IRemoteFileSystemProvider;
import me.zhanghai.android.files.provider.remote.IRemotePosixFileAttributeView;
import me.zhanghai.android.files.provider.remote.IRemotePosixFileStore;
import me.zhanghai.android.files.provider.remote.ParcelableObject;

interface IRemoteFileService {

    IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(String scheme);

    IRemoteFileSystem getRemoteFileSystemInterface(in ParcelableObject parcelableFileSystem);

    IRemotePosixFileStore getRemotePosixFileStoreInterface(in ParcelableObject parcelableFileStore);

    IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
            in ParcelableObject parcelableAttributeView);

    void refreshArchiveFileSystem(in ParcelableObject parcelableFileSystem);
}
