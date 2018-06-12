/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.MapCompat;

public class Archive {

    private static final ArchiveStreamFactory sArchiveStreamFactory = new ArchiveStreamFactory();

    private Archive() {}

    public static Map<Uri, List<Information>> read(InputStream inputStream) {
        List<ArchiveEntry> entries = new ArrayList<>();
        try {
            readEntries(inputStream, entries);
        } catch (ArchiveException | IOException e) {
            // TODO
            e.printStackTrace();
        }
        List<Information> informations = Functional.map(entries, entry -> new Information(
                getEntryPath(entry), entry));
        Map<Uri, List<Information>> tree = new HashMap<>();
        tree.put(Uri.parse("/"), new ArrayList<>());
        Map<Uri, Boolean> directoryInformationExists = new HashMap<>();
        for (Information information : informations) {
            Uri parentPath = getParentPath(information.path);
            MapCompat.putIfAbsent(directoryInformationExists, parentPath, false);
            MapCompat.computeIfAbsent(tree, parentPath, _1 -> new ArrayList<>())
                    .add(information);
            if (information.entry.isDirectory()) {
                directoryInformationExists.put(information.path, true);
                MapCompat.computeIfAbsent(tree, information.path, _1 -> new ArrayList<>());
            }
        }
        for (Map.Entry<Uri, Boolean> mapEntry : directoryInformationExists.entrySet()) {
            Uri.Builder builder = Uri.parse("/").buildUpon();
            Uri parentPath = builder.build();
            for (String pathSegment : mapEntry.getKey().getPathSegments()) {
                builder.appendPath(pathSegment);
                Uri path = builder.build();
                if (!MapCompat.getOrDefault(directoryInformationExists, path, false)) {
                    ArchiveEntry entry = new ArchiveDirectoryEntry(path.getPath());
                    Information information = new Information(path, entry);
                    tree.get(parentPath).add(information);
                }
                MapCompat.computeIfAbsent(tree, path, _1 -> new ArrayList<>());
                parentPath = path;
            }
        }
        return tree;
    }

    private static void readEntries(InputStream inputStream, List<ArchiveEntry> entries)
            throws ArchiveException, IOException {
        try (ArchiveInputStream archiveInputStream = sArchiveStreamFactory.createArchiveInputStream(
                new BufferedInputStream(inputStream))) {
            while (true) {
                ArchiveEntry entry = archiveInputStream.getNextEntry();
                if (entry == null) {
                    break;
                }
                entries.add(entry);
            }
        }
    }

    private static Uri getEntryPath(ArchiveEntry entry) {
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
        if (builder.length() == 0) {
            builder.append('/');
        }
        return Uri.parse(builder.toString());
    }

    private static Uri getParentPath(Uri path) {
        Uri.Builder builder = path.buildUpon()
                .path("/");
        List<String> pathSegments = path.getPathSegments();
        for (int i = 0, count = pathSegments.size() - 1; i < count; ++i) {
            String pathSegment = pathSegments.get(i);
            builder.appendPath(pathSegment);
        }
        return builder.build();
    }

    private static Iterable<Uri> getAncestorPaths(Uri path) {
        return () -> new Iterator<Uri>() {
            private List<String> mPathSegments = path.getPathSegments();
            private int mIndex = 0;
            private Uri.Builder mBuilder = path.buildUpon().path("/");
            @Override
            public boolean hasNext() {
                return mIndex < mPathSegments.size();
            }
            @Override
            public Uri next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                mBuilder.appendPath(mPathSegments.get(mIndex));
                ++mIndex;
                return mBuilder.build();
            }
        };
    }

    public static class Information {

        public Uri path;
        public ArchiveEntry entry;

        public Information(Uri path, ArchiveEntry entry) {
            this.path = path;
            this.entry = entry;
        }
    }

    private static class ArchiveDirectoryEntry implements ArchiveEntry {

        private String mName;

        public ArchiveDirectoryEntry(String name) {
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
