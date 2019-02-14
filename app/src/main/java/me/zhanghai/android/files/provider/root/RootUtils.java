/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import android.os.Process;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.AccessDeniedException;
import java8.nio.file.Path;

public class RootUtils {

    private static boolean sRunningAsRoot = Process.myUid() == 0;

    public static boolean isRunningAsRoot() {
        return sRunningAsRoot;
    }

    public static void requireRunningAsRoot() {
        if (!sRunningAsRoot) {
            throw new IllegalArgumentException("Must be running as root");
        }
    }

    public static void requireRunningAsNonRoot() {
        if (sRunningAsRoot) {
            throw new IllegalArgumentException("Must be running as non-root");
        }
    }

    public static <T> void acceptRootable(@NonNull Path path, @NonNull T localObject,
                                          @NonNull T rootObject, @NonNull Consumer<T> consumer)
            throws IOException {
        applyRootable(path, localObject, rootObject, consumer.asFunction());
    }

    public static <T> void acceptRootable(@NonNull Path path1, @NonNull Path path2,
                                          @NonNull T localObject, @NonNull T rootObject,
                                          @NonNull Consumer<T> consumer) throws IOException {
        applyRootable(path1, path2, localObject, rootObject, consumer.asFunction());
    }

    public static <T, R> R applyRootable(@NonNull Path path, @NonNull T localObject,
                                         @NonNull T rootObject, @NonNull Function<T, R> function)
            throws IOException {
        RootablePath rootablePath = requireRootablePath(path);
        if (sRunningAsRoot || !rootablePath.canUseRoot()) {
            return function.apply(localObject);
        }
        if (!rootablePath.shouldUseRoot()) {
            try {
                return function.apply(localObject);
            } catch (AccessDeniedException e) {
                rootablePath.setUseRoot();
            }
        }
        return function.apply(rootObject);
    }

    public static <T, R> R applyRootable(@NonNull Path path1, @NonNull Path path2,
                                         @NonNull T localObject, @NonNull T rootObject,
                                         @NonNull Function<T, R> function)
            throws IOException {
        RootablePath rootablePath1 = requireRootablePath(path1);
        RootablePath rootablePath2 = requireRootablePath(path2);
        if (sRunningAsRoot || !rootablePath1.canUseRoot() || !rootablePath2.canUseRoot()) {
            return function.apply(localObject);
        }
        if (!rootablePath1.shouldUseRoot() && !rootablePath2.shouldUseRoot()) {
            try {
                return function.apply(localObject);
            } catch (AccessDeniedException e) {
                // Ignored, as we don't know which path(s) should use root afterwards.
            }
        }
        return function.apply(rootObject);
    }

    @NonNull
    private static RootablePath requireRootablePath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (!(path instanceof RootablePath)) {
            throw new IllegalArgumentException("Path is not a RootablePath: " + path);
        }
        return (RootablePath) path;
    }

    public interface Consumer<T> {

        void accept(T object) throws IOException;

        @NonNull
        default Function<T, ?> asFunction() {
            return object -> {
                accept(object);
                return null;
            };
        }
    }

    public interface Function<T, R> {

        R apply(T object) throws IOException;
    }
}
