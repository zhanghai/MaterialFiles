/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;

public abstract class CovariantPath<PathType extends CovariantPath<PathType>> implements Path {

    @Nullable
    @Override
    public abstract PathType getRoot();

    @Nullable
    @Override
    public abstract PathType getFileName();

    @Nullable
    @Override
    public abstract PathType getParent();

    @NonNull
    @Override
    public abstract PathType getName(int index);

    @NonNull
    @Override
    public abstract PathType subpath(int beginIndex, int endIndex);

    @NonNull
    @Override
    public abstract PathType normalize();

    @NonNull
    @Override
    public abstract PathType resolve(@NonNull Path other);

    @NonNull
    @Override
    public abstract PathType resolve(@NonNull String other);

    @NonNull
    @Override
    public abstract PathType resolveSibling(@NonNull Path other);

    @NonNull
    @Override
    public abstract PathType resolveSibling(@NonNull String other);

    @NonNull
    @Override
    public abstract PathType relativize(@NonNull Path other);

    @NonNull
    @Override
    public abstract PathType toAbsolutePath();

    @NonNull
    @Override
    public abstract PathType toRealPath(@NonNull LinkOption... options) throws IOException;
}
