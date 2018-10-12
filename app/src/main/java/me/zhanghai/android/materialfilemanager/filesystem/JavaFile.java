/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import org.threeten.bp.Instant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.R;

public class JavaFile {

    public static Information loadInformation(File file) throws FileSystemException {
        Information information = new Information();
        information.canRead = file.canRead();
        information.canWrite = file.canWrite();
        information.exists = file.exists();
        information.isDirectory = file.isDirectory();
        information.isFile = file.isFile();
        information.isHidden = file.isHidden();
        information.lastModified = Instant.ofEpochMilli(file.lastModified());
        information.length = file.length();
        try {
            information.isSymbolicLink = isSymbolicLink(file);
        } catch (IOException e) {
            throw new FileSystemException(R.string.file_error_information, e);
        }
        return information;
    }

    // @see https://github.com/apache/commons-io/commit/9d432121e1c60557da3e159252a88885944e5f00
    public static boolean isSymbolicLink(File file) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Files.isSymbolicLink(file.toPath());
        } else {
            File fileWithCanonicalParent;
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                fileWithCanonicalParent = new File(parentFile.getCanonicalFile(), file.getName());
            } else {
                fileWithCanonicalParent = file;
            }
            if (!fileWithCanonicalParent.getAbsoluteFile().equals(
                    fileWithCanonicalParent.getCanonicalFile())) {
                return true;
            }
            // TODO: Check for broken symbolic link?
            return false;
        }
    }

    public static boolean isDirectory(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    public static long getFreeSpace(String path) {
        File file = new File(path);
        return file.getFreeSpace();
    }

    public static long getTotalSpace(String path) {
        File file = new File(path);
        return file.getTotalSpace();
    }

    public static List<String> getChildren(File directory) throws FileSystemException {
        String[] children = directory.list();
        if (children == null) {
            throw new FileSystemException(R.string.file_list_error_directory);
        }
        return Arrays.asList(children);
    }

    public static List<File> getChildFiles(File directory) throws FileSystemException {
        File[] children = directory.listFiles();
        if (children == null) {
            throw new FileSystemException(R.string.file_list_error_directory);
        }
        return Arrays.asList(children);
    }

    public static void delete(File file) throws FileSystemException {
        boolean result = file.delete();
        if (!result) {
            throw new FileSystemException(R.string.file_delete_error);
        }
    }

    public static void rename(File file, String newName) throws FileSystemException {
        File newFile = new File(file.getParent(), newName);
        boolean result = file.renameTo(newFile);
        if (!result) {
            throw new FileSystemException(R.string.file_rename_error);
        }
    }

    public static void createFile(File file) throws FileSystemException {
        try {
            boolean result = file.createNewFile();
            if (!result) {
                throw new FileSystemException(R.string.file_create_file_error_already_exists);
            }
        } catch (IOException e) {
            throw new FileSystemException(R.string.file_create_file_error, e);
        }
    }

    public static void createDirectory(File file) throws FileSystemException {
        boolean result = file.mkdir();
        if (!result) {
            throw new FileSystemException(R.string.file_create_directory_error);
        }
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
        public boolean isSymbolicLink;

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
                    && length == that.length
                    && isSymbolicLink == that.isSymbolicLink;
        }

        @Override
        public int hashCode() {
            return Objects.hash(canRead, canWrite, exists, isDirectory, isFile, isHidden,
                    lastModified, length, isSymbolicLink);
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
            isSymbolicLink = in.readByte() != 0;
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
            dest.writeByte(isSymbolicLink ? (byte) 1 : (byte) 0);
        }
    }
}
