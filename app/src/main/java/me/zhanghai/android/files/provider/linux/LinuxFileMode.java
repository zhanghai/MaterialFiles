/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.OsConstants;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;

public class LinuxFileMode {

    private LinuxFileMode() {}

    @NonNull
    public static EnumSet<LinuxFileModeBit> fromInt(int modeInt) {
        EnumSet<LinuxFileModeBit> mode = EnumSet.noneOf(LinuxFileModeBit.class);
        if ((modeInt & OsConstants.S_ISUID) != 0) {
            mode.add(LinuxFileModeBit.SET_USER_ID);
        }
        if ((modeInt & OsConstants.S_ISGID) != 0) {
            mode.add(LinuxFileModeBit.SET_GROUP_ID);
        }
        if ((modeInt & OsConstants.S_ISVTX) != 0) {
            mode.add(LinuxFileModeBit.STICKY);
        }
        if ((modeInt & OsConstants.S_IRUSR) != 0) {
            mode.add(LinuxFileModeBit.OWNER_READ);
        }
        if ((modeInt & OsConstants.S_IWUSR) != 0) {
            mode.add(LinuxFileModeBit.OWNER_WRITE);
        }
        if ((modeInt & OsConstants.S_IXUSR) != 0) {
            mode.add(LinuxFileModeBit.OWNER_EXECUTE);
        }
        if ((modeInt & OsConstants.S_IRGRP) != 0) {
            mode.add(LinuxFileModeBit.GROUP_READ);
        }
        if ((modeInt & OsConstants.S_IWGRP) != 0) {
            mode.add(LinuxFileModeBit.GROUP_WRITE);
        }
        if ((modeInt & OsConstants.S_IXGRP) != 0) {
            mode.add(LinuxFileModeBit.GROUP_EXECUTE);
        }
        if ((modeInt & OsConstants.S_IROTH) != 0) {
            mode.add(LinuxFileModeBit.OTHERS_READ);
        }
        if ((modeInt & OsConstants.S_IWOTH) != 0) {
            mode.add(LinuxFileModeBit.OTHERS_WRITE);
        }
        if ((modeInt & OsConstants.S_IXOTH) != 0) {
            mode.add(LinuxFileModeBit.OTHERS_EXECUTE);
        }
        return mode;
    }

    public static int toInt(@NonNull Set<LinuxFileModeBit> mode) {
        Objects.requireNonNull(mode);
        return (mode.contains(LinuxFileModeBit.SET_USER_ID) ? OsConstants.S_ISUID : 0)
                | (mode.contains(LinuxFileModeBit.SET_GROUP_ID) ? OsConstants.S_ISGID : 0)
                | (mode.contains(LinuxFileModeBit.STICKY) ? OsConstants.S_ISVTX : 0)
                | (mode.contains(LinuxFileModeBit.OWNER_READ) ? OsConstants.S_IRUSR : 0)
                | (mode.contains(LinuxFileModeBit.OWNER_WRITE) ? OsConstants.S_IWUSR : 0)
                | (mode.contains(LinuxFileModeBit.OWNER_EXECUTE) ? OsConstants.S_IXUSR : 0)
                | (mode.contains(LinuxFileModeBit.GROUP_READ) ? OsConstants.S_IRGRP : 0)
                | (mode.contains(LinuxFileModeBit.GROUP_WRITE) ? OsConstants.S_IWGRP : 0)
                | (mode.contains(LinuxFileModeBit.GROUP_EXECUTE) ? OsConstants.S_IXGRP : 0)
                | (mode.contains(LinuxFileModeBit.OTHERS_READ) ? OsConstants.S_IROTH : 0)
                | (mode.contains(LinuxFileModeBit.OTHERS_WRITE) ? OsConstants.S_IWOTH : 0)
                | (mode.contains(LinuxFileModeBit.OTHERS_EXECUTE) ? OsConstants.S_IXOTH : 0);
    }

    @NonNull
    public static String toString(@NonNull Set<LinuxFileModeBit> mode) {
        Objects.requireNonNull(mode);
        boolean hasSetUserIdBit = mode.contains(LinuxFileModeBit.SET_USER_ID);
        boolean hasSetGroupIdBit = mode.contains(LinuxFileModeBit.SET_GROUP_ID);
        boolean hasStickyBit = mode.contains(LinuxFileModeBit.STICKY);
        return new StringBuilder()
                .append(mode.contains(LinuxFileModeBit.OWNER_READ) ? 'r' : '-')
                .append(mode.contains(LinuxFileModeBit.OWNER_WRITE) ? 'w' : '-')
                .append(mode.contains(LinuxFileModeBit.OWNER_EXECUTE) ? hasSetUserIdBit ? 's' : 'x'
                        : hasSetUserIdBit ? 'S' : '-')
                .append(mode.contains(LinuxFileModeBit.GROUP_READ) ? 'r' : '-')
                .append(mode.contains(LinuxFileModeBit.GROUP_WRITE) ? 'w' : '-')
                .append(mode.contains(LinuxFileModeBit.GROUP_EXECUTE) ? hasSetGroupIdBit ? 's' : 'x'
                        : hasSetGroupIdBit ? 'S' : '-')
                .append(mode.contains(LinuxFileModeBit.OTHERS_READ) ? 'r' : '-')
                .append(mode.contains(LinuxFileModeBit.OTHERS_WRITE) ? 'w' : '-')
                .append(mode.contains(LinuxFileModeBit.OTHERS_EXECUTE) ? hasStickyBit ? 't' : 'x'
                        : hasStickyBit ? 'T' : '-')
                .toString();
    }
}
