/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class RemoteCallback implements Parcelable {

    @Nullable
    private final Listener mListener;
    @Nullable
    private final IRemoteCallback mRemoteListener;

    public RemoteCallback(@NonNull Listener listener) {
        mListener = listener;
        mRemoteListener = null;
    }

    public void sendResult(Bundle result) {
        if (mRemoteListener != null) {
            try {
                mRemoteListener.sendResult(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            mListener.onResult(result);
        }
    }

    public interface Listener {
        void onResult(Bundle result);
    }

    private class Stub extends IRemoteCallback.Stub {

        @Override
        public void sendResult(Bundle result) {
            RemoteCallback.this.sendResult(result);
        }
    }


    public static final Creator<RemoteCallback> CREATOR
            = new Creator<RemoteCallback>() {
        public RemoteCallback createFromParcel(Parcel parcel) {
            return new RemoteCallback(parcel);
        }
        public RemoteCallback[] newArray(int size) {
            return new RemoteCallback[size];
        }
    };

    private RemoteCallback(Parcel parcel) {
        mListener = null;
        mRemoteListener = IRemoteCallback.Stub.asInterface(parcel.readStrongBinder());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeStrongBinder(new Stub().asBinder());
    }
}
