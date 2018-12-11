/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filesystem;

import android.text.TextUtils;

import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import me.zhanghai.android.files.AppApplication;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.functional.Functional;
import me.zhanghai.android.files.functional.FunctionalException;
import me.zhanghai.android.files.functional.throwing.ThrowingFunction;
import me.zhanghai.android.files.provider.linux.LinuxFileMode;
import me.zhanghai.android.files.provider.linux.LinuxFileModeBit;
import me.zhanghai.android.files.shell.SuShell;
import me.zhanghai.android.files.util.Holder;

public class ShellFs {

    @NonNull
    public static LocalFileSystem.Information getInformation(@NonNull String path)
            throws FileSystemException {
        String command = ShellEscaper.escape(getFsPath()) + " -f " + ShellEscaper.escape(path);
        Holder<String> outputHolder = new Holder<>();
        int exitCode = SuShell.run(command, outputHolder, null);
        if (exitCode != 0) {
            throw new FileSystemException(R.string.file_error_information);
        }
        String output = outputHolder.value;
        LocalFileSystem.Information information = parseInformation(output);
        return information;
    }

    @NonNull
    public static List<Pair<String, LocalFileSystem.Information>> getChildrenAndInformation(
            @NonNull String path) throws FileSystemException {
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
        List<Pair<String, LocalFileSystem.Information>> children;
        try {
            children = Functional.map(childOutputs, (ThrowingFunction<String, Pair<String,
                    LocalFileSystem.Information>>) childStat -> {
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
                        LocalFileSystem.Information childInformation = !TextUtils.isEmpty(
                                childFile) ? parseInformation(childFile) : null;
                        return new Pair<>(childName, childInformation);
                    });
        } catch (FunctionalException e) {
            throw e.getCauseAs(FileSystemException.class);
        }
        return children;
    }

    @NonNull
    private static String getFsPath() {
        return AppApplication.getInstance().getApplicationInfo().nativeLibraryDir + "/libfs.so";
    }

    @NonNull
    private static LocalFileSystem.Information parseInformation(@NonNull String output)
            throws FileSystemException {
        String[] fields = output.split("\0");
        if (fields.length < 7) {
            throw new FileSystemException(R.string.file_error_information);
        }
        try {
            boolean isSymbolicLinkStat = Integer.parseInt(fields[0]) == 1;
            int typeAndMode = Integer.parseInt(fields[1]);
            PosixFileType type = Syscall.parseType(typeAndMode);
            EnumSet<LinuxFileModeBit> mode = LinuxFileMode.fromInt(typeAndMode);
            PosixUser owner = new PosixUser();
            owner.id = Integer.parseInt(fields[2]);
            PosixGroup group = new PosixGroup();
            group.id = Integer.parseInt(fields[3]);
            long size = Long.parseLong(fields[4]);
            Instant lastModificationTime = Instant.ofEpochSecond(Long.parseLong(fields[5]),
                    Long.parseLong(fields[6]));
            int index = 7;
            if (index >= fields.length) {
                throw new IllegalArgumentException();
            }
            boolean isSymbolicLink = Integer.parseInt(fields[index]) == 1;
            ++index;
            String symbolicLinkTarget = null;
            if (isSymbolicLink) {
                if (index >= fields.length) {
                    throw new IllegalArgumentException();
                }
                symbolicLinkTarget = fields[index];
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
                owner.name = fields[index];
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
                group.name = fields[index];
                ++index;
            }
            if (index != fields.length) {
                throw new IllegalArgumentException();
            }
            return new LocalFileSystem.Information(isSymbolicLinkStat, type, mode, owner, group,
                    size, lastModificationTime, isSymbolicLink, symbolicLinkTarget);
        } catch (IllegalArgumentException e) {
            throw new FileSystemException(R.string.file_error_information, e);
        }
    }
}
