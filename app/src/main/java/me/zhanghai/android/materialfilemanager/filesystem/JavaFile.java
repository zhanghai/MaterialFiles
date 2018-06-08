/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

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
        information.lastModified = file.lastModified();
        information.length = file.length();
        return information;
    }

    public static class Information {

        public boolean canRead;
        public boolean canWrite;
        public boolean exists;
        public boolean isDirectory;
        public boolean isFile;
        public boolean isHidden;
        public long lastModified;
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
                    && lastModified == that.lastModified
                    && length == that.length;
        }

        @Override
        public int hashCode() {
            return Objects.hash(canRead, canWrite, exists, isDirectory, isFile, isHidden,
                    lastModified, length);
        }
    }
}
