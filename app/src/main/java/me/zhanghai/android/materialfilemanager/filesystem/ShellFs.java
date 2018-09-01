/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.AppApplication;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.FunctionalException;
import me.zhanghai.android.materialfilemanager.functional.throwing.ThrowingFunction;
import me.zhanghai.android.materialfilemanager.shell.SuShell;
import me.zhanghai.android.materialfilemanager.util.Holder;

public class ShellFs {

    public static Information loadInformation(String path) throws FileSystemException {
        String command = ShellEscaper.escape(getFsPath()) + " -f " + ShellEscaper.escape(path);
        Holder<String> outputHolder = new Holder<>();
        int exitCode = SuShell.run(command, outputHolder, null);
        if (exitCode != 0) {
            throw new FileSystemException(R.string.file_error_information);
        }
        String output = outputHolder.value;
        Information information = parseInformation(output);
        return information;
    }

    public static List<Pair<String, Information>> getChildrenAndInformation(String path)
            throws FileSystemException {
        String command = ShellEscaper.escape(getFsPath()) + " -d " + ShellEscaper.escape(path);
        Holder<String> outputHolder = new Holder<>();
        int exitCode = SuShell.run(command, outputHolder, null);
        if (exitCode != 0) {
            throw new FileSystemException(R.string.file_list_error_directory);
        }
        String output = outputHolder.value;
        if (output.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> childOutputs = Arrays.asList(output.split("\0\0\0\n\0\0\0"));
        List<Pair<String, Information>> children;
        try {
            children = Functional.map(childOutputs,
                    (ThrowingFunction<String, Pair<String, Information>>) childStat -> {
                        String[] childNameAndFile = childStat.split("\0", 2);
                        String childName;
                        String childFile;
                        switch (childNameAndFile.length) {
                            case 1:
                                childName = childNameAndFile[0];
                                childFile = null;
                                break;
                            case 2:
                                childName = childNameAndFile[0];
                                childFile = childNameAndFile[1];
                                break;
                            default:
                                throw new FileSystemException(R.string.file_error_information);
                        }
                        Information childInformation = !TextUtils.isEmpty(childFile) ?
                                parseInformation(childFile) : null;
                        return new Pair<>(childName, childInformation);
                    });
        } catch (FunctionalException e) {
            throw e.getCauseAs(FileSystemException.class);
        }
        return children;
    }

    private static String getFsPath() {
        return AppApplication.getInstance().getApplicationInfo().nativeLibraryDir + "/libfs.so";
    }

    private static Information parseInformation(String output) throws FileSystemException {
        Information information = new Information();
        String[] fields = output.split("\0");
        if (fields.length < 6) {
            throw new FileSystemException(R.string.file_error_information);
        }
        try {
            int mode = Integer.parseInt(fields[0]);
            information.type = Syscall.parseType(mode);
            information.mode = Syscall.parseMode(mode);
            information.owner = new PosixUser();
            information.owner.id = Integer.parseInt(fields[1]);
            information.group = new PosixGroup();
            information.group.id = Integer.parseInt(fields[2]);
            information.size = Long.parseLong(fields[3]);
            information.lastModificationTime = Instant.ofEpochSecond(Long.parseLong(fields[4]),
                    Long.parseLong(fields[5]));
            int index = 6;
            if (index >= fields.length) {
                throw new IllegalArgumentException();
            }
            information.isSymbolicLink = Integer.parseInt(fields[index]) == 1;
            ++index;
            if (information.isSymbolicLink) {
                if (index >= fields.length) {
                    throw new IllegalArgumentException();
                }
                information.symbolicLinkTarget = fields[index];
                ++index;
            }
            if (index >= fields.length) {
                throw new IllegalArgumentException();
            }
            boolean hasOwnerName = Integer.parseInt(fields[index]) == 1;
            ++index;
            if (hasOwnerName) {
                if (index >= fields.length) {
                    throw new IllegalArgumentException();
                }
                information.owner.name = fields[index];
                ++index;
            }
            if (index >= fields.length) {
                throw new IllegalArgumentException();
            }
            boolean hasGroupName = Integer.parseInt(fields[index]) == 1;
            ++index;
            if (hasGroupName) {
                if (index >= fields.length) {
                    throw new IllegalArgumentException();
                }
                information.group.name = fields[index];
                ++index;
            }
            if (index != fields.length) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new FileSystemException(R.string.file_error_information, e);
        }
        return information;
    }

    public static class Information implements Parcelable {

        public PosixFileType type;
        public EnumSet<PosixFileModeBit> mode;
        public PosixUser owner;
        public PosixGroup group;
        public long size;
        public Instant lastAccessTime;
        public Instant lastModificationTime;
        public Instant lastStatusChangeTime;
        public boolean isSymbolicLink;
        public String symbolicLinkTarget;
        public Information symbolicLinkStatLInformation;


        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            Information that = (Information) object;
            return size == that.size
                    && isSymbolicLink == that.isSymbolicLink
                    && type == that.type
                    && Objects.equals(mode, that.mode)
                    && Objects.equals(owner, that.owner)
                    && Objects.equals(group, that.group)
                    && Objects.equals(lastAccessTime, that.lastAccessTime)
                    && Objects.equals(lastModificationTime, that.lastModificationTime)
                    && Objects.equals(lastStatusChangeTime, that.lastStatusChangeTime)
                    && Objects.equals(symbolicLinkTarget, that.symbolicLinkTarget)
                    && Objects.equals(symbolicLinkStatLInformation,
                    that.symbolicLinkStatLInformation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, mode, owner, group, size, lastAccessTime,
                    lastModificationTime, lastStatusChangeTime, isSymbolicLink, symbolicLinkTarget,
                    symbolicLinkStatLInformation);
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
            int tmpType = in.readInt();
            type = tmpType == -1 ? null : PosixFileType.values()[tmpType];
            //noinspection unchecked
            mode = (EnumSet<PosixFileModeBit>) in.readSerializable();
            owner = in.readParcelable(PosixUser.class.getClassLoader());
            group = in.readParcelable(PosixGroup.class.getClassLoader());
            size = in.readLong();
            lastAccessTime = (Instant) in.readSerializable();
            lastModificationTime = (Instant) in.readSerializable();
            lastStatusChangeTime = (Instant) in.readSerializable();
            isSymbolicLink = in.readByte() != 0;
            symbolicLinkTarget = in.readString();
            symbolicLinkStatLInformation = in.readParcelable(Information.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type == null ? -1 : type.ordinal());
            dest.writeSerializable(mode);
            dest.writeParcelable(owner, flags);
            dest.writeParcelable(group, flags);
            dest.writeLong(size);
            dest.writeSerializable(lastAccessTime);
            dest.writeSerializable(lastModificationTime);
            dest.writeSerializable(lastStatusChangeTime);
            dest.writeByte(isSymbolicLink ? (byte) 1 : (byte) 0);
            dest.writeString(symbolicLinkTarget);
            dest.writeParcelable(symbolicLinkStatLInformation, flags);
        }
    }
}
