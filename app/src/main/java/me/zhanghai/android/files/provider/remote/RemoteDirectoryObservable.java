/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.provider.common.DirectoryObservable;
import me.zhanghai.android.files.util.RemoteCallback;

public class RemoteDirectoryObservable implements DirectoryObservable, Parcelable {

    @Nullable
    private final DirectoryObservable mLocalDirectoryObservable;

    @Nullable
    private final IRemoteDirectoryObservable mRemoteDirectoryObservable;

    private final Set<Runnable> mRemoteObservers;

    private boolean mRemoteInitialized;

    private final Object mRemoteLock;

    public RemoteDirectoryObservable(@NonNull DirectoryObservable directoryObservable) {
        mLocalDirectoryObservable = directoryObservable;
        mRemoteDirectoryObservable = null;
        mRemoteObservers = null;
        mRemoteLock = null;
    }

    public void initForRemote() throws IOException {
        synchronized (mRemoteLock) {
            if (mRemoteInitialized) {
                throw new IllegalStateException();
            }
            try {
                mRemoteDirectoryObservable.addObserver(new RemoteCallback(result -> {
                    synchronized (mRemoteLock) {
                        for (Runnable observer : mRemoteObservers) {
                            observer.run();
                        }
                    }
                }));
            } catch (RemoteException e) {
                close();
                throw new RemoteFileSystemException(e);
            }
            mRemoteInitialized = true;
        }
    }

    @Override
    public void addObserver(@NonNull Runnable observer) {
        Objects.requireNonNull(observer);
        synchronized (mRemoteLock) {
            if (!mRemoteInitialized) {
                throw new IllegalStateException();
            }
            mRemoteObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(@NonNull Runnable observer) {
        Objects.requireNonNull(observer);
        synchronized (mRemoteLock) {
            if (!mRemoteInitialized) {
                throw new IllegalStateException();
            }
            mRemoteObservers.remove(observer);
        }
    }

    @Override
    public void close() throws IOException {
        if (mRemoteDirectoryObservable != null) {
            synchronized (mRemoteLock) {
                ParcelableException exception = new ParcelableException();
                try {
                    mRemoteDirectoryObservable.close(exception);
                } catch (RemoteException e) {
                    throw new RemoteFileSystemException(e);
                }
                exception.throwIfNotNull();
                mRemoteObservers.clear();
            }
        } else {
            mLocalDirectoryObservable.close();
        }
    }

    private static class Stub extends IRemoteDirectoryObservable.Stub {

        @NonNull
        private final DirectoryObservable mDirectoryObservable;

        public Stub(@NonNull DirectoryObservable directoryObservable) {
            mDirectoryObservable = directoryObservable;
        }

        @Override
        public void addObserver(@NonNull RemoteCallback observer) {
            Objects.requireNonNull(observer);
            mDirectoryObservable.addObserver(() -> observer.sendResult(null));
        }

        @Override
        public void close(@NonNull ParcelableException exception) {
            try {
                mDirectoryObservable.close();
            } catch (IOException | RuntimeException e) {
                exception.set(e);
            }
        }
    }


    public static final Creator<RemoteDirectoryObservable> CREATOR =
            new Creator<RemoteDirectoryObservable>() {
                @Override
                public RemoteDirectoryObservable createFromParcel(Parcel source) {
                    return new RemoteDirectoryObservable(source);
                }
                @Override
                public RemoteDirectoryObservable[] newArray(int size) {
                    return new RemoteDirectoryObservable[size];
                }
            };

    protected RemoteDirectoryObservable(Parcel in) {
        mLocalDirectoryObservable = null;
        mRemoteDirectoryObservable = IRemoteDirectoryObservable.Stub.asInterface(
                in.readStrongBinder());
        mRemoteObservers = new HashSet<>();
        mRemoteLock = new Object();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mRemoteDirectoryObservable != null) {
            throw new IllegalStateException("Already at the remote side");
        }
        dest.writeStrongBinder(new Stub(mLocalDirectoryObservable).asBinder());
    }
}
