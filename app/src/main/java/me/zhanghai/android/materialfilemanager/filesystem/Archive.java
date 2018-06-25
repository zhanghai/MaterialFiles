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
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.FunctionalIterator;
import me.zhanghai.android.materialfilemanager.functional.compat.Consumer;
import me.zhanghai.android.materialfilemanager.util.MapCompat;

public class Archive {

    private static final ArchiveStreamFactory sArchiveStreamFactory = new ArchiveStreamFactory();

    private static final Map<File, Map<Uri, List<Information>>> sTreeCache =
            new ConcurrentHashMap<>();

    private Archive() {}

    public static void retainCache(Collection<File> files) {
        sTreeCache.keySet().retainAll(files);
    }

    public static void invalidateCache(File file) {
        sTreeCache.remove(file);
    }

    public static Map<Uri, List<Information>> readTree(File file)
            throws ArchiveException, IOException {
        Map<Uri, List<Information>> tree = sTreeCache.get(file);
        if (tree == null) {
            List<ArchiveEntry> entries = readEntries(file);
            List<Information> informations = Functional.map(entries, entry -> new Information(
                    getEntryPath(entry), entry));
            tree = new HashMap<>();
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
            sTreeCache.put(file, tree);
        }
        return tree;
    }

    private static List<ArchiveEntry> readEntries(File file) throws ArchiveException, IOException {
        String type;
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            type = ArchiveStreamFactory.detect(inputStream);
        }
        ArrayList<ArchiveEntry> entries = new ArrayList<>();
        switch (type) {
            case ArchiveStreamFactory.ZIP: {
                try (ZipFileCompat zipFile = new ZipFileCompat(file, /*FIXME*/"GB18030")) {
                    FunctionalIterator.forEachRemaining(zipFile.getEntries(),
                            (Consumer<ZipArchiveEntry>) entries::add);
                }
                break;
            }
            case ArchiveStreamFactory.SEVEN_Z: {
                try (SevenZFile sevenZFile = new SevenZFile(file)) {
                    Functional.forEach(sevenZFile.getEntries(),
                            (Consumer<SevenZArchiveEntry>) entries::add);
                }
                break;
            }
            default: {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    try (ArchiveInputStream archiveInputStream =
                                 sArchiveStreamFactory.createArchiveInputStream(
                                         new BufferedInputStream(fileInputStream))) {
                        while (true) {
                            ArchiveEntry entry = archiveInputStream.getNextEntry();
                            if (entry == null) {
                                break;
                            }
                            entries.add(entry);
                        }
                    }
                }
            }
        }
        return entries;
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

    public static class Information {

        public Uri path;
        public ArchiveEntry entry;

        public Information(Uri path, ArchiveEntry entry) {
            this.path = path;
            this.entry = entry;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            Information that = (Information) object;
            return Objects.equals(path, that.path)
                    && Objects.equals(entry, that.entry);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, entry);
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

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            ArchiveDirectoryEntry that = (ArchiveDirectoryEntry) object;
            return Objects.equals(mName, that.mName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mName);
        }
    }
}
