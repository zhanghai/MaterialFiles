/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;

import org.threeten.bp.Instant;

import java.io.File;
import java.io.FileDescriptor;
import java.util.EnumSet;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.R;

public class AndroidOs {

    public static Information loadInformation(String path) throws FileSystemException {
        return loadInformation(path, false);
    }

    private static Information loadInformation(String path, boolean followSymbolicLinks)
            throws FileSystemException {
        StructStat stat;
        try {
            stat = followSymbolicLinks ? Os.stat(path) : Os.lstat(path);
        } catch (ErrnoException e) {
            throw new FileSystemException(R.string.file_error_information, e);
        }
        Information information = new Information();
        information.containingDeviceId = stat.st_dev;
        information.inodeNumber = stat.st_ino;
        information.type = parseType(stat.st_mode);
        if (information.type == PosixFileType.SYMBOLIC_LINK) {
            try {
                information.symbolicLinkPath = Os.readlink(path);
            } catch (ErrnoException e) {
                throw new FileSystemException(R.string.file_error_information, e);
            }
            try {
                information.symbolicLinkStatInformation = loadInformation(path, true);
            } catch (FileSystemException e) {
                e.printStackTrace();
                Throwable cause = e.getCause();
                if (cause instanceof ErrnoException) {
                    ErrnoException errnoException = (ErrnoException) cause;
                    information.symbolicLinkStatErrno = errnoException.errno;
                }
            }
        }
        information.mode = parseMode(stat.st_mode);
        information.linkCount = stat.st_nlink;
        information.userId = stat.st_uid;
        information.groupId = stat.st_gid;
        information.deviceId = stat.st_rdev;
        information.size = stat.st_size;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            information.lastAccessTime = Instant.ofEpochSecond(stat.st_atim.tv_sec,
                    stat.st_atim.tv_nsec);
            information.lastModificationTime = Instant.ofEpochSecond(stat.st_mtim.tv_sec,
                    stat.st_mtim.tv_nsec);
            information.lastStatusChangeTime = Instant.ofEpochSecond(stat.st_ctim.tv_sec,
                    stat.st_ctim.tv_nsec);
        } else {
            information.lastAccessTime = Instant.ofEpochSecond(stat.st_atime);
            information.lastModificationTime = Instant.ofEpochSecond(stat.st_mtime);
            information.lastStatusChangeTime = Instant.ofEpochSecond(stat.st_ctime);
        }
        information.preferredIoBlockSize = stat.st_blksize;
        information.allocatedBlockCount = stat.st_blocks;
        return information;
    }

    private static PosixFileType parseType(int st_mode) {
        return OsConstants.S_ISDIR(st_mode) ? PosixFileType.DIRECTORY
                : OsConstants.S_ISCHR(st_mode) ? PosixFileType.CHARACTER_DEVICE
                : OsConstants.S_ISBLK(st_mode) ? PosixFileType.BLOCK_DEVICE
                : OsConstants.S_ISREG(st_mode) ? PosixFileType.REGULAR_FILE
                : OsConstants.S_ISFIFO(st_mode) ? PosixFileType.FIFO
                : OsConstants.S_ISLNK(st_mode) ? PosixFileType.SYMBOLIC_LINK
                : OsConstants.S_ISSOCK(st_mode) ? PosixFileType.SOCKET
                : PosixFileType.UNKNOWN;
    }

    private static EnumSet<PosixFileModeBit> parseMode(int st_mode) {
        EnumSet<PosixFileModeBit> mode = EnumSet.noneOf(PosixFileModeBit.class);
        if ((st_mode & OsConstants.S_ISUID) != 0) {
            mode.add(PosixFileModeBit.SET_USER_ID);
        }
        if ((st_mode & OsConstants.S_ISGID) != 0) {
            mode.add(PosixFileModeBit.SET_GROUP_ID);
        }
        if ((st_mode & OsConstants.S_ISVTX) != 0) {
            mode.add(PosixFileModeBit.STICKY);
        }
        if ((st_mode & OsConstants.S_IRUSR) != 0) {
            mode.add(PosixFileModeBit.OWNER_READ);
        }
        if ((st_mode & OsConstants.S_IWUSR) != 0) {
            mode.add(PosixFileModeBit.OWNER_WRITE);
        }
        if ((st_mode & OsConstants.S_IXUSR) != 0) {
            mode.add(PosixFileModeBit.OWNER_EXECUTE);
        }
        if ((st_mode & OsConstants.S_IRGRP) != 0) {
            mode.add(PosixFileModeBit.GROUP_READ);
        }
        if ((st_mode & OsConstants.S_IWGRP) != 0) {
            mode.add(PosixFileModeBit.GROUP_WRITE);
        }
        if ((st_mode & OsConstants.S_IXGRP) != 0) {
            mode.add(PosixFileModeBit.GROUP_EXECUTE);
        }
        if ((st_mode & OsConstants.S_IROTH) != 0) {
            mode.add(PosixFileModeBit.OTHERS_READ);
        }
        if ((st_mode & OsConstants.S_IWOTH) != 0) {
            mode.add(PosixFileModeBit.OTHERS_WRITE);
        }
        if ((st_mode & OsConstants.S_IXOTH) != 0) {
            mode.add(PosixFileModeBit.OTHERS_EXECUTE);
        }
        return mode;
    }

    public static void delete(String path) throws FileSystemException {
        try {
            Os.remove(path);
        } catch (ErrnoException e) {
            throw new FileSystemException(R.string.file_delete_error, e);
        }
    }

    public static void move(String path, String newPath) throws FileSystemException {
        try {
            Os.rename(path, newPath);
        } catch (ErrnoException e) {
            throw new FileSystemException(R.string.file_move_error, e);
        }
    }

    public static void rename(String path, String newName) throws FileSystemException {
        String newPath = new File(new File(path).getParent(), newName).getPath();
        try {
            Os.rename(path, newPath);
        } catch (ErrnoException e) {
            throw new FileSystemException(R.string.file_rename_error, e);
        }
    }

    public static void createFile(String path) throws FileSystemException {
        try {
            FileDescriptor fd = Os_creat(path, OsConstants.S_IRUSR | OsConstants.S_IWUSR
                    | OsConstants.S_IRGRP | OsConstants.S_IWGRP | OsConstants.S_IROTH
                    | OsConstants.S_IWOTH);
            Os.close(fd);
        } catch (ErrnoException e) {
            throw new FileSystemException(R.string.file_create_file_error, e);
        }
    }

    private static FileDescriptor Os_creat(String path, int mode) throws ErrnoException {
        return Os.open(path, OsConstants.O_WRONLY | OsConstants.O_CREAT | OsConstants.O_TRUNC,
                mode);
    }

    public static void createDirectory(String path) throws FileSystemException {
        try {
            Os.mkdir(path, OsConstants.S_IRWXU | OsConstants.S_IRWXG | OsConstants.S_IRWXO);
        } catch (ErrnoException e) {
            throw new FileSystemException(R.string.file_create_directory_error, e);
        }
    }

    public static class Information implements Parcelable {

        public long containingDeviceId;
        public long inodeNumber;
        public PosixFileType type;
        public String symbolicLinkPath;
        public Information symbolicLinkStatInformation;
        public int symbolicLinkStatErrno;
        public EnumSet<PosixFileModeBit> mode;
        public long linkCount;
        public long userId;
        public long groupId;
        public long deviceId;
        public long size;
        public Instant lastAccessTime;
        public Instant lastModificationTime;
        public Instant lastStatusChangeTime;
        public long preferredIoBlockSize;
        public long allocatedBlockCount;


        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            Information that = (Information) object;
            return containingDeviceId == that.containingDeviceId
                    && inodeNumber == that.inodeNumber
                    && symbolicLinkStatErrno == that.symbolicLinkStatErrno
                    && linkCount == that.linkCount
                    && userId == that.userId
                    && groupId == that.groupId
                    && deviceId == that.deviceId
                    && size == that.size
                    && preferredIoBlockSize == that.preferredIoBlockSize
                    && allocatedBlockCount == that.allocatedBlockCount
                    && type == that.type
                    && Objects.equals(symbolicLinkPath, that.symbolicLinkPath)
                    && Objects.equals(symbolicLinkStatInformation, that.symbolicLinkStatInformation)
                    && Objects.equals(mode, that.mode)
                    && Objects.equals(lastAccessTime, that.lastAccessTime)
                    && Objects.equals(lastModificationTime, that.lastModificationTime)
                    && Objects.equals(lastStatusChangeTime, that.lastStatusChangeTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(containingDeviceId, inodeNumber, type, symbolicLinkPath,
                    symbolicLinkStatInformation, symbolicLinkStatErrno, mode, linkCount, userId,
                    groupId, deviceId, size, lastAccessTime, lastModificationTime,
                    lastStatusChangeTime, preferredIoBlockSize, allocatedBlockCount);
        }


        public static final Creator<Information> CREATOR = new Creator<Information>() {
            @Override
            public Information createFromParcel(Parcel source) {
                return new Information(source);
            }
            @Override
            public Information[] newArray(int size) {
                return new Information[size];
            }
        };

        public Information() {}

        protected Information(Parcel in) {
            containingDeviceId = in.readLong();
            inodeNumber = in.readLong();
            int tmpType = in.readInt();
            type = tmpType == -1 ? null : PosixFileType.values()[tmpType];
            symbolicLinkPath = in.readString();
            symbolicLinkStatInformation = in.readParcelable(Information.class.getClassLoader());
            symbolicLinkStatErrno = in.readInt();
            //noinspection unchecked
            mode = (EnumSet<PosixFileModeBit>) in.readSerializable();
            linkCount = in.readLong();
            userId = in.readLong();
            groupId = in.readLong();
            deviceId = in.readLong();
            size = in.readLong();
            lastAccessTime = (Instant) in.readSerializable();
            lastModificationTime = (Instant) in.readSerializable();
            lastStatusChangeTime = (Instant) in.readSerializable();
            preferredIoBlockSize = in.readLong();
            allocatedBlockCount = in.readLong();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(containingDeviceId);
            dest.writeLong(inodeNumber);
            dest.writeInt(type == null ? -1 : type.ordinal());
            dest.writeString(symbolicLinkPath);
            dest.writeParcelable(symbolicLinkStatInformation, flags);
            dest.writeInt(symbolicLinkStatErrno);
            dest.writeSerializable(mode);
            dest.writeLong(linkCount);
            dest.writeLong(userId);
            dest.writeLong(groupId);
            dest.writeLong(deviceId);
            dest.writeLong(size);
            dest.writeSerializable(lastAccessTime);
            dest.writeSerializable(lastModificationTime);
            dest.writeSerializable(lastStatusChangeTime);
            dest.writeLong(preferredIoBlockSize);
            dest.writeLong(allocatedBlockCount);
        }
    }
}
