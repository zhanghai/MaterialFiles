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
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.attribute.PosixFilePermission;

public class LinuxFileMode {

    static final Set<LinuxFileModeBit> DEFAULT_MODE_CREATE_FILE = fromInt(OsConstants.S_IRUSR
            | OsConstants.S_IWUSR | OsConstants.S_IRGRP | OsConstants.S_IWGRP | OsConstants.S_IROTH
            | OsConstants.S_IWOTH);

    static final Set<LinuxFileModeBit> DEFAULT_MODE_CREATE_DIRECTORY = fromInt(OsConstants.S_IRWXU
            | OsConstants.S_IRWXG | OsConstants.S_IRWXO);

    private LinuxFileMode() {}

    @NonNull
    static Set<LinuxFileModeBit> fromAttributes(@NonNull FileAttribute<?>[] attributes,
                                                @NonNull Set<LinuxFileModeBit> defaultMode) {
        Set<LinuxFileModeBit> mode = null;
        for (FileAttribute<?> attribute : attributes) {
            Objects.requireNonNull(attribute);
            if (!Objects.equals(attribute.name(), LinuxFileModeAttribute.NAME)) {
                throw new UnsupportedOperationException(attribute.name());
            }
            Object value = attribute.value();
            Objects.requireNonNull(value);
            if (!(value instanceof Set)) {
                throw new UnsupportedOperationException(value.toString());
            }
            //noinspection unchecked
            mode = (Set<LinuxFileModeBit>) value;
            for (Object modeBit : mode) {
                Objects.requireNonNull(modeBit);
                if (!(modeBit instanceof LinuxFileModeBit)) {
                    throw new UnsupportedOperationException(modeBit.toString());
                }
            }
        }
        return mode != null ? mode : defaultMode;
    }

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

    @NonNull
    public static EnumSet<LinuxFileModeBit> fromPermissions(
            @NonNull Set<PosixFilePermission> permissions) {
        EnumSet<LinuxFileModeBit> mode = EnumSet.noneOf(LinuxFileModeBit.class);
        for (PosixFilePermission permission : permissions) {
            switch (permission) {
                case OWNER_READ:
                    mode.add(LinuxFileModeBit.OWNER_READ);
                    break;
                case OWNER_WRITE:
                    mode.add(LinuxFileModeBit.OWNER_WRITE);
                    break;
                case OWNER_EXECUTE:
                    mode.add(LinuxFileModeBit.OWNER_EXECUTE);
                    break;
                case GROUP_READ:
                    mode.add(LinuxFileModeBit.GROUP_READ);
                    break;
                case GROUP_WRITE:
                    mode.add(LinuxFileModeBit.GROUP_WRITE);
                    break;
                case GROUP_EXECUTE:
                    mode.add(LinuxFileModeBit.GROUP_EXECUTE);
                    break;
                case OTHERS_READ:
                    mode.add(LinuxFileModeBit.OTHERS_READ);
                    break;
                case OTHERS_WRITE:
                    mode.add(LinuxFileModeBit.OTHERS_WRITE);
                    break;
                case OTHERS_EXECUTE:
                    mode.add(LinuxFileModeBit.OTHERS_EXECUTE);
                    break;
                default:
                    throw new UnsupportedOperationException(permission.toString());
            }
        }
        return mode;
    }

    @NonNull
    public static FileAttribute<Set<LinuxFileModeBit>> toAttributes(
            @NonNull Set<LinuxFileModeBit> mode) {
        return new LinuxFileModeAttribute(mode);
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
