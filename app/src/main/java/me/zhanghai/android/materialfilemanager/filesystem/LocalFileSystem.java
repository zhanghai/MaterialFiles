/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;
import android.system.ErrnoException;
import android.system.OsConstants;

import java.util.List;

import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.FunctionalException;
import me.zhanghai.android.materialfilemanager.functional.throwing.ThrowingFunction;

public class LocalFileSystem {

    private LocalFileSystem() {}

    @NonNull
    @WorkerThread
    public static Syscall.Information getInformation(@NonNull String path)
            throws FileSystemException {
        return getWithSyscallOrShellFs(path, Syscall::getInformation, ShellFs::getInformation);
    }

    @NonNull
    @WorkerThread
    public static List<Pair<String, Syscall.Information>> getChildren(@NonNull String path)
            throws FileSystemException {
        return getWithSyscallOrShellFs(path, LocalFileSystem::getChildrenWithSyscall,
                ShellFs::getChildrenAndInformation);
    }

    @NonNull
    @WorkerThread
    private static List<Pair<String, Syscall.Information>> getChildrenWithSyscall(
            @NonNull String path) throws FileSystemException {
        List<String> childNames = Syscall.getChildren(path);
        if (childNames == null) {
            // TODO: Correct way of throwing?
            throw new FileSystemException(new NullPointerException(
                    "Syscall.getChildren() returned null"));
        }
        List<Syscall.Information> childInformations;
        try {
            childInformations = Functional.map(childNames, (ThrowingFunction<String,
                    Syscall.Information>) childName -> Syscall.getInformation(LocalFile.joinPaths(
                    path, childName)));
        } catch (FunctionalException e) {
            throw e.getCauseAs(FileSystemException.class);
        }
        return Functional.map(childNames, (childName, index) -> new Pair<>(childName,
                childInformations.get(index)));
    }

    @NonNull
    private static <T> T getWithSyscallOrShellFs(@NonNull String path,
                                                 @NonNull GetValueFromPath<T> getWithSyscall,
                                                 @NonNull GetValueFromPath<T> getWithShellFs)
            throws FileSystemException {
        try {
            return getWithSyscall.get(path);
        } catch (FileSystemException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ErrnoException) {
                ErrnoException errnoException = (ErrnoException) cause;
                if (errnoException.errno == OsConstants.EACCES) {
                    return getWithShellFs.get(path);
                }
            }
            throw e;
        }
    }

    private interface GetValueFromPath<T> {
        @NonNull
        T get(@NonNull String path) throws FileSystemException;
    }
}
