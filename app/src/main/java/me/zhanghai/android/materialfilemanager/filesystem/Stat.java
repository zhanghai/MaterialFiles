/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Parcel;
import android.os.Parcelable;

import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.MapBuilder;
import me.zhanghai.android.materialfilemanager.util.ObjectUtils;
import me.zhanghai.android.materialfilemanager.util.StringCompat;

public class Stat {

    private static final Map<Character, PosixFileType> sCharToTypeMap =
            MapBuilder.<Character, PosixFileType>newHashMap()
                    .put('d', PosixFileType.DIRECTORY)
                    .put('c', PosixFileType.CHARACTER_DEVICE)
                    .put('b', PosixFileType.BLOCK_DEVICE)
                    .put('-', PosixFileType.REGULAR_FILE)
                    .put('p', PosixFileType.FIFO)
                    .put('l', PosixFileType.SYMBOLIC_LINK)
                    .put('s', PosixFileType.SOCKET)
                    .buildUnmodifiable();

    // FIXME: We've expanded three bits.
    private static final Map<PosixFileModeBit, Character> sPermissionToCharMap =
            MapBuilder.<PosixFileModeBit, Character>newHashMap()
                    .put(PosixFileModeBit.OWNER_READ, 'r')
                    .put(PosixFileModeBit.OWNER_WRITE, 'w')
                    .put(PosixFileModeBit.OWNER_EXECUTE, 'x')
                    .put(PosixFileModeBit.GROUP_READ, 'r')
                    .put(PosixFileModeBit.GROUP_WRITE, 'w')
                    .put(PosixFileModeBit.GROUP_EXECUTE, 'x')
                    .put(PosixFileModeBit.OTHERS_READ, 'r')
                    .put(PosixFileModeBit.OTHERS_WRITE, 'w')
                    .put(PosixFileModeBit.OTHERS_EXECUTE, 'x')
                    .buildUnmodifiable();

    public static String makeCommand(Iterable<String> paths) {
        return "stat -c '%A %h %u %U %g %G %s %X %Y %Z' " + StringCompat.join(" ", Functional.map(
                paths, ShellEscaper::escape));
    }

    public static String makeCommand(String... paths) {
        return makeCommand(Arrays.asList(paths));
    }

    public static Information parseOutput(String output) {
        Information information = new Information();
        String[] fields = output.split(" ");
        information.type = parseType(fields[0].charAt(0));
        information.permissions = parsePermissions(fields[0].substring(1));
        information.hardLinkCount = Long.parseLong(fields[1]);
        information.userId = Long.parseLong(fields[2]);
        information.userName = fields[3];
        information.groupId = Long.parseLong(fields[4]);
        information.groupName = fields[5];
        information.size = Long.parseLong(fields[6]);
        information.lastAccess = Instant.ofEpochSecond(Long.parseLong(fields[7]));
        information.lastModification = Instant.ofEpochSecond(Long.parseLong(fields[8]));
        information.lastStatusChange = Instant.ofEpochSecond(Long.parseLong(fields[9]));
        return information;
    }

    private static PosixFileType parseType(char typeChar) {
        return ObjectUtils.firstNonNull(sCharToTypeMap.get(typeChar), PosixFileType.UNKNOWN);
    }

    private static EnumSet<PosixFileModeBit> parsePermissions(String permissionsString) {
        // FIXME: We've expanded three bits.
        EnumSet<PosixFileModeBit> permissions = EnumSet.noneOf(PosixFileModeBit.class);
        PosixFileModeBit[] permissionValues = PosixFileModeBit.values();
        for (int i = 0; i < permissionValues.length; ++i) {
            PosixFileModeBit permission = permissionValues[i];
            if (permissionsString.charAt(i) == sPermissionToCharMap.get(permission)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    public static class Information implements Parcelable {

        public PosixFileType type;
        public EnumSet<PosixFileModeBit> permissions;
        public long hardLinkCount;
        public long userId;
        public String userName;
        public long groupId;
        public String groupName;
        public long size;
        public Instant lastAccess;
        public Instant lastModification;
        public Instant lastStatusChange;

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            Information that = (Information) object;
            return userId == that.userId &&
                    groupId == that.groupId &&
                    size == that.size &&
                    type == that.type &&
                    Objects.equals(permissions, that.permissions) &&
                    Objects.equals(userName, that.userName) &&
                    Objects.equals(groupName, that.groupName) &&
                    Objects.equals(lastAccess, that.lastAccess) &&
                    Objects.equals(lastModification, that.lastModification) &&
                    Objects.equals(lastStatusChange, that.lastStatusChange);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, permissions, userId, userName, groupId, groupName, size,
                    lastAccess, lastModification, lastStatusChange);
        }


        public static final Parcelable.Creator<Information> CREATOR =
                new Parcelable.Creator<Information>() {
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
            int typeOrdinal = in.readInt();
            type = typeOrdinal != -1 ? PosixFileType.values()[typeOrdinal] : null;
            //noinspection unchecked
            permissions = (EnumSet<PosixFileModeBit>) in.readSerializable();
            hardLinkCount = in.readLong();
            userId = in.readLong();
            userName = in.readString();
            groupId = in.readLong();
            groupName = in.readString();
            size = in.readLong();
            lastAccess = (Instant) in.readSerializable();
            lastModification = (Instant) in.readSerializable();
            lastStatusChange = (Instant) in.readSerializable();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type != null ? type.ordinal() : -1);
            dest.writeSerializable(permissions);
            dest.writeLong(hardLinkCount);
            dest.writeLong(userId);
            dest.writeString(userName);
            dest.writeLong(groupId);
            dest.writeString(groupName);
            dest.writeLong(size);
            dest.writeSerializable(lastAccess);
            dest.writeSerializable(lastModification);
            dest.writeSerializable(lastStatusChange);
        }
    }
}
