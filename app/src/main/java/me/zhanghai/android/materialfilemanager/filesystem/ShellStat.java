/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;

import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import eu.chainfire.libsuperuser.Shell;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.FunctionalException;
import me.zhanghai.android.materialfilemanager.functional.throwing.ThrowingFunction;
import me.zhanghai.android.materialfilemanager.util.MapBuilder;
import me.zhanghai.android.materialfilemanager.util.MapCompat;
import me.zhanghai.android.materialfilemanager.util.StringCompat;

public class ShellStat {

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

    public static Information loadInformation(String path) throws FileSystemException {
        String command = "stat -c '%A/%h/%u/%U/%g/%G/%s/%X/%Y/%Z' " + ShellEscaper.escape(path);
        List<String> outputLines = Shell.SH.run(command);
        if (outputLines == null) {
            throw new FileSystemException(R.string.file_error_information);
        }
        String output = StringCompat.join("\n", outputLines);
        Information information = parseInformation(output);
        loadSymbolicLinkInformationIf(path, information);
        return information;
    }

    public static List<Pair<String, Information>> getChildrenAndInformation(String path)
            throws FileSystemException {
        // Normalize the path to prevent consecutive slashes.
        path = new java.io.File(path).getPath();
        // For easier replacement later.
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        String command = "find " + ShellEscaper.escape(path) + " -mindepth 1 -maxdepth 1 -exec " +
                "stat -c '%n/%A/%h/%u/%U/%g/%G/%s/%X/%Y/%Z//' {} \\;";
        // TODO: Handle error and show the file name at least.
        List<String> outputLines = Shell.SH.run(command);
        if (outputLines == null) {
            throw new FileSystemException(R.string.file_list_error_directory);
        }
        String output = StringCompat.join("\n", outputLines);
        if (output.isEmpty()) {
            return Collections.emptyList();
        }
        // Strip the last "//" of output.
        if (!output.endsWith("//")) {
            throw new FileSystemException(R.string.file_error_information);
        }
        output = output.substring(0, output.length() - 2);
        List<String> childStats = Arrays.asList(output.split("\n//"));
        // Strip the leading path.
        String finalPath = path;
        if (!Functional.every(childStats, childStat -> childStat.startsWith(finalPath))) {
            throw new FileSystemException(R.string.file_error_information);
        }
        int pathLength = path.length();
        childStats = Functional.map(childStats, childStat -> childStat.substring(pathLength));
        List<Pair<String, Information>> children;
        try {
            children = Functional.map(childStats,
                    (ThrowingFunction<String, Pair<String, Information>>) childStat -> {
                        String[] childNameAndStat2 = childStat.split("/", 2);
                        if (childNameAndStat2.length != 2) {
                            throw new FileSystemException(R.string.file_error_information);
                        }
                        String childName = childNameAndStat2[0];
                        String childStat2 = childNameAndStat2[1];
                        Information childInformation = parseInformation(childStat2);
                        String childPath = finalPath + childName;
                        loadSymbolicLinkInformationIf(childPath, childInformation);
                        return new Pair<>(childName, childInformation);
                    });
        } catch (FunctionalException e) {
            throw e.getCauseAs(FileSystemException.class);
        }
        return children;
    }

    private static void loadSymbolicLinkInformationIf(String path, Information information)
            throws FileSystemException {
        if (information.type != PosixFileType.SYMBOLIC_LINK) {
            return;
        }
        String escapedPath = ShellEscaper.escape(path);
        String[] commands = {
                "stat -L -c '%A/%h/%u/%U/%g/%G/%s/%X/%Y/%Z' " + escapedPath + " || echo",
                "readlink " + escapedPath
        };
        List<String> outputLines = Shell.SH.run(commands);
        if (outputLines == null || outputLines.size() < 1) {
            throw new FileSystemException(R.string.file_error_information);
        }
        String statLOutput = outputLines.get(0);
        String readlinkOutput = StringCompat.join("\n", outputLines.subList(1, outputLines.size()));
        information.symbolicLinkTarget = readlinkOutput;
        try {
            information.symbolicLinkStatLInformation = parseInformation(statLOutput);
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    private static Information parseInformation(String output) throws FileSystemException {
        Information information = new Information();
        String[] fields = output.split("/");
        if (fields.length != 10) {
            throw new FileSystemException(R.string.file_error_information);
        }
        try {
            String modeString = fields[0];
            information.type = parseType(modeString);
            information.mode = parseMode(modeString);
            information.linkCount = Long.parseLong(fields[1]);
            information.userId = Long.parseLong(fields[2]);
            information.userName = fields[3];
            information.groupId = Long.parseLong(fields[4]);
            information.groupName = fields[5];
            information.size = Long.parseLong(fields[6]);
            information.lastAccessTime = Instant.ofEpochSecond(Long.parseLong(fields[7]));
            information.lastModificationTime = Instant.ofEpochSecond(Long.parseLong(fields[8]));
            information.lastStatusChangeTime = Instant.ofEpochSecond(Long.parseLong(fields[9]));
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
            return linkCount == that.linkCount
                    && userId == that.userId
                    && groupId == that.groupId
                    && size == that.size
                    && type == that.type
                    && Objects.equals(mode, that.mode)
                    && Objects.equals(userName, that.userName)
                    && Objects.equals(groupName, that.groupName)
                    && Objects.equals(lastAccessTime, that.lastAccessTime)
                    && Objects.equals(lastModificationTime, that.lastModificationTime)
                    && Objects.equals(lastStatusChangeTime, that.lastStatusChangeTime)
                    && Objects.equals(symbolicLinkTarget, that.symbolicLinkTarget)
                    && Objects.equals(symbolicLinkStatLInformation,
                    that.symbolicLinkStatLInformation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, mode, linkCount, userId, userName, groupId, groupName, size,
                    lastAccessTime, lastModificationTime, lastStatusChangeTime, symbolicLinkTarget,
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
            linkCount = in.readLong();
            userId = in.readLong();
            userName = in.readString();
            groupId = in.readLong();
            groupName = in.readString();
            size = in.readLong();
            lastAccessTime = (Instant) in.readSerializable();
            lastModificationTime = (Instant) in.readSerializable();
            lastStatusChangeTime = (Instant) in.readSerializable();
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
            dest.writeLong(linkCount);
            dest.writeLong(userId);
            dest.writeString(userName);
            dest.writeLong(groupId);
            dest.writeString(groupName);
            dest.writeLong(size);
            dest.writeSerializable(lastAccessTime);
            dest.writeSerializable(lastModificationTime);
            dest.writeSerializable(lastStatusChangeTime);
            dest.writeString(symbolicLinkTarget);
            dest.writeParcelable(symbolicLinkStatLInformation, flags);
        }
    }
}
