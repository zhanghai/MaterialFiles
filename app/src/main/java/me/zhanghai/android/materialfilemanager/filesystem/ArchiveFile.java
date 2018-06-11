/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.support.annotation.NonNull;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.threeten.bp.Instant;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;

public class ArchiveFile extends BaseFile {

    public static final String SCHEME = "archive";

    private static final ArchiveStreamFactory sArchiveStreamFactory = new ArchiveStreamFactory();

    private Uri mArchivePath;
    private Uri mEntryPath;
    private ArchiveEntry mEntry;

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

    private ArchiveFile(Uri path, ArchiveEntry entry) {
        this(path);

        mEntry = entry;
    }

    private ArchiveFile(Uri archivePath, Uri entryPath, ArchiveEntry entry) {
        this(archivePath, entryPath);

        mEntry = entry;
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
        return mEntry.getSize();
    }

    @Override
    public Instant getLastModified() {
        return Instant.ofEpochMilli(mEntry.getLastModifiedDate().getTime());
    }

    @Override
    public boolean isDirectory() {
        return mEntry.isDirectory();
    }

    @NonNull
    @Override
    public java.io.File makeJavaFile() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFileList() {
        List<ArchiveEntry> entries = new ArrayList<>();
        try (InputStream fileInputStream = new BufferedInputStream(new FileInputStream(
                mArchivePath.getPath()))) {
            try (ArchiveInputStream archiveInputStream =
                         sArchiveStreamFactory.createArchiveInputStream(fileInputStream)) {
                while (true) {
                    ArchiveEntry entry = archiveInputStream.getNextEntry();
                    if (entry == null) {
                        break;
                    }
                    entries.add(entry);
                }
            }
        } catch (ArchiveException | IOException e) {
            // TODO
            e.printStackTrace();
        }
        List<ArchiveFile> entryFiles = Functional.map(entries, entry -> new ArchiveFile(
                mArchivePath, getEntryPath(entry), entry));
        Map<Uri, Boolean> directories = new HashMap<>();
        mFileList = new ArrayList<>();
        Functional.forEach(entryFiles, entryFile -> {
            List<String> currentSegments = mEntryPath.getPathSegments();
            List<String> entrySegments = entryFile.mEntryPath.getPathSegments();
            if (entrySegments.size() > currentSegments.size()
                    && CollectionUtils.isPrefix(currentSegments, entrySegments)) {
                if (entrySegments.size() == currentSegments.size() + 1) {
                    mFileList.add(entryFile);
                    if (entryFile.mEntry.isDirectory()) {
                        directories.put(entryFile.mEntryPath, true);
                    }
                } else {
                    String name = entrySegments.get(currentSegments.size());
                    Uri entryPath = mEntryPath.buildUpon().appendPath(name).build();
                    if (!directories.containsKey(entryPath)) {
                        directories.put(entryPath, false);
                    }
                }
            }
        });
        Functional.forEach(directories.entrySet(), mapEntry -> {
            boolean added = mapEntry.getValue();
            if (added) {
                return;
            }
            Uri entryPath = mapEntry.getKey();
            ArchiveEntry entry = new DirectoryArchiveEntry(entryPath.getPath());
            mFileList.add(new ArchiveFile(mArchivePath, entryPath, entry));
        });
    }

    private Uri getEntryPath(ArchiveEntry entry) {
        String name = entry.getName();
        StringBuilder builder = new StringBuilder();
        int startIndex = 0;
        while (true) {
            while (startIndex < name.length() && name.charAt(startIndex) == '/') {
                ++startIndex;
            }
            if (startIndex >= name.length()) {
                break;
            }
            int endIndex = startIndex;
            do {
                ++endIndex;
            } while (endIndex < name.length() && name.charAt(endIndex) != '/');
            builder
                    .append('/')
                    .append(name.substring(startIndex, endIndex));
            startIndex = endIndex;
        }
        String path = builder.length() > 0 ? builder.toString() : "/";
        return Uri.parse(path);
    }

    private static class DirectoryArchiveEntry implements ArchiveEntry {

        private String mName;

        public DirectoryArchiveEntry(String name) {
            if (name.endsWith("/")) {
                throw new IllegalArgumentException("Name should not end with a slash: " + name);
            }
            mName = name + "/";
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public Date getLastModifiedDate() {
            return new Date(-1);
        }
    }
}
