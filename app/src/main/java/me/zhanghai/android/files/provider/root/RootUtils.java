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
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException;

public class RootUtils {

    private static boolean sRunningAsRoot = Process.myUid() == 0;

    public static boolean isRunningAsRoot() {
        return sRunningAsRoot;
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
        RootStrategy strategy = rootablePath.getRootStrategy();
        switch (strategy) {
            case NEVER:
                return function.apply(localObject);
            case PREFER_NO: {
                try {
                    return function.apply(localObject);
                } catch (AccessDeniedException e) {
                    // Ignored.
                }
                R result = function.apply(rootObject);
                rootablePath.setPreferRoot();
                return result;
            }
            case PREFER_YES:
                try {
                    return function.apply(rootObject);
                } catch (RemoteFileSystemException e) {
                    e.printStackTrace();
                    return function.apply(localObject);
                }
            case ALWAYS:
                return function.apply(rootObject);
            default:
                throw new AssertionError(strategy);
        }
    }

    public static <T, R> R applyRootable(@NonNull Path path1, @NonNull Path path2,
                                         @NonNull T localObject, @NonNull T rootObject,
                                         @NonNull Function<T, R> function)
            throws IOException {
        RootablePath rootablePath1 = requireRootablePath(path1);
        RootablePath rootablePath2 = requireRootablePath(path2);
        RootStrategy strategy1 = rootablePath1.getRootStrategy();
        RootStrategy strategy2 = rootablePath2.getRootStrategy();
        if (strategy1 == RootStrategy.NEVER || strategy2 == RootStrategy.NEVER) {
            return function.apply(localObject);
        } else if (strategy1 == RootStrategy.ALWAYS || strategy2 == RootStrategy.ALWAYS) {
            return function.apply(rootObject);
        } else if (strategy1 == RootStrategy.PREFER_YES || strategy2 == RootStrategy.PREFER_YES) {
            // We let PREFER_YES win over PREFER_NO because user can reject a root request, but not
            // vice versa.
            try {
                return function.apply(rootObject);
            } catch (RemoteFileSystemException e) {
                e.printStackTrace();
                return function.apply(localObject);
            }
        } else {
            try {
                return function.apply(localObject);
            } catch (AccessDeniedException e) {
                // Ignored.
            }
            // We don't know which path(s) should prefer using root afterwards, so just skip
            // setPreferRoot().
            return function.apply(rootObject);
        }
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
