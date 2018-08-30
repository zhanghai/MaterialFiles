/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.fileproperties;

import android.content.Context;

import java.util.Set;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.filesystem.PosixFileModeBit;

public class FilePropertiesPermissions {

    private FilePropertiesPermissions() {}

    public static String getOwnerPermissionsString(boolean isDirectory, Set<PosixFileModeBit> mode,
                                                   Context context) {
        return getPermissionsString(isDirectory, mode.contains(PosixFileModeBit.OWNER_READ),
                mode.contains(PosixFileModeBit.OWNER_WRITE), mode.contains(
                        PosixFileModeBit.OWNER_EXECUTE), context);
    }

    public static String getGroupPermissionsString(boolean isDirectory, Set<PosixFileModeBit> mode,
                                                   Context context) {
        return getPermissionsString(isDirectory, mode.contains(PosixFileModeBit.GROUP_READ),
                mode.contains(PosixFileModeBit.GROUP_WRITE), mode.contains(
                        PosixFileModeBit.GROUP_EXECUTE), context);
    }

    public static String getOthersPermissionsString(boolean isDirectory, Set<PosixFileModeBit> mode,
                                                    Context context) {
        return getPermissionsString(isDirectory, mode.contains(PosixFileModeBit.OTHERS_READ),
                mode.contains(PosixFileModeBit.OTHERS_WRITE), mode.contains(
                        PosixFileModeBit.OTHERS_EXECUTE), context);
    }

    private static String getPermissionsString(boolean isDirectory, boolean read, boolean write,
                                               boolean execute, Context context) {
        return context.getString(getPermissionsStringRes(isDirectory, read, write, execute));
    }

    private static int getPermissionsStringRes(boolean isDirectory, boolean read, boolean write,
                                               boolean execute) {
        if (isDirectory) {
            if (read) {
                if (write) {
                    if (execute) {
                        return R.string.file_properties_permissions_directory_read_write_execute;
                    } else {
                        return R.string.file_properties_permissions_directory_read_write;
                    }
                } else {
                    if (execute) {
                        return R.string.file_properties_permissions_directory_read_execute;
                    } else {
                        return R.string.file_properties_permissions_directory_read;
                    }
                }
            } else {
                if (write) {
                    if (execute) {
                        return R.string.file_properties_permissions_directory_write_execute;
                    } else {
                        return R.string.file_properties_permissions_directory_write;
                    }
                } else {
                    if (execute) {
                        return R.string.file_properties_permissions_directory_execute;
                    } else {
                        return R.string.file_properties_permissions_directory_none;
                    }
                }
            }
        } else {
            if (read) {
                if (write) {
                    return R.string.file_properties_permissions_file_read_write;
                } else {
                    return R.string.file_properties_permissions_file_read;
                }
            } else {
                if (write) {
                    return R.string.file_properties_permissions_file_write;
                } else {
                    return R.string.file_properties_permissions_file_none;
                }
            }
        }
    }

    public static Boolean isExecutable(Set<PosixFileModeBit> mode) {
        boolean ownerExecute = mode.contains(PosixFileModeBit.OWNER_EXECUTE);
        boolean groupExecute = mode.contains(PosixFileModeBit.GROUP_EXECUTE);
        boolean othersExecute = mode.contains(PosixFileModeBit.OTHERS_EXECUTE);
        if (ownerExecute && groupExecute && othersExecute) {
            return true;
        } else if (!ownerExecute && !groupExecute && !othersExecute) {
            return false;
        } else {
            return null;
        }
    }
}
