/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileStore;
import java8.nio.file.attribute.FileStoreAttributeView;

public abstract class RemoteFileStore extends FileStore implements Parcelable {

    @NonNull
    private final String mName;
    @NonNull
    private final String mType;
    private final boolean mReadOnly;

    @NonNull
    private final IRemoteFileStore mRemoteInterface;

    public RemoteFileStore(@NonNull FileStore fileStore) {
        mName = fileStore.name();
        mType = fileStore.type();
        mReadOnly = fileStore.isReadOnly();

        mRemoteInterface = new RemoteFileStoreInterface(fileStore);
    }

    @NonNull
    @Override
    public String name() {
        return mName;
    }

    @NonNull
    @Override
    public String type() {
        return mType;
    }

    @Override
    public boolean isReadOnly() {
        return mReadOnly;
    }

    @Override
    public long getTotalSpace() throws IOException {
        ParcelableIoException ioException = new ParcelableIoException();
        long totalSpace;
        try {
            totalSpace = mRemoteInterface.getTotalSpace(ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
        return totalSpace;
    }

    @Override
    public long getUsableSpace() throws IOException {
        ParcelableIoException ioException = new ParcelableIoException();
        long usableSpace;
        try {
            usableSpace = mRemoteInterface.getUsableSpace(ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
        return usableSpace;
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        ParcelableIoException ioException = new ParcelableIoException();
        long unallocatedSpace;
        try {
            unallocatedSpace = mRemoteInterface.getUnallocatedSpace(ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
        return unallocatedSpace;
    }

    @Nullable
    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(@NonNull Class<V> type) {
        Objects.requireNonNull(type);
        return null;
    }

    @Nullable
    @Override
    public Object getAttribute(@NonNull String attribute) throws IOException {
        Objects.requireNonNull(attribute);
        throw new UnsupportedOperationException();
    }


    protected RemoteFileStore(Parcel in) {
        mName = in.readString();
        mType = in.readString();
        mReadOnly = in.readByte() != 0;
        mRemoteInterface = IRemoteFileStore.Stub.asInterface(in.readStrongBinder());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mType);
        dest.writeByte(mReadOnly ? (byte) 1 : (byte) 0);
        dest.writeStrongBinder(mRemoteInterface.asBinder());
    }
}
