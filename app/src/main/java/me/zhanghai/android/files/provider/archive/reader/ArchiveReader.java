/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.reader;

import android.util.Pair;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.charset.StandardCharsets;
import java8.nio.file.NoSuchFileException;
import java8.nio.file.NotLinkException;
import java8.nio.file.Path;
import me.zhanghai.android.files.functional.IterableCompat;
import me.zhanghai.android.files.functional.IteratorCompat;
import me.zhanghai.android.files.provider.common.IsDirectoryException;
import me.zhanghai.android.files.provider.common.PosixFileType;
import me.zhanghai.android.files.provider.common.PosixFileTypes;
import me.zhanghai.android.files.settings.SettingsLiveDatas;
import me.zhanghai.android.files.util.EnumerationCompat;
import me.zhanghai.android.files.util.IoUtils;
import me.zhanghai.android.files.util.MapCompat;

public class ArchiveReader {

    @NonNull
    private static final ArchiveStreamFactory sArchiveStreamFactory = new ArchiveStreamFactory();

    private ArchiveReader() {}

    @NonNull
    public static Pair<Map<Path, ArchiveEntry>, Map<Path, List<Path>>> readEntries(
            @NonNull Path file, @NonNull Path rootPath) throws IOException {
        Map<Path, ArchiveEntry> entries = new HashMap<>();
        List<ArchiveEntry> rawEntries = readEntries(file);
        for (ArchiveEntry entry : rawEntries) {
            Path path = rootPath.resolve(entry.getName());
            MapCompat.putIfAbsent(entries, path, entry);
        }
        if (!entries.containsKey(rootPath)) {
            entries.put(rootPath, new DirectoryArchiveEntry(""));
        }
        Map<Path, List<Path>> tree = new HashMap<>();
        tree.put(rootPath, new ArrayList<>());
        List<Path> paths = new ArrayList<>(entries.keySet());
        for (Path path : paths) {
            while (true) {
                Path parentPath = path.getParent();
                if (parentPath == null) {
                    break;
                }
                ArchiveEntry entry = entries.get(path);
                if (entry.isDirectory()) {
                    MapCompat.computeIfAbsent(tree, path, _1 -> new ArrayList<>());
                }
                MapCompat.computeIfAbsent(tree, parentPath, _1 -> new ArrayList<>())
                        .add(path);
                if (entries.containsKey(parentPath)) {
                    break;
                }
                entries.put(parentPath, new DirectoryArchiveEntry(parentPath.toString()));
                path = parentPath;
            }
        }
        return new Pair<>(entries, tree);
    }

    @NonNull
    private static List<ArchiveEntry> readEntries(@NonNull Path file) throws IOException {
        File ioFile = file.toFile();
        String type;
        try (FileInputStream fileInputStream = new FileInputStream(ioFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
            try {
                type = ArchiveStreamFactory.detect(bufferedInputStream);
            } catch (org.apache.commons.compress.archivers.ArchiveException e) {
                throw new ArchiveException(e);
            }
        } catch (FileNotFoundException e) {
            NoSuchFileException noSuchFileException = new NoSuchFileException(file.toString());
            noSuchFileException.initCause(e);
            throw noSuchFileException;
        }
        ArrayList<ArchiveEntry> entries = new ArrayList<>();
        switch (type) {
            case ArchiveStreamFactory.ZIP: {
                String encoding = SettingsLiveDatas.ZIP_FILE_NAME_ENCODING.getValue();
                try (ZipFileCompat zipFile = new ZipFileCompat(ioFile, encoding)) {
                    IteratorCompat.forEachRemaining(EnumerationCompat.asIterator(
                            zipFile.getEntries()), entries::add);
                }
                break;
            }
            case ArchiveStreamFactory.SEVEN_Z: {
                try (SevenZFile sevenZFile = new SevenZFile(ioFile)) {
                    IterableCompat.forEach(sevenZFile.getEntries(), entries::add);
                }
                break;
            }
            default: {
                try (FileInputStream fileInputStream = new FileInputStream(ioFile);
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(
                             fileInputStream);
                     ArchiveInputStream archiveInputStream =
                             sArchiveStreamFactory.createArchiveInputStream(bufferedInputStream)) {
                    ArchiveEntry entry;
                    while ((entry = archiveInputStream.getNextEntry()) != null) {
                        entries.add(entry);
                    }
                } catch (FileNotFoundException e) {
                    NoSuchFileException noSuchFileException = new NoSuchFileException(
                            file.toString());
                    noSuchFileException.initCause(e);
                    throw noSuchFileException;
                } catch (org.apache.commons.compress.archivers.ArchiveException e) {
                    throw new ArchiveException(e);
                }
            }
        }
        return entries;
    }

    @NonNull
    public static InputStream newInputStream(@NonNull Path file, @NonNull ArchiveEntry entry)
            throws IOException {
        if (entry.isDirectory()) {
            throw new IsDirectoryException(file.toString());
        }
        File ioFile = file.toFile();
        if (entry instanceof ZipArchiveEntry) {
            ZipArchiveEntry zipEntry = (ZipArchiveEntry) entry;
            String encoding = SettingsLiveDatas.ZIP_FILE_NAME_ENCODING.getValue();
            boolean successful = false;
            ZipFileCompat zipFile = null;
            InputStream zipEntryInputStream = null;
            try {
                zipFile = new ZipFileCompat(ioFile, encoding);
                zipEntryInputStream = zipFile.getInputStream(zipEntry);
                if (zipEntryInputStream == null) {
                    throw new NoSuchFileException(file.toString());
                }
                InputStream inputStream = new ZipFileEntryInputStream(zipFile, zipEntryInputStream);
                successful = true;
                return inputStream;
            } finally {
                if (!successful) {
                    if (zipEntryInputStream != null) {
                        zipEntryInputStream.close();
                    }
                    if (zipFile != null) {
                        zipFile.close();
                    }
                }
            }
        } else if (entry instanceof SevenZArchiveEntry) {
            boolean successful = false;
            SevenZFile sevenZFile = null;
            try {
                sevenZFile = new SevenZFile(ioFile);
                SevenZArchiveEntry currentEntry;
                while ((currentEntry = sevenZFile.getNextEntry()) != null) {
                    if (!Objects.equals(currentEntry.getName(), entry.getName())) {
                        continue;
                    }
                    InputStream inputStream = new SevenZArchiveEntryInputStream(sevenZFile,
                            currentEntry);
                    successful = true;
                    return inputStream;
                }
                throw new NoSuchFileException(file.toString());
            } finally {
                if (!successful && sevenZFile != null) {
                    sevenZFile.close();
                }
            }
        } else {
            boolean successful = false;
            FileInputStream fileInputStream = null;
            BufferedInputStream bufferedInputStream = null;
            ArchiveInputStream archiveInputStream = null;
            try {
                fileInputStream = new FileInputStream(ioFile);
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                archiveInputStream = sArchiveStreamFactory.createArchiveInputStream(
                        bufferedInputStream);
                ArchiveEntry currentEntry;
                while ((currentEntry = archiveInputStream.getNextEntry()) != null) {
                    if (!Objects.equals(currentEntry.getName(), entry.getName())) {
                        continue;
                    }
                    successful = true;
                    return archiveInputStream;
                }
                throw new NoSuchFileException(file.toString());
            } catch (FileNotFoundException e) {
                NoSuchFileException noSuchFileException = new NoSuchFileException(
                        file.toString());
                noSuchFileException.initCause(e);
                throw noSuchFileException;
            } catch (org.apache.commons.compress.archivers.ArchiveException e) {
                throw new ArchiveException(e);
            } finally {
                if (!successful) {
                    if (archiveInputStream != null) {
                        archiveInputStream.close();
                    } else if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    } else if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                }
            }
        }
    }

    @NonNull
    public static String readSymbolicLink(@NonNull Path file, @NonNull ArchiveEntry entry)
            throws IOException {
        if (!isSymbolicLink(entry)) {
            throw new NotLinkException(file.toString());
        }
        if (entry instanceof TarArchiveEntry) {
            TarArchiveEntry tarEntry = (TarArchiveEntry) entry;
            return tarEntry.getLinkName();
        } else {
            try (InputStream inputStream = newInputStream(file, entry)) {
                return IoUtils.inputStreamToString(inputStream, StandardCharsets.UTF_8);
            }
        }
    }

    private static boolean isSymbolicLink(@NonNull ArchiveEntry entry) {
        return PosixFileTypes.fromArchiveEntry(entry) == PosixFileType.SYMBOLIC_LINK;
    }

    private static class DirectoryArchiveEntry implements ArchiveEntry {

        @NonNull
        private String mName;

        public DirectoryArchiveEntry(@NonNull String name) {
            if (name.endsWith("/")) {
                throw new IllegalArgumentException("name should not end with a slash: " + name);
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
            DirectoryArchiveEntry that = (DirectoryArchiveEntry) object;
            return Objects.equals(mName, that.mName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mName);
        }
    }

    private static class ZipFileEntryInputStream extends InputStream {

        @NonNull
        private final ZipFileCompat mFile;
        @NonNull
        private final InputStream mInputStream;

        public ZipFileEntryInputStream(@NonNull ZipFileCompat file,
                                       @NonNull InputStream inputStream) {
            mFile = file;
            mInputStream = inputStream;
        }

        @Override
        public int available() throws IOException {
            return mInputStream.available();
        }

        @Override
        public int read() throws IOException {
            return mInputStream.read();
        }

        @Override
        public int read(@NonNull byte[] b) throws IOException {
            return mInputStream.read(b);
        }

        @Override
        public int read(@NonNull byte[] b, int off, int len) throws IOException {
            return mInputStream.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            mInputStream.close();
            mFile.close();
        }
    }

    private static class SevenZArchiveEntryInputStream extends InputStream {

        @NonNull
        private final SevenZFile mFile;
        @NonNull
        private final SevenZArchiveEntry mEntry;

        public SevenZArchiveEntryInputStream(@NonNull SevenZFile file,
                                             @NonNull SevenZArchiveEntry entry) {
            mFile = file;
            mEntry = entry;
        }

        @Override
        public int available() {
            long size = mEntry.getSize();
            long read = mFile.getStatisticsForCurrentEntry()
                    .getUncompressedCount();
            long available = size - read;
            return (int) Math.min(available, Integer.MAX_VALUE);
        }

        @Override
        public int read() throws IOException {
            return mFile.read();
        }

        @Override
        public int read(@NonNull byte[] b) throws IOException {
            return mFile.read(b);
        }

        @Override
        public int read(@NonNull byte[] b, int off, int len) throws IOException {
            return mFile.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            mFile.close();
        }
    }
}
