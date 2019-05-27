/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.RemoteException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.channels.SeekableByteChannel;

public class RemoteSeekableByteChannel implements SeekableByteChannel, Parcelable {

    @Nullable
    private final SeekableByteChannel mLocalChannel;
    @Nullable
    private final ParcelFileDescriptor[] mLocalReadPipe;
    @Nullable
    private final ParcelFileDescriptor[] mLocalWritePipe;

    @Nullable
    private final IRemoteSeekableByteChannel mRemoteChannel;
    @Nullable
    private final ParcelFileDescriptor mRemoteReadFd;
    @Nullable
    private final ParcelFileDescriptor mRemoteWriteFd;

    @NonNull
    private final Object mRemoteLock = new Object();

    private volatile boolean mRemoteClosed;

    public RemoteSeekableByteChannel(@NonNull SeekableByteChannel channel) throws IOException {
        mLocalChannel = channel;
        mLocalReadPipe = ParcelFileDescriptor.createPipe();
        mLocalWritePipe = ParcelFileDescriptor.createPipe();
        mRemoteChannel = null;
        mRemoteReadFd = null;
        mRemoteWriteFd = null;
    }

    @Override
    public int read(@NonNull ByteBuffer destination) throws IOException {
        if (mRemoteChannel != null) {
            synchronized (mRemoteLock) {
                int remaining = destination.remaining();
                ParcelableException exception = new ParcelableException();
                int size;
                try {
                    size = mRemoteChannel.read(remaining, exception);
                } catch (RemoteException e) {
                    throw new RemoteFileSystemException(e);
                }
                exception.throwIfNotNull();
                int oldLimit = destination.limit();
                destination.limit(destination.position() + size);
                try {
                    readFromPipe(mRemoteReadFd, destination);
                } finally {
                    destination.limit(oldLimit);
                }
                return size;
            }
        } else {
            return mLocalChannel.read(destination);
        }
    }

    @Override
    public int write(@NonNull ByteBuffer source) throws IOException {
        if (mRemoteChannel != null) {
            synchronized (mRemoteLock) {
                int remaining = source.remaining();
                writeToPipe(mRemoteWriteFd, source);
                ParcelableException exception = new ParcelableException();
                int size;
                try {
                    size = mRemoteChannel.write(remaining, exception);
                } catch (RemoteException e) {
                    throw new RemoteFileSystemException(e);
                }
                exception.throwIfNotNull();
                return size;
            }
        } else {
            return mLocalChannel.write(source);
        }
    }

    @Override
    public long position() throws IOException {
        if (mRemoteChannel != null) {
            synchronized (mRemoteLock) {
                ParcelableException exception = new ParcelableException();
                long position;
                try {
                    position = mRemoteChannel.position(exception);
                } catch (RemoteException e) {
                    throw new RemoteFileSystemException(e);
                }
                exception.throwIfNotNull();
                return position;
            }
        } else {
            return mLocalChannel.position();
        }
    }

    @NonNull
    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (mRemoteChannel != null) {
            synchronized (mRemoteLock) {
                ParcelableException exception = new ParcelableException();
                try {
                    mRemoteChannel.position2(newPosition, exception);
                } catch (RemoteException e) {
                    throw new RemoteFileSystemException(e);
                }
                exception.throwIfNotNull();
            }
        } else {
            mLocalChannel.position(newPosition);
        }
        return this;
    }

    @Override
    public long size() throws IOException {
        if (mRemoteChannel != null) {
            synchronized (mRemoteLock) {
                ParcelableException exception = new ParcelableException();
                long size;
                try {
                    size = mRemoteChannel.size(exception);
                } catch (RemoteException e) {
                    throw new RemoteFileSystemException(e);
                }
                exception.throwIfNotNull();
                return size;
            }
        } else {
            return mLocalChannel.size();
        }
    }

    @NonNull
    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        if (mRemoteChannel != null) {
            synchronized (mRemoteLock) {
                ParcelableException exception = new ParcelableException();
                try {
                    mRemoteChannel.truncate(size, exception);
                } catch (RemoteException e) {
                    throw new RemoteFileSystemException(e);
                }
                exception.throwIfNotNull();
            }
        } else {
            return mLocalChannel.truncate(size);
        }
        return this;
    }

    @Override
    public boolean isOpen() {
        if (mRemoteChannel != null) {
            return !mRemoteClosed;
        } else {
            return mLocalChannel.isOpen();
        }
    }

    @Override
    public void close() throws IOException {
        if (mRemoteChannel != null) {
            synchronized (mRemoteLock) {
                mRemoteReadFd.close();
                mRemoteWriteFd.close();
                ParcelableException exception = new ParcelableException();
                try {
                    mRemoteChannel.close(exception);
                } catch (RemoteException e) {
                    throw new RemoteFileSystemException(e);
                }
                exception.throwIfNotNull();
                mRemoteClosed = true;
            }
        } else {
            mLocalChannel.close();
        }
    }

    private static class Stub extends IRemoteSeekableByteChannel.Stub {

        @NonNull
        private final SeekableByteChannel mChannel;
        @NonNull
        private final ParcelFileDescriptor mReadFd;
        @NonNull
        private final ParcelFileDescriptor mWriteFd;

        @NonNull
        private final Object mLock = new Object();

        public Stub(@NonNull SeekableByteChannel channel, @NonNull ParcelFileDescriptor readFd,
                    @NonNull ParcelFileDescriptor writeFd) {
            mChannel = channel;
            mReadFd = readFd;
            mWriteFd = writeFd;
        }

        @Override
        public int read(int remaining, @NonNull ParcelableException exception) {
            synchronized (mLock) {
                int size;
                try {
                    ByteBuffer buffer = ByteBuffer.allocateDirect(remaining);
                    size = mChannel.read(buffer);
                    if (size > 0) {
                        buffer.flip();
                        writeToPipe(mReadFd, buffer);
                    }
                } catch (IOException | RuntimeException e) {
                    exception.set(e);
                    return 0;
                }
                return size;
            }
        }

        @Override
        public int write(int remaining, @NonNull ParcelableException exception) {
            synchronized (mLock) {
                int size;
                try {
                    ByteBuffer buffer = ByteBuffer.allocateDirect(remaining);
                    readFromPipe(mWriteFd, buffer);
                    buffer.flip();
                    size = mChannel.write(buffer);
                } catch (IOException | RuntimeException e) {
                    exception.set(e);
                    return 0;
                }
                return size;
            }
        }

        @Override
        public long position(@NonNull ParcelableException exception) {
            synchronized (mLock) {
                long position;
                try {
                    position = mChannel.position();
                } catch (IOException | RuntimeException e) {
                    exception.set(e);
                    return 0;
                }
                return position;
            }
        }

        @Override
        public void position2(long newPosition, @NonNull ParcelableException exception) {
            synchronized (mLock) {
                try {
                    mChannel.position(newPosition);
                } catch (IOException | RuntimeException e) {
                    exception.set(e);
                }
            }
        }

        @Override
        public long size(@NonNull ParcelableException exception) {
            synchronized (mLock) {
                long size;
                try {
                    size = mChannel.size();
                } catch (IOException | RuntimeException e) {
                    exception.set(e);
                    return 0;
                }
                return size;
            }
        }

        @Override
        public void truncate(long size, @NonNull ParcelableException exception) {
            synchronized (mLock) {
                try {
                    mChannel.truncate(size);
                } catch (IOException | RuntimeException e) {
                    exception.set(e);
                }
            }
        }

        @Override
        public void close(@NonNull ParcelableException exception) {
            synchronized (mLock) {
                try {
                    mReadFd.close();
                    mWriteFd.close();
                    mChannel.close();
                } catch (IOException | RuntimeException e) {
                    exception.set(e);
                }
            }
        }
    }

    private static void readFromPipe(@NonNull ParcelFileDescriptor pfd, @NonNull ByteBuffer buffer)
            throws IOException {
        try (FileInputStream inputStream = new FileInputStream(pfd.getFileDescriptor())) {
            try (FileChannel channel = inputStream.getChannel()) {
                channel.read(buffer);
            }
        }
    }

    private static void writeToPipe(@NonNull ParcelFileDescriptor pfd, @NonNull ByteBuffer buffer)
            throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor())) {
            try (FileChannel channel = outputStream.getChannel()) {
                channel.write(buffer);
                channel.force(false);
            }
            outputStream.flush();
        }
    }


    public static final Creator<RemoteSeekableByteChannel> CREATOR =
            new Creator<RemoteSeekableByteChannel>() {
                @Override
                public RemoteSeekableByteChannel createFromParcel(Parcel source) {
                    return new RemoteSeekableByteChannel(source);
                }
                @Override
                public RemoteSeekableByteChannel[] newArray(int size) {
                    return new RemoteSeekableByteChannel[size];
                }
            };

    protected RemoteSeekableByteChannel(Parcel in) {
        mLocalChannel = null;
        mLocalReadPipe = null;
        mLocalWritePipe = null;
        mRemoteChannel = IRemoteSeekableByteChannel.Stub.asInterface(in.readStrongBinder());
        mRemoteReadFd = in.readFileDescriptor();
        mRemoteWriteFd = in.readFileDescriptor();
    }

    @Override
    public int describeContents() {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mRemoteChannel != null) {
            dest.writeStrongBinder(mRemoteChannel.asBinder());
            mRemoteReadFd.writeToParcel(dest, flags);
            mRemoteWriteFd.writeToParcel(dest, flags);
        } else {
            dest.writeStrongBinder(new Stub(mLocalChannel, mLocalReadPipe[1], mLocalWritePipe[0])
                    .asBinder());
            mLocalReadPipe[0].writeToParcel(dest, flags);
            mLocalWritePipe[1].writeToParcel(dest, flags);
        }
    }
}
