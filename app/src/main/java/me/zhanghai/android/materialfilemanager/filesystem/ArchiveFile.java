/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcel;

import org.apache.commons.compress.archivers.ArchiveException;
import org.threeten.bp.Instant;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;

public class ArchiveFile extends BaseFile {

    public static final String SCHEME = "archive";

    @NonNull
    private final LocalFile mArchiveFile;
    @NonNull
    private final Uri mEntryPath;
    @Nullable
    private Archive.Information mInformation;

    public ArchiveFile(@NonNull Uri uri) {
        super(uri);

        mArchiveFile = new LocalFile(Uri.parse(uri.getSchemeSpecificPart()));
        mEntryPath = Archive.pathFromString(uri.getFragment());
    }

    public ArchiveFile(@NonNull LocalFile archiveFile, @NonNull Uri entryPath) {
        super(Uri.fromParts(SCHEME, archiveFile.getUri().toString(), entryPath.toString()));

        mArchiveFile = archiveFile;
        mEntryPath = entryPath;
    }

    private ArchiveFile(@NonNull Uri uri, @NonNull Archive.Information information) {
        this(uri);

        mInformation = information;
    }

    private ArchiveFile(@NonNull LocalFile archiveFile, @NonNull Uri entryPath,
                        @NonNull Archive.Information information) {
        this(archiveFile, entryPath);

        mInformation = information;
    }

    @NonNull
    public LocalFile getArchiveFile() {
        return mArchiveFile;
    }

    @NonNull
    public Uri getEntryPath() {
        return mEntryPath;
    }

    @NonNull
    public String getEntryName() {
        return mInformation.name;
    }

    @Nullable
    @Override
    public File getParent() {
        List<String> entryPathSegments = mEntryPath.getPathSegments();
        if (entryPathSegments.isEmpty()) {
            return mArchiveFile.getParent();
        }
        Uri.Builder parentEntryPathBuilder = Archive.pathBuilderForRoot();
        List<String> parentEntryPathSegments = entryPathSegments.subList(0,
                entryPathSegments.size() - 1);
        Functional.forEach(parentEntryPathSegments, parentEntryPathBuilder::appendPath);
        Uri parentEntryPath = parentEntryPathBuilder.build();
        return new ArchiveFile(mArchiveFile, parentEntryPath);
    }

    @NonNull
    @Override
    public ArchiveFile getChild(@NonNull String childName) {
        Uri childEntryPath = mEntryPath.buildUpon()
                .appendPath(childName)
                .build();
        return new ArchiveFile(mArchiveFile, childEntryPath);
    }

    @NonNull
    @Override
    public String getName() {
        List<String> segments = mEntryPath.getPathSegments();
        if (segments.isEmpty()) {
            return mArchiveFile.getName();
        }
        return CollectionUtils.last(segments);
    }

    @Override
    public boolean hasInformation() {
        return mInformation != null;
    }

    @Override
    @WorkerThread
    public void reloadInformation() {

    }

    @Override
    public boolean isSymbolicLink() {
        return mInformation.isSymbolicLink;
    }

    @Override
    public boolean isSymbolicLinkBroken() {
        return false;
    }

    @NonNull
    @Override
    public String getSymbolicLinkTarget() {
        // TODO: Read entry content of symbolic link beforehand.
        //return (mInformation.entry instanceof ZipArchiveEntry)
        //        && ((ZipArchiveEntry) mInformation.entry).;
        return "TODO";
    }

    @Override
    public boolean isDirectory() {
        return mInformation.isDirectory;
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
        Map<Uri, List<Archive.Information>> tree;
        try {
            tree = Archive.readTree(mArchiveFile.getPath());
        } catch (ArchiveException | IOException e) {
            throw new FileSystemException(R.string.file_list_error_archive, e);
        }
        // TODO: Handle non-existent path NPE.
        return Functional.map(tree.get(mEntryPath), information -> new ArchiveFile(mArchiveFile,
                information.path, information));
    }


    @Override
    public boolean equalsIncludingInformation(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ArchiveFile that = (ArchiveFile) object;
        return Objects.equals(mUri, that.mUri)
                && Objects.equals(mArchiveFile, that.mArchiveFile)
                && Objects.equals(mEntryPath, that.mEntryPath)
                && Objects.equals(mInformation, that.mInformation);
    }


    public static final Creator<ArchiveFile> CREATOR = new Creator<ArchiveFile>() {
        @NonNull
        @Override
        public ArchiveFile createFromParcel(@NonNull Parcel source) {
            return new ArchiveFile(source);
        }
        @NonNull
        @Override
        public ArchiveFile[] newArray(int size) {
            return new ArchiveFile[size];
        }
    };

    protected ArchiveFile(@NonNull Parcel in) {
        super(in);

        mArchiveFile = in.readParcelable(File.class.getClassLoader());
        mEntryPath = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mArchiveFile, flags);
        dest.writeParcelable(mEntryPath, flags);
    }
}
