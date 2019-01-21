package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.common.ParcelableFileTime;
import me.zhanghai.android.files.provider.common.ParcelablePosixFileMode;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.android.files.provider.remote.ParcelableFileAttributes;
import me.zhanghai.android.files.provider.remote.ParcelableIoException;

interface IRemotePosixFileAttributeView {

    ParcelableFileAttributes readAttributes(out ParcelableIoException ioException);

    void setTimes(in ParcelableFileTime parcelableLastModifiedTime,
            in ParcelableFileTime parcelableLastAccessTime,
            in ParcelableFileTime parcelableCreateTime, out ParcelableIoException ioException);

    void setOwner(in PosixUser owner, out ParcelableIoException ioException);

    void setGroup(in PosixGroup group, out ParcelableIoException ioException);

    void setMode(in ParcelablePosixFileMode parcelableMode, out ParcelableIoException ioException);
}
