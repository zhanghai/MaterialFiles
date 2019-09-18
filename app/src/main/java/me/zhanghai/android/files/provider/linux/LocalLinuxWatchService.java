/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.OsConstants;
import android.system.StructPollfd;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import java8.nio.file.ClosedWatchServiceException;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.StandardWatchEventKinds;
import java8.nio.file.WatchEvent;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.provider.FileSystemProviders;
import me.zhanghai.android.files.provider.common.AbstractWatchService;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.linux.syscall.Constants;
import me.zhanghai.android.files.provider.linux.syscall.StructInotifyEvent;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;
import me.zhanghai.java.promise.Promise;
import me.zhanghai.java.promise.Settler;

class LocalLinuxWatchService extends AbstractWatchService {

    @NonNull
    private final Poller mPoller;

    LocalLinuxWatchService() throws IOException {
        mPoller = new Poller(this);
        mPoller.start();
    }

    @NonNull
    LocalLinuxWatchKey register(@NonNull LinuxPath path, @NonNull WatchEvent.Kind<?>[] kinds,
                                @NonNull WatchEvent.Modifier... modifiers) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(kinds);
        Objects.requireNonNull(modifiers);
        Set<WatchEvent.Kind<?>> kindSet = new HashSet<>();
        for (WatchEvent.Kind<?> kind : kinds) {
            Objects.requireNonNull(kind);
            if (kind == StandardWatchEventKinds.ENTRY_CREATE
                    || kind == StandardWatchEventKinds.ENTRY_DELETE
                    || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                kindSet.add(kind);
            } else if (kind == StandardWatchEventKinds.OVERFLOW) {
                // Ignored.
            } else {
                throw new UnsupportedOperationException(kind.name());
            }
        }
        for (WatchEvent.Modifier modifier : modifiers) {
            Objects.requireNonNull(modifier);
            throw new UnsupportedOperationException(modifier.name());
        }
        return mPoller.register(path, kindSet);
    }

    void cancel(@NonNull LocalLinuxWatchKey key) {
        mPoller.cancel(key);
    }

    @Override
    protected void onClose() throws IOException {
        mPoller.close();
    }

    private static class Poller extends Thread implements Closeable {

        private static final byte[] ONE_BYTE = new byte[1];

        @NonNull
        private static final AtomicInteger sId = new AtomicInteger();

        @NonNull
        private LocalLinuxWatchService mWatchService;

        @NonNull
        private final FileDescriptor[] mSocketFds;

        @NonNull
        private final FileDescriptor mInotifyFd;

        @NonNull
        private final Map<Integer, LocalLinuxWatchKey> mKeys = new HashMap<>();

        private final byte[] mInotifyBuffer = new byte[4096];

        @NonNull
        private final Queue<Runnable> mRunnables = new LinkedList<>();

        private boolean mClosed;

        @NonNull
        private final Object mLock = new Object();

        Poller(@NonNull LocalLinuxWatchService watchService) throws IOException {
            super("LocalLinuxWatchService.Poller-" + sId.getAndIncrement());

            mWatchService = watchService;

            setDaemon(true);

            try {
                mSocketFds = Syscalls.socketpair(OsConstants.AF_UNIX, OsConstants.SOCK_STREAM, 0);
                int flags = Syscalls.fcntl(mSocketFds[0], OsConstants.F_GETFL);
                if ((flags & OsConstants.O_NONBLOCK) != OsConstants.O_NONBLOCK) {
                    Syscalls.fcntl(mSocketFds[0], OsConstants.F_SETFL, flags
                            | OsConstants.O_NONBLOCK);
                }
                mInotifyFd = Syscalls.inotify_init1(OsConstants.O_NONBLOCK);
            } catch (SyscallException e) {
                throw e.toFileSystemException(null);
            }
        }

        @NonNull
        LocalLinuxWatchKey register(@NonNull LinuxPath path, @NonNull Set<WatchEvent.Kind<?>> kinds)
                throws IOException {
            return awaitPromise(new Promise<>(settler -> post(true, settler, () -> {
                try {
                    ByteString pathBytes = path.toByteString();
                    int mask = eventKindsToMask(kinds);
                    mask = maybeAddDontFollowMask(path, mask);
                    int wd;
                    try {
                        wd = Syscalls.inotify_add_watch(mInotifyFd, pathBytes, mask);
                    } catch (SyscallException e) {
                        settler.reject(e.toFileSystemException(pathBytes.toString()));
                        return;
                    }
                    LocalLinuxWatchKey key = new LocalLinuxWatchKey(mWatchService, path, wd);
                    mKeys.put(wd, key);
                    settler.resolve(key);
                } catch (RuntimeException e) {
                    settler.reject(e);
                }
            })));
        }

        private static int maybeAddDontFollowMask(@NonNull Path path, int mask) {
            BasicFileAttributes attributes = null;
            try {
                attributes = Files.readAttributes(path, BasicFileAttributes.class);
            } catch (IOException ignored) {}
            if (attributes == null) {
                try {
                    attributes = Files.readAttributes(path, BasicFileAttributes.class,
                            LinkOption.NOFOLLOW_LINKS);
                } catch (IOException ignored) {}
            }
            if (attributes != null && attributes.isSymbolicLink()) {
                return mask | Constants.IN_DONT_FOLLOW;
            }
            return mask;
        }

        void cancel(@NonNull LocalLinuxWatchKey key) {
            try {
                new Promise<Void>(settler -> post(true, settler, () -> {
                    if (key.isValid()) {
                        int wd = key.getWatchDescriptor();
                        try {
                            Syscalls.inotify_rm_watch(mInotifyFd, wd);
                        } catch (SyscallException e) {
                            // Ignored.
                            e.toFileSystemException(key.watchable().toString()).printStackTrace();
                        }
                        key.setInvalid();
                        mKeys.remove(wd);
                    }
                    settler.resolve(null);
                })).await();
            } catch (ExecutionException | InterruptedException e) {
                // Ignored.
                e.printStackTrace();
            }
        }

        @Override
        public void close() throws IOException {
            awaitPromise(new Promise<>(settler -> post(false, settler, () -> {
                try {
                    for (LocalLinuxWatchKey key : mKeys.values()) {
                        int wd = key.getWatchDescriptor();
                        try {
                            Syscalls.inotify_rm_watch(mInotifyFd, wd);
                        } catch (SyscallException e) {
                            settler.reject(e.toFileSystemException(key.watchable().toString()));
                            return;
                        }
                        key.setInvalid();
                    }
                    mKeys.clear();
                    try {
                        Syscalls.close(mInotifyFd);
                        Syscalls.close(mSocketFds[1]);
                        Syscalls.close(mSocketFds[0]);
                    } catch (SyscallException e) {
                        // Ignored.
                        e.printStackTrace();
                    }
                    mClosed = true;
                    settler.resolve(null);
                } catch (RuntimeException e) {
                    settler.reject(e);
                }
            })));
        }

        private void post(boolean ensureOpen, @NonNull Settler<?> settler,
                          @NonNull Runnable runnable) {
            synchronized (mLock) {
                mRunnables.offer(() -> {
                    if (mClosed) {
                        if (ensureOpen) {
                            settler.reject(new ClosedWatchServiceException());
                        }
                        return;
                    }
                    runnable.run();
                });
            }
            try {
                Syscalls.write(mSocketFds[1], ONE_BYTE);
            } catch (InterruptedIOException e) {
                settler.reject(e);
            } catch (SyscallException e) {
                settler.reject(e.toFileSystemException(null));
            }
        }

        private <T> T awaitPromise(@NonNull Promise<T> promise) throws IOException {
            try {
                return promise.await();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new AssertionError(cause);
                }
            } catch (InterruptedException e) {
                InterruptedIOException exception = new InterruptedIOException();
                exception.initCause(e);
                throw exception;
            }
        }

        @Override
        public void run() {
            StructPollfd[] fds = new StructPollfd[] {
                    createStructPollFd(mSocketFds[0]),
                    createStructPollFd(mInotifyFd)
            };
            try {
                while (true) {
                    fds[0].revents = 0;
                    fds[1].revents = 0;
                    Syscalls.poll(fds, -1);
                    if ((fds[0].revents & OsConstants.POLLIN) == OsConstants.POLLIN) {
                        int size = 0;
                        try {
                            size = Syscalls.read(mSocketFds[0], ONE_BYTE);
                        } catch (SyscallException e) {
                            if (e.getErrno() != OsConstants.EAGAIN) {
                                throw e;
                            }
                        }
                        if (size > 0) {
                            synchronized (mLock) {
                                Runnable runnable;
                                while ((runnable = mRunnables.poll()) != null) {
                                    runnable.run();
                                }
                            }
                            if (mClosed) {
                                break;
                            }
                        }
                    }
                    if ((fds[1].revents & OsConstants.POLLIN) == OsConstants.POLLIN) {
                        int size = 0;
                        try {
                            size = Syscalls.read(mInotifyFd, mInotifyBuffer);
                        } catch (SyscallException e) {
                            if (e.getErrno() != OsConstants.EAGAIN) {
                                throw e;
                            }
                        }
                        if (size > 0) {
                            if (FileSystemProviders.shouldOverflowWatchEvents()) {
                                for (LocalLinuxWatchKey key : mKeys.values()) {
                                    key.addEvent(StandardWatchEventKinds.OVERFLOW, null);
                                }
                                continue;
                            }
                            StructInotifyEvent[] events;
                            events = Syscalls.inotify_get_events(mInotifyBuffer, 0, size);
                            for (StructInotifyEvent event : events) {
                                if ((event.mask & Constants.IN_Q_OVERFLOW)
                                        == Constants.IN_Q_OVERFLOW) {
                                    for (LocalLinuxWatchKey key : mKeys.values()) {
                                        key.addEvent(StandardWatchEventKinds.OVERFLOW, null);
                                    }
                                    break;
                                }
                                LocalLinuxWatchKey key = mKeys.get(event.wd);
                                if ((event.mask & Constants.IN_IGNORED) == Constants.IN_IGNORED) {
                                    key.setInvalid();
                                    key.signal();
                                    mKeys.remove(event.wd);
                                } else {
                                    WatchEvent.Kind<Path> kind = maskToEventKind(event.mask);
                                    LinuxPath name = event.name != null ?
                                            key.watchable().getFileSystem().getPath(event.name)
                                            : null;
                                    key.addEvent(kind, name);
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedIOException | SyscallException e) {
                e.printStackTrace();
            }
        }

        @NonNull
        private static StructPollfd createStructPollFd(@NonNull FileDescriptor fd) {
            StructPollfd structPollfd = new StructPollfd();
            structPollfd.fd = fd;
            structPollfd.events = (short) OsConstants.POLLIN;
            return structPollfd;
        }

        private static int eventKindsToMask(@NonNull Set<WatchEvent.Kind<?>> kinds) {
            int mask = 0;
            for (WatchEvent.Kind<?> kind: kinds) {
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    mask |= Constants.IN_CREATE | Constants.IN_MOVED_TO;
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    mask |= Constants.IN_DELETE_SELF | Constants.IN_DELETE
                            | Constants.IN_MOVED_FROM;
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    mask |= Constants.IN_MOVE_SELF | Constants.IN_MODIFY | Constants.IN_ATTRIB;
                }
            }
            return mask;
        }

        @NonNull
        private static WatchEvent.Kind<Path> maskToEventKind(int mask) {
            if ((mask & Constants.IN_CREATE) == Constants.IN_CREATE
                    || (mask & Constants.IN_MOVED_TO) == Constants.IN_MOVED_TO) {
                return StandardWatchEventKinds.ENTRY_CREATE;
            } else if ((mask & Constants.IN_DELETE_SELF) == Constants.IN_DELETE_SELF
                    || (mask & Constants.IN_DELETE) == Constants.IN_DELETE
                    || (mask & Constants.IN_MOVED_FROM) == Constants.IN_MOVED_FROM) {
                return StandardWatchEventKinds.ENTRY_DELETE;
            } else if ((mask & Constants.IN_MOVE_SELF) == Constants.IN_MOVE_SELF
                    || (mask & Constants.IN_MODIFY) == Constants.IN_MODIFY
                    || (mask & Constants.IN_ATTRIB) == Constants.IN_ATTRIB) {
                return StandardWatchEventKinds.ENTRY_MODIFY;
            } else {
                throw new AssertionError(mask);
            }
        }
    }
}
