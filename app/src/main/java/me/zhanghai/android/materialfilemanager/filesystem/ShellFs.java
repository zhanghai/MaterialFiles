/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.zhanghai.android.materialfilemanager.AppApplication;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.FunctionalException;
import me.zhanghai.android.materialfilemanager.functional.throwing.ThrowingFunction;
import me.zhanghai.android.materialfilemanager.shell.SuShell;
import me.zhanghai.android.materialfilemanager.util.Holder;

public class ShellFs {

    @NonNull
    public static Syscall.Information loadInformation(@NonNull String path)
            throws FileSystemException {
        String command = ShellEscaper.escape(getFsPath()) + " -f " + ShellEscaper.escape(path);
        Holder<String> outputHolder = new Holder<>();
        int exitCode = SuShell.run(command, outputHolder, null);
        if (exitCode != 0) {
            throw new FileSystemException(R.string.file_error_information);
        }
        String output = outputHolder.value;
        Syscall.Information information = parseInformation(output);
        return information;
    }

    @NonNull
    public static List<Pair<String, Syscall.Information>> getChildrenAndInformation(
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
        List<Pair<String, Syscall.Information>> children;
        try {
            children = Functional.map(childOutputs,
                    (ThrowingFunction<String, Pair<String, Syscall.Information>>) childStat -> {
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
                        Syscall.Information childInformation = !TextUtils.isEmpty(childFile) ?
                                parseInformation(childFile) : null;
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
    private static Syscall.Information parseInformation(@NonNull String output)
            throws FileSystemException {
        Syscall.Information information = new Syscall.Information();
        String[] fields = output.split("\0");
        if (fields.length < 7) {
            throw new FileSystemException(R.string.file_error_information);
        }
        try {
            information.isSymbolicLinkStat = Integer.parseInt(fields[0]) == 1;
            int mode = Integer.parseInt(fields[1]);
            information.type = Syscall.parseType(mode);
            information.mode = Syscall.parseMode(mode);
            information.owner = new PosixUser();
            information.owner.id = Integer.parseInt(fields[2]);
            information.group = new PosixGroup();
            information.group.id = Integer.parseInt(fields[3]);
            information.size = Long.parseLong(fields[4]);
            information.lastModificationTime = Instant.ofEpochSecond(Long.parseLong(fields[5]),
                    Long.parseLong(fields[6]));
            int index = 7;
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
}
