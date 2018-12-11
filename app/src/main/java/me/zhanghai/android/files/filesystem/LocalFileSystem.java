/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filesystem;

import android.Manifest;
import android.os.Parcel;
import android.os.Parcelable;
import android.system.ErrnoException;
import android.system.OsConstants;

import org.threeten.bp.Instant;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;
import me.zhanghai.android.effortlesspermissions.EffortlessPermissions;
import me.zhanghai.android.files.AppApplication;
import me.zhanghai.android.files.functional.Functional;
import me.zhanghai.android.files.functional.FunctionalException;
import me.zhanghai.android.files.functional.throwing.ThrowingFunction;
import me.zhanghai.android.files.provider.linux.LinuxFileModeBit;

public class LocalFileSystem {

    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private LocalFileSystem() {}

    @NonNull
    @WorkerThread
    public static Information getInformation(@NonNull String path)
            throws FileSystemException {
        return getWithSyscallOrShellFs(path, Syscall::getInformation, ShellFs::getInformation);
    }

    @NonNull
    @WorkerThread
    public static List<Pair<String, Information>> getChildren(@NonNull String path)
            throws FileSystemException {
        return getWithSyscallOrShellFs(path, LocalFileSystem::getChildrenWithSyscall,
                ShellFs::getChildrenAndInformation);
    }

    @NonNull
    @WorkerThread
    private static List<Pair<String, Information>> getChildrenWithSyscall(
            @NonNull String path) throws FileSystemException {
        List<String> childNames = Syscall.getChildren(path);
        if (childNames == null) {
            // TODO: Correct way of throwing?
            throw new FileSystemException(new NullPointerException(
                    "Syscall.getChildren() returned null"));
        }
        List<Information> childInformations;
        try {
            childInformations = Functional.map(childNames, (ThrowingFunction<String,
                    Information>) childName -> Syscall.getInformation(LocalFile.joinPaths(
                    path, childName)));
        } catch (FunctionalException e) {
            throw e.getCauseAs(FileSystemException.class);
        }
        return Functional.map(childNames, (childName, index) -> new Pair<>(childName,
                childInformations.get(index)));
    }

    @NonNull
    private static <T> T getWithSyscallOrShellFs(@NonNull String path,
                                                 @NonNull GetValueFromPath<T> getWithSyscall,
                                                 @NonNull GetValueFromPath<T> getWithShellFs)
            throws FileSystemException {
        try {
            return getWithSyscall.get(path);
        } catch (FileSystemException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ErrnoException) {
                ErrnoException errnoException = (ErrnoException) cause;
                if (errnoException.errno == OsConstants.EACCES) {
                    if (EffortlessPermissions.hasPermissions(AppApplication.getInstance(),
                            PERMISSIONS_STORAGE)) {
                        return getWithShellFs.get(path);
                    }
                }
            }
            throw e;
        }
    }

    public static class Information implements Parcelable {

        public final boolean isSymbolicLinkStat;
        @NonNull
        public final PosixFileType type;
        @NonNull
        public final EnumSet<LinuxFileModeBit> mode;
        @NonNull
        public final PosixUser owner;
        @NonNull
        public final PosixGroup group;
        public final long size;
        @NonNull
        public final Instant lastModificationTime;
        public final boolean isSymbolicLink;
        @Nullable
        public final String symbolicLinkTarget;

        public Information(boolean isSymbolicLinkStat, @NonNull PosixFileType type,
                           @NonNull EnumSet<LinuxFileModeBit> mode, @NonNull PosixUser owner,
                           @NonNull PosixGroup group, long size,
                           @NonNull Instant lastModificationTime, boolean isSymbolicLink,
                           @Nullable String symbolicLinkTarget) {
            this.isSymbolicLinkStat = isSymbolicLinkStat;
            this.type = type;
            this.mode = mode;
            this.owner = owner;
            this.group = group;
            this.size = size;
            this.lastModificationTime = lastModificationTime;
            this.isSymbolicLink = isSymbolicLink;
            this.symbolicLinkTarget = symbolicLinkTarget;
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            Information that = (Information) object;
            return isSymbolicLinkStat == that.isSymbolicLinkStat
                    && size == that.size
                    && isSymbolicLink == that.isSymbolicLink
                    && type == that.type
                    && Objects.equals(mode, that.mode)
                    && Objects.equals(owner, that.owner)
                    && Objects.equals(group, that.group)
                    && Objects.equals(lastModificationTime, that.lastModificationTime)
                    && Objects.equals(symbolicLinkTarget, that.symbolicLinkTarget);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isSymbolicLinkStat, type, mode, owner, group, size,
                    lastModificationTime, isSymbolicLink, symbolicLinkTarget);
        }

        public static final Creator<Information> CREATOR = new Creator<Information>() {
            @NonNull
            @Override
            public Information createFromParcel(@NonNull Parcel source) {
                return new Information(source);
            }
            @NonNull
            @Override
            public Information[] newArray(int size) {
                return new Information[size];
            }
        };

        protected Information(@NonNull Parcel in) {
            isSymbolicLinkStat = in.readByte() != 0;
            int tmpType = in.readInt();
            type = tmpType == -1 ? null : PosixFileType.values()[tmpType];
            //noinspection unchecked
            mode = (EnumSet<LinuxFileModeBit>) in.readSerializable();
            owner = in.readParcelable(PosixUser.class.getClassLoader());
            group = in.readParcelable(PosixGroup.class.getClassLoader());
            size = in.readLong();
            lastModificationTime = (Instant) in.readSerializable();
            isSymbolicLink = in.readByte() != 0;
            symbolicLinkTarget = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeByte(isSymbolicLinkStat ? (byte) 1 : (byte) 0);
            dest.writeInt(type == null ? -1 : type.ordinal());
            dest.writeSerializable(mode);
            dest.writeParcelable(owner, flags);
            dest.writeParcelable(group, flags);
            dest.writeLong(size);
            dest.writeSerializable(lastModificationTime);
            dest.writeByte(isSymbolicLink ? (byte) 1 : (byte) 0);
            dest.writeString(symbolicLinkTarget);
        }
    }

    private interface GetValueFromPath<T> {
        @NonNull
        T get(@NonNull String path) throws FileSystemException;
    }
}
