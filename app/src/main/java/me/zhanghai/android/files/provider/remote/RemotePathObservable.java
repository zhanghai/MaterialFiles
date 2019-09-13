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
import me.zhanghai.android.files.provider.common.PathObservable;
import me.zhanghai.android.files.util.RemoteCallback;

public class RemotePathObservable implements PathObservable, Parcelable {

    @Nullable
    private final PathObservable mLocalPathObservable;

    @Nullable
    private final IRemotePathObservable mRemotePathObservable;

    private final Set<Runnable> mRemoteObservers;

    private boolean mRemoteInitialized;

    private final Object mRemoteLock;

    public RemotePathObservable(@NonNull PathObservable pathObservable) {
        mLocalPathObservable = pathObservable;
        mRemotePathObservable = null;
        mRemoteObservers = null;
        mRemoteLock = null;
    }

    public void initForRemote() throws IOException {
        synchronized (mRemoteLock) {
            if (mRemoteInitialized) {
                throw new IllegalStateException();
            }
            try {
                mRemotePathObservable.addObserver(new RemoteCallback(result -> {
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
        if (mRemotePathObservable != null) {
            synchronized (mRemoteLock) {
                ParcelableException exception = new ParcelableException();
                try {
                    mRemotePathObservable.close(exception);
                } catch (RemoteException e) {
                    throw new RemoteFileSystemException(e);
                }
                exception.throwIfNotNull();
                mRemoteObservers.clear();
            }
        } else {
            mLocalPathObservable.close();
        }
    }

    private static class Stub extends IRemotePathObservable.Stub {

        @NonNull
        private final PathObservable mPathObservable;

        public Stub(@NonNull PathObservable pathObservable) {
            mPathObservable = pathObservable;
        }

        @Override
        public void addObserver(@NonNull RemoteCallback observer) {
            Objects.requireNonNull(observer);
            mPathObservable.addObserver(() -> observer.sendResult(null));
        }

        @Override
        public void close(@NonNull ParcelableException exception) {
            try {
                mPathObservable.close();
            } catch (IOException | RuntimeException e) {
                exception.set(e);
            }
        }
    }


    public static final Creator<RemotePathObservable> CREATOR =
            new Creator<RemotePathObservable>() {
                @Override
                public RemotePathObservable createFromParcel(Parcel source) {
                    return new RemotePathObservable(source);
                }
                @Override
                public RemotePathObservable[] newArray(int size) {
                    return new RemotePathObservable[size];
                }
            };

    protected RemotePathObservable(Parcel in) {
        mLocalPathObservable = null;
        mRemotePathObservable = IRemotePathObservable.Stub.asInterface(
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
        if (mRemotePathObservable != null) {
            throw new IllegalStateException("Already at the remote side");
        }
        dest.writeStrongBinder(new Stub(mLocalPathObservable).asBinder());
    }
}
