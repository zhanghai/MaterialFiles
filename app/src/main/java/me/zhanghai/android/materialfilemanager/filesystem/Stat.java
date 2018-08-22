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

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.MapBuilder;
import me.zhanghai.android.materialfilemanager.util.MapCompat;
import me.zhanghai.android.materialfilemanager.util.StringCompat;

public class Stat {

    private static final Map<Character, PosixFileType> sModeTypeCharToTypeMap =
            MapBuilder.<Character, PosixFileType>newHashMap()
                    .put('d', PosixFileType.DIRECTORY)
                    .put('c', PosixFileType.CHARACTER_DEVICE)
                    .put('b', PosixFileType.BLOCK_DEVICE)
                    .put('-', PosixFileType.REGULAR_FILE)
                    .put('p', PosixFileType.FIFO)
                    .put('l', PosixFileType.SYMBOLIC_LINK)
                    .put('s', PosixFileType.SOCKET)
                    .buildUnmodifiable();

    public static String makeCommand(Iterable<String> paths) {
        return "stat -c '%A %h %u %U %g %G %s %X %Y %Z' " + StringCompat.join(" ", Functional.map(
                paths, ShellEscaper::escape));
    }

    public static String makeCommand(String... paths) {
        return makeCommand(Arrays.asList(paths));
    }

    public static Information parseOutput(String output) throws FileSystemException {
        Information information = new Information();
        try {
            String[] fields = output.split(" ");
            information.type = parseType(fields[0]);
            information.mode = parseMode(fields[0]);
            information.linkCount = Long.parseLong(fields[1]);
            information.userId = Long.parseLong(fields[2]);
            information.userName = fields[3];
            information.groupId = Long.parseLong(fields[4]);
            information.groupName = fields[5];
            information.size = Long.parseLong(fields[6]);
            information.lastAccessTime = Instant.ofEpochSecond(Long.parseLong(fields[7]));
            information.lastModificationTime = Instant.ofEpochSecond(Long.parseLong(fields[8]));
            information.lastStatusChangeTime = Instant.ofEpochSecond(Long.parseLong(fields[9]));
            // TODO: Throw when too many fields?
        } catch (IllegalArgumentException e) {
            throw new FileSystemException(R.string.file_error_information, e);
        }
        return information;
    }

    // @see https://android.googlesource.com/platform/external/toybox/+/master/lib/lib.c
    // mode_to_string()
    // @see https://github.com/coreutils/gnulib/blob/master/lib/filemode.c ftypelet()
    private static PosixFileType parseType(String modeString) {
        char typeChar = modeString.charAt(0);
        // Tolerate non-standard file types.
        return MapCompat.getOrDefault(sModeTypeCharToTypeMap, typeChar, PosixFileType.UNKNOWN);
    }

    // @see https://android.googlesource.com/platform/external/toybox/+/master/lib/lib.c
    //      mode_to_string()
    // @see https://github.com/coreutils/gnulib/blob/master/lib/filemode.c strmode()
    private static EnumSet<PosixFileModeBit> parseMode(String modeString)
            throws IllegalArgumentException {
        EnumSet<PosixFileModeBit> mode = EnumSet.noneOf(PosixFileModeBit.class);
        switch (modeString.charAt(1)) {
            case 'r':
                mode.add(PosixFileModeBit.OWNER_READ);
                // Fall through!
            case '-':
                break;
            default:
                throw new IllegalArgumentException("Unknown char '" + modeString.charAt(1) +
                        "' in mode string \"" + modeString + "\"");
        }
        switch (modeString.charAt(2)) {
            case 'w':
                mode.add(PosixFileModeBit.OWNER_WRITE);
                // Fall through!
            case '-':
                break;
            default:
                throw new IllegalArgumentException("Unknown char '" + modeString.charAt(2) +
                        "' in mode string \"" + modeString + "\"");
        }
        switch (modeString.charAt(3)) {
            case 's':
                mode.add(PosixFileModeBit.OWNER_EXECUTE);
                // Fall through!
            case 'S':
                mode.add(PosixFileModeBit.SET_USER_ID);
                break;
            case 'x':
                mode.add(PosixFileModeBit.OWNER_EXECUTE);
                // Fall through!
            case '-':
                break;
            default:
                throw new IllegalArgumentException("Unknown char '" + modeString.charAt(3) +
                        "' in mode string \"" + modeString + "\"");
        }
        switch (modeString.charAt(4)) {
            case 'r':
                mode.add(PosixFileModeBit.GROUP_READ);
                // Fall through!
            case '-':
                break;
            default:
                throw new IllegalArgumentException("Unknown char '" + modeString.charAt(4) +
                        "' in mode string \"" + modeString + "\"");
        }
        switch (modeString.charAt(5)) {
            case 'w':
                mode.add(PosixFileModeBit.GROUP_WRITE);
                // Fall through!
            case '-':
                break;
            default:
                throw new IllegalArgumentException("Unknown char '" + modeString.charAt(5) +
                        "' in mode string \"" + modeString + "\"");
        }
        switch (modeString.charAt(6)) {
            case 's':
                mode.add(PosixFileModeBit.GROUP_EXECUTE);
                // Fall through!
            case 'S':
                mode.add(PosixFileModeBit.SET_GROUP_ID);
                break;
            case 'x':
                mode.add(PosixFileModeBit.GROUP_EXECUTE);
                // Fall through!
            case '-':
                break;
            default:
                throw new IllegalArgumentException("Unknown char '" + modeString.charAt(6) +
                        "' in mode string \"" + modeString + "\"");
        }
        switch (modeString.charAt(7)) {
            case 'r':
                mode.add(PosixFileModeBit.OTHERS_READ);
                // Fall through!
            case '-':
                break;
            default:
                throw new IllegalArgumentException("Unknown char '" + modeString.charAt(7) +
                        "' in mode string \"" + modeString + "\"");
        }
        switch (modeString.charAt(8)) {
            case 'w':
                mode.add(PosixFileModeBit.OTHERS_WRITE);
                // Fall through!
            case '-':
                break;
            default:
                throw new IllegalArgumentException("Unknown char '" + modeString.charAt(8) +
                        "' in mode string \"" + modeString + "\"");
        }
        switch (modeString.charAt(9)) {
            case 't':
                mode.add(PosixFileModeBit.OTHERS_EXECUTE);
                // Fall through!
            case 'T':
                mode.add(PosixFileModeBit.STICKY);
                break;
            case 'x':
                mode.add(PosixFileModeBit.OTHERS_EXECUTE);
                // Fall through!
            case '-':
                break;
            default:
                throw new IllegalArgumentException("Unknown char '" + modeString.charAt(9) +
                        "' in mode string \"" + modeString + "\"");
        }
        return mode;
    }

    public static class Information implements Parcelable {

        public PosixFileType type;
        public EnumSet<PosixFileModeBit> mode;
        public long linkCount;
        public long userId;
        public String userName;
        public long groupId;
        public String groupName;
        public long size;
        public Instant lastAccessTime;
        public Instant lastModificationTime;
        public Instant lastStatusChangeTime;

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            Information that = (Information) object;
            return userId == that.userId
                    && groupId == that.groupId
                    && size == that.size
                    && type == that.type
                    && Objects.equals(mode, that.mode)
                    && Objects.equals(userName, that.userName)
                    && Objects.equals(groupName, that.groupName)
                    && Objects.equals(lastAccessTime, that.lastAccessTime)
                    && Objects.equals(lastModificationTime, that.lastModificationTime)
                    && Objects.equals(lastStatusChangeTime, that.lastStatusChangeTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, mode, userId, userName, groupId, groupName, size,
                    lastAccessTime, lastModificationTime, lastStatusChangeTime);
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
            mode = (EnumSet<PosixFileModeBit>) in.readSerializable();
            linkCount = in.readLong();
            userId = in.readLong();
            userName = in.readString();
            groupId = in.readLong();
            groupName = in.readString();
            size = in.readLong();
            lastAccessTime = (Instant) in.readSerializable();
            lastModificationTime = (Instant) in.readSerializable();
            lastStatusChangeTime = (Instant) in.readSerializable();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type != null ? type.ordinal() : -1);
            dest.writeSerializable(mode);
            dest.writeLong(linkCount);
            dest.writeLong(userId);
            dest.writeString(userName);
            dest.writeLong(groupId);
            dest.writeString(groupName);
            dest.writeLong(size);
            dest.writeSerializable(lastAccessTime);
            dest.writeSerializable(lastModificationTime);
            dest.writeSerializable(lastStatusChangeTime);
        }
    }
}
