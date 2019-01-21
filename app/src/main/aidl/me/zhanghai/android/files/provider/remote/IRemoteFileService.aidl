package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.remote.IRemoteFileSystem;
import me.zhanghai.android.files.provider.remote.IRemoteFileSystemProvider;
import me.zhanghai.android.files.provider.remote.IRemotePosixFileAttributeView;
import me.zhanghai.android.files.provider.remote.ParcelableFileAttributeView;
import me.zhanghai.android.files.provider.remote.ParcelableFileSystem;

interface IRemoteFileService {

    IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(String remoteScheme);

    IRemoteFileSystem getRemoteFileSystemInterface(in ParcelableFileSystem remoteFileSystem);

    IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
            in ParcelableFileAttributeView remoteAttributeView);

    void refreshArchiveFileSystem(in ParcelableFileSystem remoteFileSystem);
}
