/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.functional.FunctionalIterator;
import me.zhanghai.android.materialfilemanager.functional.compat.Consumer;
import me.zhanghai.android.materialfilemanager.settings.SettingsLiveDatas;
import me.zhanghai.android.materialfilemanager.util.MapCompat;

public class Archive {

    @NonNull
    private static final ArchiveStreamFactory sArchiveStreamFactory = new ArchiveStreamFactory();

    @NonNull
    private static final Map<String, Map<Uri, List<Information>>> sTreeCache =
            new ConcurrentHashMap<>();

    @NonNull
    public static Uri.Builder pathBuilderForRoot() {
        return new Uri.Builder()
                .path("/");
    }

    @NonNull
    public static Uri pathForRoot() {
        return pathBuilderForRoot().build();
    }

    @NonNull
    public static Uri pathFromString(@NonNull String path) {
        return new Uri.Builder()
                .path(path)
                .build();
    }

    private Archive() {}

    public static void retainCache(@NonNull Collection<String> archivePaths) {
        sTreeCache.keySet().retainAll(archivePaths);
    }

    public static void invalidateCache(@NonNull String archivePath) {
        sTreeCache.remove(archivePath);
    }

    @NonNull
    public static Map<Uri, List<Information>> readTree(@NonNull String archivePath)
            throws ArchiveException, IOException {
        Map<Uri, List<Information>> tree = sTreeCache.get(archivePath);
        if (tree == null) {
            List<ArchiveEntry> entries = readEntries(archivePath);
            List<Information> informations = Functional.map(entries, entry -> new Information(
                    makePath(entry), entry));
            tree = new HashMap<>();
            tree.put(pathForRoot(), new ArrayList<>());
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
            Set<Uri> directoryPaths = new HashSet<>(directoryInformationExists.keySet());
            for (Uri directoryPath : directoryPaths) {
                Uri.Builder builder = pathBuilderForRoot();
                Uri parentPath = builder.build();
                for (String pathSegment : directoryPath.getPathSegments()) {
                    builder.appendPath(pathSegment);
                    Uri path = builder.build();
                    if (!MapCompat.getOrDefault(directoryInformationExists, path, false)) {
                        ArchiveEntry entry = new ArchiveDirectoryEntry(path.getPath());
                        Information information = new Information(path, entry);
                        tree.get(parentPath).add(information);
                        directoryInformationExists.put(path, true);
                    }
                    MapCompat.computeIfAbsent(tree, path, _1 -> new ArrayList<>());
                    parentPath = path;
                }
            }
            sTreeCache.put(archivePath, tree);
        }
        return tree;
    }

    @NonNull
    private static List<ArchiveEntry> readEntries(@NonNull String archivePath)
            throws ArchiveException, IOException {
        File archiveFile = new File(archivePath);
        String type;
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(archiveFile))) {
            type = ArchiveStreamFactory.detect(inputStream);
        }
        ArrayList<ArchiveEntry> entries = new ArrayList<>();
        switch (type) {
            case ArchiveStreamFactory.ZIP: {
                String encoding = SettingsLiveDatas.ZIP_FILE_NAME_ENCODING.getValue();
                try (ZipFileCompat zipFile = new ZipFileCompat(archiveFile, encoding)) {
                    FunctionalIterator.forEachRemaining(zipFile.getEntries(),
                            (Consumer<ZipArchiveEntry>) entries::add);
                }
                break;
            }
            case ArchiveStreamFactory.SEVEN_Z: {
                try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
                    Functional.forEach(sevenZFile.getEntries(),
                            (Consumer<SevenZArchiveEntry>) entries::add);
                }
                break;
            }
            default: {
                try (FileInputStream fileInputStream = new FileInputStream(archiveFile)) {
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

    @NonNull
    private static Uri makePath(@NonNull ArchiveEntry entry) {
        String name = entry.getName();
        Uri.Builder pathBuilder = pathBuilderForRoot();
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
            pathBuilder.appendPath(name.substring(startIndex, endIndex));
            startIndex = endIndex;
        }
        return pathBuilder.build();
    }

    @NonNull
    private static Uri getParentPath(@NonNull Uri path) {
        Uri.Builder pathBuilder = pathBuilderForRoot();
        List<String> pathSegments = path.getPathSegments();
        for (int i = 0, count = pathSegments.size() - 1; i < count; ++i) {
            String pathSegment = pathSegments.get(i);
            pathBuilder.appendPath(pathSegment);
        }
        return pathBuilder.build();
    }

    public static class Information {

        @NonNull
        public final Uri path;
        @NonNull
        public final ArchiveEntry entry;

        public Information(@NonNull Uri path, @NonNull ArchiveEntry entry) {
            this.path = path;
            this.entry = entry;
        }

        @Override
        public boolean equals(@Nullable Object object) {
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

        @NonNull
        private String mName;

        public ArchiveDirectoryEntry(@NonNull String name) {
            if (name.endsWith("/")) {
                throw new IllegalArgumentException("Name should not end with a slash: " + name);
            }
            mName = name + "/";
        }

        @NonNull
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

        @NonNull
        @Override
        public Date getLastModifiedDate() {
            return new Date(-1);
        }

        @Override
        public boolean equals(@Nullable Object object) {
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
