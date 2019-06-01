/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java8.nio.file.StandardWatchEventKinds;
import java8.nio.file.WatchEvent;
import java8.nio.file.WatchKey;
import me.zhanghai.android.files.util.CollectionUtils;

public abstract class AbstractWatchKey implements WatchKey {

    private static final int MAX_PENDING_EVENTS = 256;

    @NonNull
    private final AbstractWatchService mWatchService;
    @NonNull
    private final Path mPath;

    private boolean mSignaled;
    private List<Event<?>> mEvents = new ArrayList<>();

    @NonNull
    protected final Object mLock = new Object();

    public AbstractWatchKey(@NonNull AbstractWatchService watchService, @NonNull Path path) {
        mWatchService = Objects.requireNonNull(watchService);
        mPath = Objects.requireNonNull(path);
    }

    public <T> void addEvent(@NonNull WatchEvent.Kind<T> kind, @Nullable T context) {
        Objects.requireNonNull(kind);
        synchronized (mLock) {
            if (!mEvents.isEmpty()) {
                Event<?> lastEvent = CollectionUtils.last(mEvents);
                if (lastEvent.kind() == StandardWatchEventKinds.OVERFLOW
                        || (lastEvent.kind() == kind && Objects.equals(lastEvent.context(),
                        context))) {
                    lastEvent.repeat();
                    return;
                }
            }
            if (kind == StandardWatchEventKinds.OVERFLOW || mEvents.size() >= MAX_PENDING_EVENTS) {
                mEvents.clear();
                mEvents.add(new Event<>(StandardWatchEventKinds.OVERFLOW, null));
                signal();
                return;
            }
            mEvents.add(new Event<>(kind, context));
            signal();
        }
    }

    public void signal() {
        synchronized (mLock) {
            if (!mSignaled) {
                mSignaled = true;
                mWatchService.enqueue(this);
            }
        }
    }

    @NonNull
    @Override
    public List<WatchEvent<?>> pollEvents() {
        synchronized (mLock) {
            List<Event<?>> events = mEvents;
            mEvents = new ArrayList<>();
            //noinspection unchecked
            return (List<WatchEvent<?>>) (List<?>) events;
        }
    }

    @Override
    public boolean reset() {
        synchronized (mLock) {
            boolean valid = isValid();
            if (valid && mSignaled) {
                if (mEvents.isEmpty()) {
                    mSignaled = false;
                } else {
                    mWatchService.enqueue(this);
                }
            }
            return valid;
        }
    }

    @NonNull
    @Override
    public Path watchable() {
        return mPath;
    }

    @NonNull
    protected AbstractWatchService getWatchService() {
        return mWatchService;
    }


    private static class Event<T> implements WatchEvent<T> {

        @NonNull
        private final WatchEvent.Kind<T> mKind;
        @Nullable
        private final T mContext;

        private int mCount = 1;

        @NonNull
        private final Object mLock = new Object();

        Event(@NonNull WatchEvent.Kind<T> type, @Nullable T context) {
            mKind = Objects.requireNonNull(type);
            mContext = context;
        }

        @NonNull
        @Override
        public WatchEvent.Kind<T> kind() {
            return mKind;
        }

        @Nullable
        @Override
        public T context() {
            return mContext;
        }

        @Override
        public int count() {
            return mCount;
        }

        void repeat() {
            synchronized (mLock) {
                ++mCount;
            }
        }
    }
}
