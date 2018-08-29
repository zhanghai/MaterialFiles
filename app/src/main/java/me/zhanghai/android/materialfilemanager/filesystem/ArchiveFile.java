/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.threeten.bp.Instant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;

public class ArchiveFile extends BaseFile {

    public static final String SCHEME = "archive";

    private LocalFile mArchiveFile;
    private Uri mEntryPath;
    private Archive.Information mInformation;

    public ArchiveFile(Uri uri) {
        super(uri);

        mArchiveFile = new LocalFile(Uri.parse(uri.getSchemeSpecificPart()));
        mEntryPath = Archive.pathFromString(uri.getFragment());
    }

    public ArchiveFile(LocalFile archiveFile, Uri entryPath) {
        super(Uri.fromParts(SCHEME, archiveFile.getUri().toString(), entryPath.toString()));

        mArchiveFile = archiveFile;
        mEntryPath = entryPath;
    }

    private ArchiveFile(Uri uri, Archive.Information information) {
        this(uri);

        mInformation = information;
    }

    private ArchiveFile(LocalFile archiveFile, Uri entryPath, Archive.Information information) {
        this(archiveFile, entryPath);

        mInformation = information;
    }

    public LocalFile getArchiveFile() {
        return mArchiveFile;
    }

    public Uri getEntryPath() {
        return mEntryPath;
    }

    public String getEntryName() {
        return mInformation.entry.getName();
    }

    @Nullable
    @Override
    public File getParent() {
        List<String> entryPathSegments = mEntryPath.getPathSegments();
        if (entryPathSegments.isEmpty()) {
            return mArchiveFile.getParent();
        }
        Uri.Builder entryPathBuilder = Archive.pathBuilderForRoot();
        entryPathSegments = entryPathSegments.subList(0, entryPathSegments.size() - 1);
        Functional.forEach(entryPathSegments, entryPathBuilder::appendPath);
        return new ArchiveFile(mArchiveFile, entryPathBuilder.build());
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
    public void reloadInformation() {

    }

    @Override
    public boolean isDirectory() {
        return mInformation.entry.isDirectory();
    }

    @Override
    public boolean isSymbolicLink() {
        // FIXME: Move this to information.
        return (mInformation.entry instanceof ZipArchiveEntry)
                && ((ZipArchiveEntry) mInformation.entry).isUnixSymlink();
    }

    @Override
    public long getSize() {
        return mInformation.entry.getSize();
    }

    @Override
    public Instant getLastModificationTime() {
        return Instant.ofEpochMilli(mInformation.entry.getLastModifiedDate().getTime());
    }

    @Override
    public List<File> getChildren() throws FileSystemException {
        Map<Uri, List<Archive.Information>> tree;
        try {
            tree = Archive.readTree(mArchiveFile.makeJavaFile());
        } catch (ArchiveException | IOException e) {
            throw new FileSystemException(R.string.file_list_error_archive, e);
        }
        // TODO: Handle non-existent path NPE.
        return Functional.map(tree.get(mEntryPath), information -> new ArchiveFile(mArchiveFile,
                information.path, information));
    }


    @Override
    public boolean equals(Object object) {
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

    @Override
    public int hashCode() {
        return Objects.hash(mUri, mArchiveFile, mEntryPath, mInformation);
    }


    public static final Creator<ArchiveFile> CREATOR = new Creator<ArchiveFile>() {
        @Override
        public ArchiveFile createFromParcel(Parcel source) {
            return new ArchiveFile(source);
        }
        @Override
        public ArchiveFile[] newArray(int size) {
            return new ArchiveFile[size];
        }
    };

    protected ArchiveFile(Parcel in) {
        super(in);

        mArchiveFile = in.readParcelable(File.class.getClassLoader());
        mEntryPath = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mArchiveFile, flags);
        dest.writeParcelable(mEntryPath, flags);
    }
}
