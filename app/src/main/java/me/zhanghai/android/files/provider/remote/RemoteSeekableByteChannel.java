/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.io.IOException;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.channels.SeekableByteChannel;
import me.zhanghai.android.files.provider.common.ForceableChannel;

public class RemoteSeekableByteChannel implements ForceableChannel, SeekableByteChannel,
        Parcelable {

    @Nullable
    private final SeekableByteChannel mLocalChannel;

    @Nullable
    private final IRemoteSeekableByteChannel mRemoteChannel;

    private volatile boolean mRemoteClosed;

    public RemoteSeekableByteChannel(@NonNull SeekableByteChannel channel) {
        mLocalChannel = channel;
        mRemoteChannel = null;
    }

    @Override
    public int read(@NonNull ByteBuffer destination) throws IOException {
        if (mRemoteChannel != null) {
            byte[] destinationBytes = new byte[destination.remaining()];
            ParcelableException exception = new ParcelableException();
            int size;
            try {
                size = mRemoteChannel.read(destinationBytes, exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
            if (size > 0) {
                destination.put(destinationBytes, 0, size);
            }
            return size;
        } else {
            return mLocalChannel.read(destination);
        }
    }

    @Override
    public int write(@NonNull ByteBuffer source) throws IOException {
        if (mRemoteChannel != null) {
            int oldPosition = source.position();
            byte[] sourceBytes = new byte[source.remaining()];
            source.get(sourceBytes);
            source.position(oldPosition);
            ParcelableException exception = new ParcelableException();
            int size;
            try {
                size = mRemoteChannel.write(sourceBytes, exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
            source.position(oldPosition + size);
            return size;
        } else {
            return mLocalChannel.write(source);
        }
    }

    @Override
    public long position() throws IOException {
        if (mRemoteChannel != null) {
            ParcelableException exception = new ParcelableException();
            long position;
            try {
                position = mRemoteChannel.position(exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
            return position;
        } else {
            return mLocalChannel.position();
        }
    }

    @NonNull
    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (mRemoteChannel != null) {
            ParcelableException exception = new ParcelableException();
            try {
                mRemoteChannel.position2(newPosition, exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
        } else {
            mLocalChannel.position(newPosition);
        }
        return this;
    }

    @Override
    public long size() throws IOException {
        if (mRemoteChannel != null) {
            ParcelableException exception = new ParcelableException();
            long size;
            try {
                size = mRemoteChannel.size(exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
            return size;
        } else {
            return mLocalChannel.size();
        }
    }

    @NonNull
    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        if (mRemoteChannel != null) {
            ParcelableException exception = new ParcelableException();
            try {
                mRemoteChannel.truncate(size, exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
        } else {
            return mLocalChannel.truncate(size);
        }
        return this;
    }

    @Override
    public void force(boolean metaData) throws IOException {
        if (mRemoteChannel != null) {
            ParcelableException exception = new ParcelableException();
            try {
                mRemoteChannel.force(metaData, exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
        } else {
            ForceableChannel.force(mLocalChannel, metaData);
        }
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
            ParcelableException exception = new ParcelableException();
            try {
                mRemoteChannel.close(exception);
            } catch (RemoteException e) {
                throw new RemoteFileSystemException(e);
            }
            exception.throwIfNotNull();
            mRemoteClosed = true;
        } else {
            mLocalChannel.close();
        }
    }

    private static class Stub extends IRemoteSeekableByteChannel.Stub {

        @NonNull
        private final SeekableByteChannel mChannel;

        public Stub(@NonNull SeekableByteChannel channel) {
            mChannel = channel;
        }

        @Override
        public int read(@NonNull byte[] destination, @NonNull ParcelableException exception) {
            int size;
            try {
                ByteBuffer buffer = ByteBuffer.wrap(destination);
                size = mChannel.read(buffer);
            } catch (IOException | RuntimeException e) {
                exception.set(e);
                return 0;
            }
            return size;
        }

        @Override
        public int write(@NonNull byte[] source, @NonNull ParcelableException exception) {
            int size;
            try {
                ByteBuffer buffer = ByteBuffer.wrap(source);
                size = mChannel.write(buffer);
            } catch (IOException | RuntimeException e) {
                exception.set(e);
                return 0;
            }
            return size;
        }

        @Override
        public long position(@NonNull ParcelableException exception) {
            long position;
            try {
                position = mChannel.position();
            } catch (IOException | RuntimeException e) {
                exception.set(e);
                return 0;
            }
            return position;
        }

        @Override
        public void position2(long newPosition, @NonNull ParcelableException exception) {
            try {
                mChannel.position(newPosition);
            } catch (IOException | RuntimeException e) {
                exception.set(e);
            }
        }

        @Override
        public long size(@NonNull ParcelableException exception) {
            long size;
            try {
                size = mChannel.size();
            } catch (IOException | RuntimeException e) {
                exception.set(e);
                return 0;
            }
            return size;
        }

        @Override
        public void truncate(long size, @NonNull ParcelableException exception) {
            try {
                mChannel.truncate(size);
            } catch (IOException | RuntimeException e) {
                exception.set(e);
            }
        }

        @Override
        public void force(boolean metaData, @NonNull ParcelableException exception) {
            try {
                ForceableChannel.force(mChannel, metaData);
            } catch (IOException | RuntimeException e) {
                exception.set(e);
            }
        }

        @Override
        public void close(@NonNull ParcelableException exception) {
            try {
                mChannel.close();
            } catch (IOException | RuntimeException e) {
                exception.set(e);
            }
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
        mRemoteChannel = IRemoteSeekableByteChannel.Stub.asInterface(in.readStrongBinder());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mRemoteChannel != null) {
            dest.writeStrongBinder(mRemoteChannel.asBinder());
        } else {
            dest.writeStrongBinder(new Stub(mLocalChannel).asBinder());
        }
    }
}
