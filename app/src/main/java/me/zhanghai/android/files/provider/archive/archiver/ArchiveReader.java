/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Pair;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

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
import androidx.preference.PreferenceManager;
import eu.chainfire.librootjava.RootJava;
import java8.nio.charset.StandardCharsets;
import java8.nio.file.AccessMode;
import java8.nio.file.NoSuchFileException;
import java8.nio.file.NotLinkException;
import java8.nio.file.Path;
import java9.lang.Iterables;
import java9.util.Iterators;
import me.zhanghai.android.files.BuildConfig;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.EnumerationCompat;
import me.zhanghai.android.files.compat.MapCompat;
import me.zhanghai.android.files.provider.common.IsDirectoryException;
import me.zhanghai.android.files.provider.common.MoreFiles;
import me.zhanghai.android.files.provider.common.PosixFileType;
import me.zhanghai.android.files.provider.common.PosixFileTypes;
import me.zhanghai.android.files.provider.root.RootUtils;
import me.zhanghai.android.files.settings.SettingsLiveDatas;
import me.zhanghai.android.files.util.IoUtils;

public class ArchiveReader {

    @NonNull
    private static final CompressorStreamFactory sCompressorStreamFactory =
            new CompressorStreamFactory();
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
        String compressorType;
        String archiveType;
        try (FileInputStream fileInputStream = new FileInputStream(ioFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
            try {
                compressorType = CompressorStreamFactory.detect(bufferedInputStream);
            } catch (CompressorException e) {
                // Ignored.
                compressorType = null;
            }
            try (BufferedInputStream bufferedCompressorInputStream = compressorType != null ?
                    new BufferedInputStream(sCompressorStreamFactory.createCompressorInputStream(
                            compressorType, bufferedInputStream)) : bufferedInputStream) {
                archiveType = ArchiveStreamFactory.detect(bufferedCompressorInputStream);
            } catch (org.apache.commons.compress.archivers.ArchiveException e) {
                throw new ArchiveException(e);
            } catch (CompressorException e) {
                throw new ArchiveException(e);
            }
        } catch (FileNotFoundException e) {
            MoreFiles.checkAccess(file, AccessMode.READ);
            NoSuchFileException noSuchFileException = new NoSuchFileException(file.toString());
            noSuchFileException.initCause(e);
            throw noSuchFileException;
        }
        String encoding = getArchiveFileNameEncoding();
        ArrayList<ArchiveEntry> entries = new ArrayList<>();
        if (compressorType == null) {
            switch (archiveType) {
                case ArchiveStreamFactory.ZIP:
                    try (ZipFileCompat zipFile = new ZipFileCompat(ioFile, encoding)) {
                        Iterators.forEachRemaining(EnumerationCompat.asIterator(
                                zipFile.getEntries()), entries::add);
                        return entries;
                    }
                case ArchiveStreamFactory.SEVEN_Z:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        throw new IOException(new UnsupportedOperationException("SevenZFile"));
                    }
                    try (SevenZFile sevenZFile = new SevenZFile(ioFile)) {
                        Iterables.forEach(sevenZFile.getEntries(), entries::add);
                        return entries;
                    }
            }
        }
        try (FileInputStream fileInputStream = new FileInputStream(ioFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             InputStream compressorInputStream = compressorType != null ?
                     sCompressorStreamFactory.createCompressorInputStream(compressorType,
                             bufferedInputStream) : bufferedInputStream;
             ArchiveInputStream archiveInputStream = sArchiveStreamFactory.createArchiveInputStream(
                     archiveType, compressorInputStream, encoding)) {
            ArchiveEntry entry;
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                entries.add(entry);
            }
            return entries;
        } catch (FileNotFoundException e) {
            NoSuchFileException noSuchFileException = new NoSuchFileException(
                    file.toString());
            noSuchFileException.initCause(e);
            throw noSuchFileException;
        } catch (org.apache.commons.compress.archivers.ArchiveException e) {
            throw new ArchiveException(e);
        } catch (CompressorException e) {
            throw new ArchiveException(e);
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
            return SettingsLiveDatas.ARCHIVE_FILE_NAME_ENCODING.getValue();
        }
    }

    @NonNull
    public static InputStream newInputStream(@NonNull Path file, @NonNull ArchiveEntry entry)
            throws IOException {
        if (entry.isDirectory()) {
            throw new IsDirectoryException(file.toString());
        }
        File ioFile = file.toFile();
        String compressorType;
        String archiveType;
        try (FileInputStream fileInputStream = new FileInputStream(ioFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
            try {
                compressorType = CompressorStreamFactory.detect(bufferedInputStream);
            } catch (CompressorException e) {
                // Ignored.
                compressorType = null;
            }
            try (BufferedInputStream bufferedCompressorInputStream = compressorType != null ?
                    new BufferedInputStream(sCompressorStreamFactory.createCompressorInputStream(
                            compressorType, bufferedInputStream)) : bufferedInputStream) {
                archiveType = ArchiveStreamFactory.detect(bufferedCompressorInputStream);
            } catch (org.apache.commons.compress.archivers.ArchiveException e) {
                throw new ArchiveException(e);
            } catch (CompressorException e) {
                throw new ArchiveException(e);
            }
        } catch (FileNotFoundException e) {
            MoreFiles.checkAccess(file, AccessMode.READ);
            NoSuchFileException noSuchFileException = new NoSuchFileException(file.toString());
            noSuchFileException.initCause(e);
            throw noSuchFileException;
        }
        String encoding = getArchiveFileNameEncoding();
        if (compressorType == null) {
            if (entry instanceof ZipArchiveEntry) {
                ZipArchiveEntry zipEntry = (ZipArchiveEntry) entry;
                boolean successful = false;
                ZipFileCompat zipFile = null;
                InputStream zipEntryInputStream = null;
                try {
                    zipFile = new ZipFileCompat(ioFile, encoding);
                    zipEntryInputStream = zipFile.getInputStream(zipEntry);
                    if (zipEntryInputStream == null) {
                        throw new NoSuchFileException(file.toString());
                    }
                    InputStream inputStream = new ZipFileEntryInputStream(zipFile,
                            zipEntryInputStream);
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
            }
        }
        boolean successful = false;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        InputStream compressorInputStream = null;
        ArchiveInputStream archiveInputStream = null;
        try {
            fileInputStream = new FileInputStream(ioFile);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            compressorInputStream = compressorType != null ?
                    sCompressorStreamFactory.createCompressorInputStream(compressorType,
                            bufferedInputStream) : bufferedInputStream;
            archiveInputStream = sArchiveStreamFactory.createArchiveInputStream(archiveType,
                    compressorInputStream, encoding);
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
        } catch (CompressorException e) {
            throw new ArchiveException(e);
        } finally {
            if (!successful) {
                if (archiveInputStream != null) {
                    archiveInputStream.close();
                }
                if (compressorInputStream != null) {
                    compressorInputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
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
        public boolean equals(Object object) {
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
