/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcel;
import android.text.TextUtils;

import org.threeten.bp.Instant;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;
import me.zhanghai.android.materialfilemanager.file.MimeTypes;
import me.zhanghai.android.materialfilemanager.functional.Functional;

public class LocalFile extends BaseFile {

    public static final String SCHEME = "file";

    private static final String HIDDEN_FILE_PREFIX = ".";

    @Nullable
    private LocalFileSystem.Information mInformation;

    @NonNull
    static Uri uriFromPath(@NonNull String path) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority("")
                .path(path)
                .build();
    }

    @NonNull
    static String uriToPath(@NonNull Uri uri) {
        return uri.getPath();
    }

    @NonNull
    static String joinPaths(@NonNull String parent, @NonNull String child) {
        return new java.io.File(parent, child).getPath();
    }

    public LocalFile(@NonNull Uri uri) {
        // TODO: Better determination of strategy?
        super(uri);
    }

    LocalFile(@NonNull Uri uri, @NonNull LocalFileSystem.Information information) {
        super(uri);

        mInformation = information;
    }

    @NonNull
    public String getPath() {
        return uriToPath(mUri);
    }

    @Nullable
    @Override
    public LocalFile getParent() {
        String parentPath = new java.io.File(getPath()).getParent();
        if (TextUtils.isEmpty(parentPath)) {
            return null;
        }
        return new LocalFile(uriFromPath(parentPath));
    }

    @NonNull
    @Override
    public LocalFile getChild(@NonNull String childName) {
        String childPath = new java.io.File(getPath(), childName).getPath();
        return new LocalFile(uriFromPath(childPath));
    }

    @Override
    public boolean isHidden() {
        return getName().startsWith(HIDDEN_FILE_PREFIX);
    }

    @Override
    public boolean hasInformation() {
        return mInformation != null;
    }

    @Override
    @WorkerThread
    public void reloadInformation() throws FileSystemException {
        mInformation = LocalFileSystem.getInformation(getPath());
    }

    @Override
    public boolean isSymbolicLink() {
        return mInformation.isSymbolicLink;
    }

    @Override
    public boolean isSymbolicLinkBroken() {
        if (!isSymbolicLink()) {
            throw new IllegalStateException("Not a symbolic link");
        }
        return !mInformation.isSymbolicLinkStat;
    }

    @NonNull
    @Override
    public String getSymbolicLinkTarget() {
        if (!isSymbolicLink()) {
            throw new IllegalStateException("Not a symbolic link");
        }
        return mInformation.symbolicLinkTarget;
    }

    @NonNull
    public PosixFileType getType() {
        return mInformation.type;
    }

    @Override
    public boolean isDirectory() {
        return getType() == PosixFileType.DIRECTORY;
    }

    @NonNull
    @Override
    public String getMimeType() {
        String mimeType = MimeTypes.getPosixMimeType(getType());
        if (!TextUtils.isEmpty(mimeType)) {
            return mimeType;
        }
        return super.getMimeType();
    }

    @NonNull
    public Set<PosixFileModeBit> getMode() {
        return mInformation.mode;
    }

    @NonNull
    public PosixUser getOwner() {
        return mInformation.owner;
    }

    @NonNull
    public PosixGroup getGroup() {
        return mInformation.group;
    }

    @Override
    public long getSize() {
        return mInformation.size;
    }

    @NonNull
    @Override
    public Instant getLastModificationTime() {
        return mInformation.lastModificationTime;
    }

    @NonNull
    @Override
    @WorkerThread
    public List<File> getChildren() throws FileSystemException {
        String path = getPath();
        List<Pair<String, LocalFileSystem.Information>> children = LocalFileSystem.getChildren(
                path);
        return Functional.map(children, child -> {
            Uri childUri = LocalFile.uriFromPath(LocalFile.joinPaths(path, child.first));
            return new LocalFile(childUri, child.second);
        });
    }


    @Override
    public boolean equalsIncludingInformation(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        LocalFile that = (LocalFile) object;
        return Objects.equals(mUri, that.mUri)
                && Objects.equals(mInformation, that.mInformation);
    }


    public static final Creator<LocalFile> CREATOR = new Creator<LocalFile>() {
        @NonNull
        @Override
        public LocalFile createFromParcel(Parcel source) {
            return new LocalFile(source);
        }
        @NonNull
        @Override
        public LocalFile[] newArray(int size) {
            return new LocalFile[size];
        }
    };

    protected LocalFile(@NonNull Parcel in) {
        super(in);

        mInformation = in.readParcelable(LocalFileSystem.Information.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mInformation, flags);
    }
}
