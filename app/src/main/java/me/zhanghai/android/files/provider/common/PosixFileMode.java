/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.system.OsConstants;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.attribute.PosixFilePermission;

public class PosixFileMode {

    public static final Set<PosixFileModeBit> DEFAULT_MODE_CREATE_FILE = fromInt(OsConstants.S_IRUSR
            | OsConstants.S_IWUSR | OsConstants.S_IRGRP | OsConstants.S_IWGRP | OsConstants.S_IROTH
            | OsConstants.S_IWOTH);

    public static final Set<PosixFileModeBit> DEFAULT_MODE_CREATE_DIRECTORY = fromInt(
            OsConstants.S_IRWXU | OsConstants.S_IRWXG | OsConstants.S_IRWXO);

    private PosixFileMode() {}

    @NonNull
    public static Set<PosixFileModeBit> fromAttributes(@NonNull FileAttribute<?>[] attributes,
                                                       @NonNull Set<PosixFileModeBit> defaultMode) {
        Set<PosixFileModeBit> mode = null;
        for (FileAttribute<?> attribute : attributes) {
            Objects.requireNonNull(attribute);
            if (!Objects.equals(attribute.name(), PosixFileModeAttribute.NAME)) {
                throw new UnsupportedOperationException(attribute.name());
            }
            Object value = attribute.value();
            Objects.requireNonNull(value);
            if (!(value instanceof Set)) {
                throw new UnsupportedOperationException(value.toString());
            }
            //noinspection unchecked
            mode = (Set<PosixFileModeBit>) value;
            for (Object modeBit : mode) {
                Objects.requireNonNull(modeBit);
                if (!(modeBit instanceof PosixFileModeBit)) {
                    throw new UnsupportedOperationException(modeBit.toString());
                }
            }
        }
        return mode != null ? mode : defaultMode;
    }

    @NonNull
    public static EnumSet<PosixFileModeBit> fromInt(int modeInt) {
        EnumSet<PosixFileModeBit> mode = EnumSet.noneOf(PosixFileModeBit.class);
        if ((modeInt & OsConstants.S_ISUID) != 0) {
            mode.add(PosixFileModeBit.SET_USER_ID);
        }
        if ((modeInt & OsConstants.S_ISGID) != 0) {
            mode.add(PosixFileModeBit.SET_GROUP_ID);
        }
        if ((modeInt & OsConstants.S_ISVTX) != 0) {
            mode.add(PosixFileModeBit.STICKY);
        }
        if ((modeInt & OsConstants.S_IRUSR) != 0) {
            mode.add(PosixFileModeBit.OWNER_READ);
        }
        if ((modeInt & OsConstants.S_IWUSR) != 0) {
            mode.add(PosixFileModeBit.OWNER_WRITE);
        }
        if ((modeInt & OsConstants.S_IXUSR) != 0) {
            mode.add(PosixFileModeBit.OWNER_EXECUTE);
        }
        if ((modeInt & OsConstants.S_IRGRP) != 0) {
            mode.add(PosixFileModeBit.GROUP_READ);
        }
        if ((modeInt & OsConstants.S_IWGRP) != 0) {
            mode.add(PosixFileModeBit.GROUP_WRITE);
        }
        if ((modeInt & OsConstants.S_IXGRP) != 0) {
            mode.add(PosixFileModeBit.GROUP_EXECUTE);
        }
        if ((modeInt & OsConstants.S_IROTH) != 0) {
            mode.add(PosixFileModeBit.OTHERS_READ);
        }
        if ((modeInt & OsConstants.S_IWOTH) != 0) {
            mode.add(PosixFileModeBit.OTHERS_WRITE);
        }
        if ((modeInt & OsConstants.S_IXOTH) != 0) {
            mode.add(PosixFileModeBit.OTHERS_EXECUTE);
        }
        return mode;
    }

    @NonNull
    public static EnumSet<PosixFileModeBit> fromPermissions(
            @NonNull Set<PosixFilePermission> permissions) {
        EnumSet<PosixFileModeBit> mode = EnumSet.noneOf(PosixFileModeBit.class);
        for (PosixFilePermission permission : permissions) {
            switch (permission) {
                case OWNER_READ:
                    mode.add(PosixFileModeBit.OWNER_READ);
                    break;
                case OWNER_WRITE:
                    mode.add(PosixFileModeBit.OWNER_WRITE);
                    break;
                case OWNER_EXECUTE:
                    mode.add(PosixFileModeBit.OWNER_EXECUTE);
                    break;
                case GROUP_READ:
                    mode.add(PosixFileModeBit.GROUP_READ);
                    break;
                case GROUP_WRITE:
                    mode.add(PosixFileModeBit.GROUP_WRITE);
                    break;
                case GROUP_EXECUTE:
                    mode.add(PosixFileModeBit.GROUP_EXECUTE);
                    break;
                case OTHERS_READ:
                    mode.add(PosixFileModeBit.OTHERS_READ);
                    break;
                case OTHERS_WRITE:
                    mode.add(PosixFileModeBit.OTHERS_WRITE);
                    break;
                case OTHERS_EXECUTE:
                    mode.add(PosixFileModeBit.OTHERS_EXECUTE);
                    break;
                default:
                    throw new UnsupportedOperationException(permission.toString());
            }
        }
        return mode;
    }

    @NonNull
    public static FileAttribute<Set<PosixFileModeBit>> toAttribute(
            @NonNull Set<PosixFileModeBit> mode) {
        return new PosixFileModeAttribute(mode);
    }

    public static int toInt(@NonNull Set<PosixFileModeBit> mode) {
        Objects.requireNonNull(mode);
        return (mode.contains(PosixFileModeBit.SET_USER_ID) ? OsConstants.S_ISUID : 0)
                | (mode.contains(PosixFileModeBit.SET_GROUP_ID) ? OsConstants.S_ISGID : 0)
                | (mode.contains(PosixFileModeBit.STICKY) ? OsConstants.S_ISVTX : 0)
                | (mode.contains(PosixFileModeBit.OWNER_READ) ? OsConstants.S_IRUSR : 0)
                | (mode.contains(PosixFileModeBit.OWNER_WRITE) ? OsConstants.S_IWUSR : 0)
                | (mode.contains(PosixFileModeBit.OWNER_EXECUTE) ? OsConstants.S_IXUSR : 0)
                | (mode.contains(PosixFileModeBit.GROUP_READ) ? OsConstants.S_IRGRP : 0)
                | (mode.contains(PosixFileModeBit.GROUP_WRITE) ? OsConstants.S_IWGRP : 0)
                | (mode.contains(PosixFileModeBit.GROUP_EXECUTE) ? OsConstants.S_IXGRP : 0)
                | (mode.contains(PosixFileModeBit.OTHERS_READ) ? OsConstants.S_IROTH : 0)
                | (mode.contains(PosixFileModeBit.OTHERS_WRITE) ? OsConstants.S_IWOTH : 0)
                | (mode.contains(PosixFileModeBit.OTHERS_EXECUTE) ? OsConstants.S_IXOTH : 0);
    }

    @NonNull
    public static EnumSet<PosixFilePermission> toPermissions(
            @NonNull Set<PosixFileModeBit> mode) {
        EnumSet<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);
        for (PosixFileModeBit modeBit : mode) {
            switch (modeBit) {
                case OWNER_READ:
                    permissions.add(PosixFilePermission.OWNER_READ);
                    break;
                case OWNER_WRITE:
                    permissions.add(PosixFilePermission.OWNER_WRITE);
                    break;
                case OWNER_EXECUTE:
                    permissions.add(PosixFilePermission.OWNER_EXECUTE);
                    break;
                case GROUP_READ:
                    permissions.add(PosixFilePermission.GROUP_READ);
                    break;
                case GROUP_WRITE:
                    permissions.add(PosixFilePermission.GROUP_WRITE);
                    break;
                case GROUP_EXECUTE:
                    permissions.add(PosixFilePermission.GROUP_EXECUTE);
                    break;
                case OTHERS_READ:
                    permissions.add(PosixFilePermission.OTHERS_READ);
                    break;
                case OTHERS_WRITE:
                    permissions.add(PosixFilePermission.OTHERS_WRITE);
                    break;
                case OTHERS_EXECUTE:
                    permissions.add(PosixFilePermission.OTHERS_EXECUTE);
                    break;
                default:
                    throw new UnsupportedOperationException(modeBit.toString());
            }
        }
        return permissions;
    }

    @NonNull
    public static String toString(@NonNull Set<PosixFileModeBit> mode) {
        Objects.requireNonNull(mode);
        boolean hasSetUserIdBit = mode.contains(PosixFileModeBit.SET_USER_ID);
        boolean hasSetGroupIdBit = mode.contains(PosixFileModeBit.SET_GROUP_ID);
        boolean hasStickyBit = mode.contains(PosixFileModeBit.STICKY);
        return new StringBuilder()
                .append(mode.contains(PosixFileModeBit.OWNER_READ) ? 'r' : '-')
                .append(mode.contains(PosixFileModeBit.OWNER_WRITE) ? 'w' : '-')
                .append(mode.contains(PosixFileModeBit.OWNER_EXECUTE) ? hasSetUserIdBit ? 's' : 'x'
                        : hasSetUserIdBit ? 'S' : '-')
                .append(mode.contains(PosixFileModeBit.GROUP_READ) ? 'r' : '-')
                .append(mode.contains(PosixFileModeBit.GROUP_WRITE) ? 'w' : '-')
                .append(mode.contains(PosixFileModeBit.GROUP_EXECUTE) ? hasSetGroupIdBit ? 's' : 'x'
                        : hasSetGroupIdBit ? 'S' : '-')
                .append(mode.contains(PosixFileModeBit.OTHERS_READ) ? 'r' : '-')
                .append(mode.contains(PosixFileModeBit.OTHERS_WRITE) ? 'w' : '-')
                .append(mode.contains(PosixFileModeBit.OTHERS_EXECUTE) ? hasStickyBit ? 't' : 'x'
                        : hasStickyBit ? 'T' : '-')
                .toString();
    }
}
