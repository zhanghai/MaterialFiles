/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.support.annotation.NonNull;

import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;

public class ArchiveFile extends BaseFile {

    public static final String SCHEME = "archive";

    private Uri mArchivePath;
    private Uri mEntryPath;
    private Archive.Information mInformation;

    public ArchiveFile(Uri path) {
        super(path);

        mArchivePath = Uri.parse(path.getSchemeSpecificPart());
        mEntryPath = Uri.parse(path.getFragment());
    }

    public ArchiveFile(Uri archivePath, Uri entryPath) {
        super(Uri.fromParts(SCHEME, archivePath.toString(), entryPath.toString()));

        mArchivePath = archivePath;
        mEntryPath = entryPath;
    }

    private ArchiveFile(Uri path, Archive.Information information) {
        this(path);

        mInformation = information;
    }

    private ArchiveFile(Uri archivePath, Uri entryPath, Archive.Information information) {
        this(archivePath, entryPath);

        mInformation = information;
    }

    @NonNull
    @Override
    public List<File> makeFilePath() {
        File archiveFile = FileFactory.create(mArchivePath);
        List<File> path = new ArrayList<>(archiveFile.makeFilePath());
        CollectionUtils.pop(path);
        Uri.Builder entryPathBuilder = mEntryPath.buildUpon().path("/");
        path.add(new ArchiveFile(mArchivePath, entryPathBuilder.build()));
        for (String entryPathSegment : mEntryPath.getPathSegments()) {
            entryPathBuilder.appendPath(entryPathSegment);
            path.add(new ArchiveFile(mArchivePath, entryPathBuilder.build()));
        }
        return path;
    }

    @NonNull
    @Override
    public String getName() {
        List<String> segments = mEntryPath.getPathSegments();
        if (segments.isEmpty()) {
            return mArchivePath.getLastPathSegment();
        }
        return CollectionUtils.last(segments);
    }

    @Override
    public void loadInformation() {

    }

    @Override
    public long getSize() {
        return mInformation.entry.getSize();
    }

    @Override
    public Instant getLastModified() {
        return Instant.ofEpochMilli(mInformation.entry.getLastModifiedDate().getTime());
    }

    @Override
    public boolean isDirectory() {
        return mInformation.entry.isDirectory();
    }

    @NonNull
    @Override
    public java.io.File makeJavaFile() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFileList() {
        Map<Uri, List<Archive.Information>> tree = Archive.read(new java.io.File(
                mArchivePath.getPath()));
        // TODO: Handle non-existent path NPE.
        mFileList = Functional.map(tree.get(mEntryPath), information -> new ArchiveFile(
                mArchivePath, information.path, information));
    }
}
