/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver_sevenzipjbinding;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.threeten.bp.Instant;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import eu.chainfire.librootjava.RootJava;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.charset.StandardCharsets;
import java8.nio.file.NoSuchFileException;
import java8.nio.file.NotLinkException;
import java8.nio.file.Path;
import me.zhanghai.android.files.BuildConfig;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.MapCompat;
import me.zhanghai.android.files.provider.common.IsDirectoryException;
import me.zhanghai.android.files.provider.common.MoreFiles;
import me.zhanghai.android.files.provider.common.PosixFileType;
import me.zhanghai.android.files.provider.common.PosixFileTypes;
import me.zhanghai.android.files.provider.root.RootUtils;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.util.IoUtils;

public class ArchiveReader {

    private ArchiveReader() {}

    @NonNull
    public static Pair<Map<Path, ArchiveItem>, Map<Path, List<Path>>> readItems(
            @NonNull Path file, @NonNull Path rootPath) throws IOException {
        Map<Path, ArchiveItem> items = new HashMap<>();
        List<ArchiveItem> rawItems = readItems(file);
        for (ArchiveItem item : rawItems) {
            Path path = rootPath.resolve(item.getPath());
            // Normalize an absolute path to prevent path traversal attack.
            if (!path.isAbsolute()) {
                throw new AssertionError("Path must be absolute: " + path.toString());
            }
            if (path.getNameCount() > 0) {
                path = path.normalize();
                if (path.getNameCount() == 0) {
                    // Don't allow a path to become the root path only after normalization.
                    continue;
                }
            }
            MapCompat.putIfAbsent(items, path, item);
        }
        if (!items.containsKey(rootPath)) {
            items.put(rootPath, new DirectoryArchiveItem("/"));
        }
        Map<Path, List<Path>> tree = new HashMap<>();
        tree.put(rootPath, new ArrayList<>());
        List<Path> paths = new ArrayList<>(items.keySet());
        for (Path path : paths) {
            while (true) {
                Path parentPath = path.getParent();
                if (parentPath == null) {
                    break;
                }
                ArchiveItem item = items.get(path);
                if (item.isDirectory()) {
                    MapCompat.computeIfAbsent(tree, path, _1 -> new ArrayList<>());
                }
                MapCompat.computeIfAbsent(tree, parentPath, _1 -> new ArrayList<>())
                        .add(path);
                if (items.containsKey(parentPath)) {
                    break;
                }
                items.put(parentPath, new DirectoryArchiveItem(parentPath.toString()));
                path = parentPath;
            }
        }
        return new Pair<>(items, tree);
    }

    @NonNull
    private static List<ArchiveItem> readItems(@NonNull Path file) throws IOException {
        try (SeekableByteChannel channel = MoreFiles.newByteChannel(file);
             IInArchive archive = SevenZip.openInArchive(null, new ByteChannelInStream(channel))) {
            List<ArchiveItem> items = new ArrayList<>();
            for (ISimpleInArchiveItem item : archive.getSimpleInterface().getArchiveItems()) {
                items.add(new SimpleArchiveItem(archive, item));
            }
            return items;
        }
    }

    @NonNull
    private static String getArchiveFileNameEncoding() {
        if (RootUtils.isRunningAsRoot()) {
            try {
                Context context = RootJava.getPackageContext(BuildConfig.APPLICATION_ID);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                        context);
                String key = context.getString(R.string.pref_key_archive_file_name_encoding);
                String defaultValue = context.getString(
                        R.string.pref_default_value_archive_file_name_encoding);
                return sharedPreferences.getString(key, defaultValue);
            } catch (Exception e) {
                e.printStackTrace();
                return StandardCharsets.UTF_8.name();
            }
        } else {
            return Settings.ARCHIVE_FILE_NAME_ENCODING.getValue();
        }
    }

    @NonNull
    public static InputStream newInputStream(@NonNull Path file, @NonNull ArchiveItem item)
            throws IOException {
        if (item.isDirectory()) {
            throw new IsDirectoryException(file.toString());
        }
        try (SeekableByteChannel channel = MoreFiles.newByteChannel(file);
             IInArchive archive = SevenZip.openInArchive(null, new ByteChannelInStream(channel))) {
            for (ISimpleInArchiveItem simpleItem : archive.getSimpleInterface().getArchiveItems()) {
                if (Objects.equals(simpleItem.getPath(), item.getPath())) {
                    PipedInputStream inputStream = new PipedInputStream();
                    // TODO: Might deadlock if on same thread?
                    simpleItem.extractSlow(new StreamOutStream(new PipedOutputStream(inputStream)));
                    return inputStream;
                }
            }
        }
        throw new NoSuchFileException(file.toString());
    }

    @NonNull
    public static String readSymbolicLink(@NonNull Path file, @NonNull ArchiveItem entry)
            throws IOException {
        if (!isSymbolicLink(entry)) {
            throw new NotLinkException(file.toString());
        }
        try (InputStream inputStream = newInputStream(file, entry)) {
            return IoUtils.inputStreamToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    private static boolean isSymbolicLink(@NonNull ArchiveItem item) {
        return PosixFileTypes.fromArchiveItem(item) == PosixFileType.SYMBOLIC_LINK;
    }

    private static class DirectoryArchiveItem implements ArchiveItem {

        @NonNull
        private String mPath;

        public DirectoryArchiveItem(@NonNull String path) {
            mPath = path;
        }

        @NonNull
        @Override
        public String getPath() {
            return mPath;
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public long getPackedSize() {
            return 0;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public int getAttributes() {
            return 0;
        }

        @Nullable
        @Override
        public Instant getCreationTime() {
            return null;
        }

        @Nullable
        @Override
        public Instant getLastAccessTime() {
            return null;
        }

        @Nullable
        @Override
        public Instant getLastModifiedTime() {
            return null;
        }

        @Override
        public boolean isEncrypted() {
            return false;
        }

        @Override
        public boolean isCommented() {
            return false;
        }

        @Nullable
        @Override
        public Integer getCrc() {
            return null;
        }

        @Nullable
        @Override
        public String getMethod() {
            return null;
        }

        @Nullable
        @Override
        public String getHostOs() {
            return null;
        }

        @Nullable
        @Override
        public String getOwner() {
            return null;
        }

        @Nullable
        @Override
        public String getGroup() {
            return null;
        }

        @Nullable
        @Override
        public String getComment() {
            return null;
        }

        @Override
        public int getIndex() {
            return -1;
        }

        @Nullable
        @Override
        public String getLink() {
            return null;
        }
    }
}
