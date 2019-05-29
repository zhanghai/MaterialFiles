package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.common.ParcelableFileTime;
import me.zhanghai.android.files.provider.common.ParcelablePosixFileMode;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.android.files.provider.remote.ParcelableException;
import me.zhanghai.android.files.provider.remote.ParcelableObject;

interface IRemotePosixFileAttributeView {

    ParcelableObject readAttributes(out ParcelableException exception);

    void setTimes(in ParcelableFileTime parcelableLastModifiedTime,
            in ParcelableFileTime parcelableLastAccessTime,
            in ParcelableFileTime parcelableCreateTime, out ParcelableException exception);

    void setOwner(in PosixUser owner, out ParcelableException exception);

    void setGroup(in PosixGroup group, out ParcelableException exception);

    void setMode(in ParcelablePosixFileMode parcelableMode, out ParcelableException exception);

    void setSeLinuxContext(in ParcelableObject parcelableContext,
            out ParcelableException exception);

    void restoreSeLinuxContext(out ParcelableException exception);
}
