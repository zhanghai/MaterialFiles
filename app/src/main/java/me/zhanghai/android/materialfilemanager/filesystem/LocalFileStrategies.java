/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;

import org.threeten.bp.Instant;

import java.util.List;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.FunctionalException;
import me.zhanghai.android.materialfilemanager.functional.throwing.ThrowingFunction;

public interface LocalFileStrategies {

    class JavaFileStrategy implements LocalFileStrategy {

        private JavaFile.Information mInformation;

        public JavaFileStrategy() {}

        public JavaFileStrategy(JavaFile.Information information) {
            mInformation = information;
        }

        @Override
        public boolean hasInformation() {
            return mInformation != null;
        }

        @Override
        @WorkerThread
        public void reloadInformation(LocalFile file) throws FileSystemException {
            mInformation = JavaFile.loadInformation(file.makeJavaFile());
        }

        @Override
        public long getSize() {
            return mInformation.length;
        }

        @Override
        public Instant getLastModificationTime() {
            return mInformation.lastModified;
        }

        @Override
        public boolean isDirectory() {
            // TODO: Symbolic link?
            return mInformation.isDirectory;
        }

        @Override
        public boolean isSymbolicLink() {
            return mInformation.isSymbolicLink;
        }

        @Override
        @WorkerThread
        public List<File> getChildren(LocalFile file) throws FileSystemException {
            List<java.io.File> childJavaFiles = JavaFile.getChildFiles(file.makeJavaFile());
            List<JavaFile.Information> childInformations;
            try {
                childInformations = Functional.map(childJavaFiles, (ThrowingFunction<java.io.File,
                        JavaFile.Information>) JavaFile::loadInformation);
            } catch (FunctionalException e) {
                throw e.getCauseAs(FileSystemException.class);
            }
            return Functional.map(childJavaFiles, (childJavaFile, index) -> {
                Uri childUri = LocalFile.uriFromPath(childJavaFile.getPath());
                JavaFileStrategy childStrategy = new JavaFileStrategy(childInformations.get(index));
                return new LocalFile(childUri, childStrategy);
            });
        }


        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            JavaFileStrategy that = (JavaFileStrategy) object;
            return Objects.equals(mInformation, that.mInformation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mInformation);
        }


        public static final Creator<JavaFileStrategy> CREATOR = new Creator<JavaFileStrategy>() {
            @Override
            public JavaFileStrategy createFromParcel(Parcel source) {
                return new JavaFileStrategy(source);
            }
            @Override
            public JavaFileStrategy[] newArray(int size) {
                return new JavaFileStrategy[size];
            }
        };

        protected JavaFileStrategy(Parcel in) {
            mInformation = in.readParcelable(JavaFile.Information.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mInformation, flags);
        }
    }

    class AndroidOsStrategy implements LocalFileStrategy {

        private AndroidOs.Information mInformation;

        public AndroidOsStrategy() {}

        public AndroidOsStrategy(AndroidOs.Information information) {
            mInformation = information;
        }

        @Override
        public boolean hasInformation() {
            return mInformation != null;
        }

        @Override
        @WorkerThread
        public void reloadInformation(LocalFile file) throws FileSystemException {
            mInformation = AndroidOs.loadInformation(file.getPath());
        }

        @Override
        public long getSize() {
            return mInformation.size;
        }

        @Override
        public Instant getLastModificationTime() {
            return mInformation.lastModificationTime;
        }

        @Override
        public boolean isDirectory() {
            // TODO: Symbolic link?
            return mInformation.type == PosixFileType.DIRECTORY;
        }

        @Override
        public boolean isSymbolicLink() {
            return mInformation.type == PosixFileType.SYMBOLIC_LINK;
        }

        @Override
        @WorkerThread
        public List<File> getChildren(LocalFile file) throws FileSystemException {
            String parentPath = file.getPath();
            List<String> childPaths = Functional.map(JavaFile.getChildren(file.makeJavaFile()),
                    childName -> LocalFile.joinPaths(parentPath, childName));
            List<AndroidOs.Information> childInformations;
            try {
                childInformations = Functional.map(childPaths, (ThrowingFunction<String,
                        AndroidOs.Information>) AndroidOs::loadInformation);
            } catch (FunctionalException e) {
                throw e.getCauseAs(FileSystemException.class);
            }
            return Functional.map(childPaths, (childPath, index) -> {
                Uri childUri = LocalFile.uriFromPath(childPath);
                AndroidOsStrategy childStrategy = new AndroidOsStrategy(childInformations.get(
                        index));
                return new LocalFile(childUri, childStrategy);
            });
        }


        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            AndroidOsStrategy that = (AndroidOsStrategy) object;
            return Objects.equals(mInformation, that.mInformation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mInformation);
        }


        public static final Creator<AndroidOsStrategy> CREATOR = new Creator<AndroidOsStrategy>() {
            @Override
            public AndroidOsStrategy createFromParcel(Parcel source) {
                return new AndroidOsStrategy(source);
            }
            @Override
            public AndroidOsStrategy[] newArray(int size) {
                return new AndroidOsStrategy[size];
            }
        };

        protected AndroidOsStrategy(Parcel in) {
            mInformation = in.readParcelable(AndroidOs.Information.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mInformation, flags);
        }
    }

    class ShellStatStrategy implements LocalFileStrategy {

        private ShellStat.Information mInformation;

        public ShellStatStrategy() {}

        public ShellStatStrategy(ShellStat.Information information) {
            mInformation = information;
        }

        @Override
        public boolean hasInformation() {
            return mInformation != null;
        }

        @Override
        @WorkerThread
        public void reloadInformation(LocalFile file) throws FileSystemException {
            mInformation = ShellStat.loadInformation(file.getPath());
        }

        @Override
        public long getSize() {
            return mInformation.size;
        }

        @Override
        public Instant getLastModificationTime() {
            return mInformation.lastModificationTime;
        }

        @Override
        public boolean isDirectory() {
            // TODO: Symbolic link?
            return mInformation.type == PosixFileType.DIRECTORY;
        }

        @Override
        public boolean isSymbolicLink() {
            return mInformation.type == PosixFileType.SYMBOLIC_LINK;
        }

        @Override
        @WorkerThread
        public List<File> getChildren(LocalFile file) throws FileSystemException {
            String parentPath = file.getPath();
            List<Pair<String, ShellStat.Information>> children =
                    ShellStat.getChildrenAndInformation(parentPath);
            return Functional.map(children, child -> {
                Uri childUri = LocalFile.uriFromPath(LocalFile.joinPaths(parentPath, child.first));
                ShellStatStrategy childStrategy = new ShellStatStrategy(child.second);
                return new LocalFile(childUri, childStrategy);
            });
        }


        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            ShellStatStrategy that = (ShellStatStrategy) object;
            return Objects.equals(mInformation, that.mInformation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mInformation);
        }


        public static final Creator<ShellStatStrategy> CREATOR = new Creator<ShellStatStrategy>() {
            @Override
            public ShellStatStrategy createFromParcel(Parcel source) {
                return new ShellStatStrategy(source);
            }
            @Override
            public ShellStatStrategy[] newArray(int size) {
                return new ShellStatStrategy[size];
            }
        };

        protected ShellStatStrategy(Parcel in) {
            mInformation = in.readParcelable(ShellStat.Information.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mInformation, flags);
        }
    }
}
