/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Parcel;
import android.os.Parcelable;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;

import org.threeten.bp.Instant;

import java.io.File;
import java.io.FileDescriptor;
import java.util.EnumSet;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.compat.LongConsumer;
import me.zhanghai.android.materialfilemanager.jni.Linux;
import me.zhanghai.android.materialfilemanager.jni.StructGroup;
import me.zhanghai.android.materialfilemanager.jni.StructPasswd;
import me.zhanghai.android.materialfilemanager.jni.StructStatCompat;
import me.zhanghai.android.materialfilemanager.jni.StructTimespecCompat;
import me.zhanghai.android.materialfilemanager.util.ExceptionUtils;
import me.zhanghai.android.materialfilemanager.util.MoreTextUtils;

public class Syscall {

    public static Information loadInformation(String path) throws FileSystemException {
        StructStatCompat stat;
        try {
            stat = Linux.lstat(path);
        } catch (ErrnoException e) {
            throw new FileSystemException(R.string.file_error_information, e);
        }
        if (stat == null) {
            throw new FileSystemException(R.string.file_error_information);
        }
        Information information = new Information();
        boolean isSymbolicLink = OsConstants.S_ISLNK(stat.st_mode);
        if (isSymbolicLink) {
            try {
                information.symbolicLinkTarget = Os.readlink(path);
            } catch (ErrnoException e) {
                throw new FileSystemException(R.string.file_error_information, e);
            }
            try {
                StructStatCompat symbolicLinkStat = Linux.stat(path);
                if (symbolicLinkStat != null) {
                    stat = symbolicLinkStat;
                    information.isSymbolicLinkStat = true;
                }
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
        }
        information.type = parseType(stat.st_mode);
        information.mode = parseMode(stat.st_mode);
        information.owner = new PosixUser();
        information.owner.id = stat.st_uid;
        information.group = new PosixGroup();
        information.group.id = stat.st_gid;
        information.size = stat.st_size;
        information.lastModificationTime = Instant.ofEpochSecond(stat.st_mtim.tv_sec,
                stat.st_mtim.tv_nsec);
        try {
            StructPasswd passwd = Linux.getpwuid(information.owner.id);
            if (passwd != null) {
                information.owner.name = passwd.pw_name;
            }
        } catch (ErrnoException e) {
            // It's valid to have a file with a non-existent owner.
            e.printStackTrace();
        }
        try {
            StructGroup group = Linux.getgrgid(information.group.id);
            if (group != null) {
                information.group.name = group.gr_name;
            }
        } catch (ErrnoException e) {
            // It's valid to have a file with a non-existent group.
            e.printStackTrace();
        }
        return information;
    }

    static PosixFileType parseType(int st_mode) {
        return OsConstants.S_ISDIR(st_mode) ? PosixFileType.DIRECTORY
                : OsConstants.S_ISCHR(st_mode) ? PosixFileType.CHARACTER_DEVICE
                : OsConstants.S_ISBLK(st_mode) ? PosixFileType.BLOCK_DEVICE
                : OsConstants.S_ISREG(st_mode) ? PosixFileType.REGULAR_FILE
                : OsConstants.S_ISFIFO(st_mode) ? PosixFileType.FIFO
                : OsConstants.S_ISLNK(st_mode) ? PosixFileType.SYMBOLIC_LINK
                : OsConstants.S_ISSOCK(st_mode) ? PosixFileType.SOCKET
                : PosixFileType.UNKNOWN;
    }

    static EnumSet<PosixFileModeBit> parseMode(int st_mode) {
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

    public static void copy(String fromPath, String toPath, long notifyByteCount,
                            LongConsumer listener) throws FileSystemException,
            InterruptedException {
        copy(fromPath, toPath, false, notifyByteCount, listener);
    }

    /*
     * @see android.os.FileUtils#copy(java.io.FileDescriptor, java.io.FileDescriptor,
     *      android.os.FileUtils.ProgressListener, android.os.CancellationSignal, long)
     * @see https://github.com/gnome/glib/blob/master/gio/gfile.c g_file_copy()
     */
    private static void copy(String fromPath, String toPath, boolean forMove, long notifyByteCount,
                             LongConsumer listener) throws FileSystemException,
            InterruptedException {
        StructStatCompat fromStat;
        try {
            fromStat = Linux.lstat(fromPath);
            if (fromStat == null) {
                throw new FileSystemException(R.string.file_copy_error);
            }
            if (OsConstants.S_ISREG(fromStat.st_mode)) {
                FileDescriptor fromFd = Os.open(fromPath, OsConstants.O_RDONLY, 0);
                try {
                    FileDescriptor toFd = Os_creat(toPath, fromStat.st_mode);
                    try {
                        long copiedByteCount = 0;
                        long unnotifiedByteCount = 0;
                        try {
                            long sentByteCount;
                            while ((sentByteCount = Linux.sendfile(toFd, fromFd, null,
                                    notifyByteCount)) != 0) {
                                copiedByteCount += sentByteCount;
                                unnotifiedByteCount += sentByteCount;
                                if (unnotifiedByteCount >= notifyByteCount) {
                                    if (listener != null) {
                                        listener.accept(copiedByteCount);
                                    }
                                    unnotifiedByteCount = 0;
                                }
                                ExceptionUtils.throwIfInterrupted();
                            }
                        } finally {
                            if (unnotifiedByteCount > 0 && listener != null) {
                                listener.accept(copiedByteCount);
                            }
                        }
                    } finally {
                        Os.close(toFd);
                    }
                } finally {
                    Os.close(fromFd);
                }
            } else if (OsConstants.S_ISDIR(fromStat.st_mode)) {
                Os.mkdir(toPath, fromStat.st_mode);
            } else if (OsConstants.S_ISLNK(fromStat.st_mode)) {
                String target = Os.readlink(fromPath);
                Os.symlink(target, toPath);
            } else {
                throw new FileSystemException(R.string.file_copy_error_special_file);
            }
        } catch (ErrnoException e) {
            throw new FileSystemException(R.string.file_copy_error, e);
        }
        // We don't take error when copying attribute fatal, so errors will only be logged from now
        // on.
        // Ownership should be copied before permissions so that special permission bits like
        // setuid work properly.
        try {
            if (forMove) {
                Os.lchown(toPath, fromStat.st_uid, fromStat.st_gid);
            }
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
        try {
            if (!OsConstants.S_ISLNK(fromStat.st_mode)) {
                Os.chmod(toPath, fromStat.st_mode);
            }
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
        try {
            StructTimespecCompat[] times = {
                    forMove ? fromStat.st_atim : new StructTimespecCompat(0, Linux.UTIME_OMIT),
                    fromStat.st_mtim
            };
            Linux.lutimens(toPath, times);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
        try {
            // TODO: Allow u+rw temporarily if we are to copy xattrs.
            String[] xattrNames = Linux.llistxattr(fromPath);
            if (xattrNames != null) {
                for (String xattrName : xattrNames) {
                    if (!(forMove || MoreTextUtils.startsWith(xattrName, "user."))) {
                        continue;
                    }
                    byte[] xattrValue = Linux.lgetxattr(fromPath, xattrName);
                    if (xattrValue != null) {
                        Linux.lsetxattr(fromPath, xattrName, xattrValue, 0);
                    }
                }
            }
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
        // TODO: SELinux?
    }

    public static void delete(String path) throws FileSystemException {
        try {
            Os.remove(path);
        } catch (ErrnoException e) {
            throw new FileSystemException(R.string.file_delete_error, e);
        }
    }

    public static void move(String fromPath, String toPath, long notifyByteCount,
                            LongConsumer listener) throws FileSystemException,
            InterruptedException {
        try {
            Os.rename(fromPath, toPath);
        } catch (ErrnoException e) {
            copy(fromPath, toPath, true, notifyByteCount, listener);
            delete(fromPath);
        }
    }

    public static void rename(String fromPath, String toPath) throws FileSystemException {
        try {
            Os.rename(fromPath, toPath);
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

        public boolean isSymbolicLinkStat;
        public PosixFileType type;
        public EnumSet<PosixFileModeBit> mode;
        public PosixUser owner;
        public PosixGroup group;
        public long size;
        public Instant lastModificationTime;
        public boolean isSymbolicLink;
        public String symbolicLinkTarget;


        @Override
        public boolean equals(Object object) {
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
            isSymbolicLinkStat = in.readByte() != 0;
            int tmpType = in.readInt();
            type = tmpType == -1 ? null : PosixFileType.values()[tmpType];
            //noinspection unchecked
            mode = (EnumSet<PosixFileModeBit>) in.readSerializable();
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
        public void writeToParcel(Parcel dest, int flags) {
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
}
