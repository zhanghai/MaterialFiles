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
import java.util.Set;

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
        public boolean isSymbolicLink() {
            return mInformation.isSymbolicLink;
        }

        @Override
        public boolean isDirectory() {
            // This includes symbolic link to directory.
            return mInformation.isDirectory;
        }

        @Override
        public Set<PosixFileModeBit> getMode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PosixUser getOwner() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PosixGroup getGroup() {
            throw new UnsupportedOperationException();
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

    class SyscallStrategy implements LocalFileStrategy {

        private Syscall.Information mInformation;

        public SyscallStrategy() {}

        public SyscallStrategy(Syscall.Information information) {
            mInformation = information;
        }

        @Override
        public boolean hasInformation() {
            return mInformation != null;
        }

        @Override
        @WorkerThread
        public void reloadInformation(LocalFile file) throws FileSystemException {
            mInformation = Syscall.loadInformation(file.getPath());
        }

        @Override
        public boolean isSymbolicLink() {
            return mInformation.type == PosixFileType.SYMBOLIC_LINK;
        }

        private Syscall.Information getInformationFollowingSymbolicLinks() {
            return isSymbolicLink() && mInformation.symbolicLinkStatInformation != null ?
                    mInformation.symbolicLinkStatInformation : mInformation;
        }

        @Override
        public boolean isDirectory() {
            return getInformationFollowingSymbolicLinks().type == PosixFileType.DIRECTORY;
        }

        @Override
        public Set<PosixFileModeBit> getMode() {
            return getInformationFollowingSymbolicLinks().mode;
        }

        @Override
        public PosixUser getOwner() {
            return getInformationFollowingSymbolicLinks().owner;
        }

        @Override
        public PosixGroup getGroup() {
            return getInformationFollowingSymbolicLinks().group;
        }

        @Override
        public long getSize() {
            return getInformationFollowingSymbolicLinks().size;
        }

        @Override
        public Instant getLastModificationTime() {
            return getInformationFollowingSymbolicLinks().lastModificationTime;
        }

        @Override
        @WorkerThread
        public List<File> getChildren(LocalFile file) throws FileSystemException {
            String parentPath = file.getPath();
            List<String> childPaths = Functional.map(JavaFile.getChildren(file.makeJavaFile()),
                    childName -> LocalFile.joinPaths(parentPath, childName));
            List<Syscall.Information> childInformations;
            try {
                childInformations = Functional.map(childPaths, (ThrowingFunction<String,
                        Syscall.Information>) Syscall::loadInformation);
            } catch (FunctionalException e) {
                throw e.getCauseAs(FileSystemException.class);
            }
            return Functional.map(childPaths, (childPath, index) -> {
                Uri childUri = LocalFile.uriFromPath(childPath);
                SyscallStrategy childStrategy = new SyscallStrategy(childInformations.get(
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
            SyscallStrategy that = (SyscallStrategy) object;
            return Objects.equals(mInformation, that.mInformation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mInformation);
        }


        public static final Creator<SyscallStrategy> CREATOR = new Creator<SyscallStrategy>() {
            @Override
            public SyscallStrategy createFromParcel(Parcel source) {
                return new SyscallStrategy(source);
            }
            @Override
            public SyscallStrategy[] newArray(int size) {
                return new SyscallStrategy[size];
            }
        };

        protected SyscallStrategy(Parcel in) {
            mInformation = in.readParcelable(Syscall.Information.class.getClassLoader());
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
        public boolean isSymbolicLink() {
            return mInformation.type == PosixFileType.SYMBOLIC_LINK;
        }

        private ShellStat.Information getInformationFollowingSymbolicLinks() {
            return isSymbolicLink() && mInformation.symbolicLinkStatLInformation != null ?
                    mInformation.symbolicLinkStatLInformation : mInformation;
        }

        @Override
        public boolean isDirectory() {
            return getInformationFollowingSymbolicLinks().type == PosixFileType.DIRECTORY;
        }

        @Override
        public Set<PosixFileModeBit> getMode() {
            return getInformationFollowingSymbolicLinks().mode;
        }

        @Override
        public PosixUser getOwner() {
            return getInformationFollowingSymbolicLinks().owner;
        }

        @Override
        public PosixGroup getGroup() {
            return getInformationFollowingSymbolicLinks().group;
        }

        @Override
        public long getSize() {
            return getInformationFollowingSymbolicLinks().size;
        }

        @Override
        public Instant getLastModificationTime() {
            return getInformationFollowingSymbolicLinks().lastModificationTime;
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

    class ShellFsStrategy implements LocalFileStrategy {

        private ShellFs.Information mInformation;

        public ShellFsStrategy() {}

        public ShellFsStrategy(ShellFs.Information information) {
            mInformation = information;
        }

        @Override
        public boolean hasInformation() {
            return mInformation != null;
        }

        @Override
        @WorkerThread
        public void reloadInformation(LocalFile file) throws FileSystemException {
            mInformation = ShellFs.loadInformation(file.getPath());
        }

        @Override
        public boolean isSymbolicLink() {
            return mInformation.isSymbolicLink;
        }

        @Override
        public boolean isDirectory() {
            return mInformation.type == PosixFileType.DIRECTORY;
        }

        @Override
        public Set<PosixFileModeBit> getMode() {
            return mInformation.mode;
        }

        @Override
        public PosixUser getOwner() {
            return mInformation.owner;
        }

        @Override
        public PosixGroup getGroup() {
            return mInformation.group;
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
        @WorkerThread
        public List<File> getChildren(LocalFile file) throws FileSystemException {
            String parentPath = file.getPath();
            List<Pair<String, ShellFs.Information>> children =
                    ShellFs.getChildrenAndInformation(parentPath);
            return Functional.map(children, child -> {
                Uri childUri = LocalFile.uriFromPath(LocalFile.joinPaths(parentPath, child.first));
                ShellFsStrategy childStrategy = new ShellFsStrategy(child.second);
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
            ShellFsStrategy that = (ShellFsStrategy) object;
            return Objects.equals(mInformation, that.mInformation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mInformation);
        }


        public static final Creator<ShellFsStrategy> CREATOR = new Creator<ShellFsStrategy>() {
            @Override
            public ShellFsStrategy createFromParcel(Parcel source) {
                return new ShellFsStrategy(source);
            }
            @Override
            public ShellFsStrategy[] newArray(int size) {
                return new ShellFsStrategy[size];
            }
        };

        protected ShellFsStrategy(Parcel in) {
            mInformation = in.readParcelable(ShellFs.Information.class.getClassLoader());
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
