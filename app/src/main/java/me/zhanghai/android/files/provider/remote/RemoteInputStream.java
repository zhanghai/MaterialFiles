/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RemoteInputStream extends InputStream implements Parcelable {

    @Nullable
    private final InputStream mLocalInputStream;

    @Nullable
    private final IRemoteInputStream mRemoteInputStream;

    public RemoteInputStream(@NonNull InputStream inputStream) {
        mLocalInputStream = inputStream;
        mRemoteInputStream = null;
    }

    @Override
    public int read() throws IOException {
        if (mRemoteInputStream != null) {
            ParcelableException exception = new ParcelableException();
            int data;
            try {
                data = mRemoteInputStream.read(exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
            return data;
        } else {
            return mLocalInputStream.read();
        }
    }

    @Override
    public int read(@NonNull byte[] buffer, int offset, int length) throws IOException {
        if (mRemoteInputStream != null) {
            byte[] remoteBuffer = new byte[length];
            ParcelableException exception = new ParcelableException();
            int size;
            try {
                size = mRemoteInputStream.read2(remoteBuffer, exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
            if (size > 0) {
                System.arraycopy(remoteBuffer, 0, buffer, offset, size);
            }
            return size;
        } else {
            return mLocalInputStream.read(buffer, offset, length);
        }
    }

    @Override
    public long skip(long size) throws IOException {
        if (mRemoteInputStream != null) {
            ParcelableException exception = new ParcelableException();
            long skippedSize;
            try {
                skippedSize = mRemoteInputStream.skip(size, exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
            return skippedSize;
        } else {
            return mLocalInputStream.skip(size);
        }
    }

    @Override
    public int available() throws IOException {
        if (mRemoteInputStream != null) {
            ParcelableException exception = new ParcelableException();
            int size;
            try {
                size = mRemoteInputStream.available(exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
            return size;
        } else {
            return mLocalInputStream.available();
        }
    }

    @Override
    public void close() throws IOException {
        if (mRemoteInputStream != null) {
            ParcelableException exception = new ParcelableException();
            try {
                mRemoteInputStream.close(exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
        } else {
            mLocalInputStream.close();
        }
    }

    private static class Stub extends IRemoteInputStream.Stub {

        @NonNull
        private final InputStream mInputStream;

        public Stub(@NonNull InputStream inputStream) {
            mInputStream = inputStream;
        }

        @Override
        public int read(@NonNull ParcelableException exception) {
            int data;
            try {
                data = mInputStream.read();
            } catch (IOException | RuntimeException e) {
                exception.set(e);
                return 0;
            }
            return data;
        }

        @Override
        public int read2(@NonNull byte[] buffer, @NonNull ParcelableException exception) {
            int size;
            try {
                size = mInputStream.read(buffer);
            } catch (IOException | RuntimeException e) {
                exception.set(e);
                return 0;
            }
            return size;
        }

        @Override
        public long skip(long size, @NonNull ParcelableException exception) {
            long skippedSize;
            try {
                skippedSize = mInputStream.skip(size);
            } catch (IOException | RuntimeException e) {
                exception.set(e);
                return 0;
            }
            return skippedSize;
        }

        @Override
        public int available(@NonNull ParcelableException exception) {
            int size;
            try {
                size = mInputStream.available();
            } catch (IOException | RuntimeException e) {
                exception.set(e);
                return 0;
            }
            return size;
        }

        @Override
        public void close(@NonNull ParcelableException exception) {
            try {
                mInputStream.close();
            } catch (IOException | RuntimeException e) {
                exception.set(e);
            }
        }
    }


    public static final Creator<RemoteInputStream> CREATOR = new Creator<RemoteInputStream>() {
        @Override
        public RemoteInputStream createFromParcel(Parcel source) {
            return new RemoteInputStream(source);
        }
        @Override
        public RemoteInputStream[] newArray(int size) {
            return new RemoteInputStream[size];
        }
    };

    protected RemoteInputStream(Parcel in) {
        mLocalInputStream = null;
        mRemoteInputStream = IRemoteInputStream.Stub.asInterface(in.readStrongBinder());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mRemoteInputStream != null) {
            dest.writeStrongBinder(mRemoteInputStream.asBinder());
        } else {
            dest.writeStrongBinder(new Stub(mLocalInputStream).asBinder());
        }
    }
}
