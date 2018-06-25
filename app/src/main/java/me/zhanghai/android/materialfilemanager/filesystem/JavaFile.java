/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Parcel;
import android.os.Parcelable;

import org.threeten.bp.Instant;

import java.io.File;
import java.util.Objects;

public class JavaFile {

    public static Information loadInformation(File file) {
        Information information = new Information();
        information.canRead = file.canRead();
        information.canWrite = file.canWrite();
        information.exists = file.exists();
        information.isDirectory = file.isDirectory();
        information.isFile = file.isFile();
        information.isHidden = file.isHidden();
        information.lastModified = Instant.ofEpochMilli(file.lastModified());
        information.length = file.length();
        return information;
    }

    public static class Information implements Parcelable {

        public boolean canRead;
        public boolean canWrite;
        public boolean exists;
        public boolean isDirectory;
        public boolean isFile;
        public boolean isHidden;
        public Instant lastModified;
        public long length;

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            Information that = (Information) object;
            return canRead == that.canRead
                    && canWrite == that.canWrite
                    && exists == that.exists
                    && isDirectory == that.isDirectory
                    && isFile == that.isFile
                    && isHidden == that.isHidden
                    && Objects.equals(lastModified, that.lastModified)
                    && length == that.length;
        }

        @Override
        public int hashCode() {
            return Objects.hash(canRead, canWrite, exists, isDirectory, isFile, isHidden,
                    lastModified, length);
        }


        public static final Creator<Information> CREATOR = new Creator<Information>() {
            @Override
            public Information createFromParcel(Parcel source) {
                return new Information(source);
            }
            @Override
            public Information[] newArray(int size) {
                return new Information[size];
            }
        };

        public Information() {}

        protected Information(Parcel in) {
            canRead = in.readByte() != 0;
            canWrite = in.readByte() != 0;
            exists = in.readByte() != 0;
            isDirectory = in.readByte() != 0;
            isFile = in.readByte() != 0;
            isHidden = in.readByte() != 0;
            lastModified = (Instant) in.readSerializable();
            length = in.readLong();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte(canRead ? (byte) 1 : (byte) 0);
            dest.writeByte(canWrite ? (byte) 1 : (byte) 0);
            dest.writeByte(exists ? (byte) 1 : (byte) 0);
            dest.writeByte(isDirectory ? (byte) 1 : (byte) 0);
            dest.writeByte(isFile ? (byte) 1 : (byte) 0);
            dest.writeByte(isHidden ? (byte) 1 : (byte) 0);
            dest.writeSerializable(lastModified);
            dest.writeLong(length);
        }
    }
}
