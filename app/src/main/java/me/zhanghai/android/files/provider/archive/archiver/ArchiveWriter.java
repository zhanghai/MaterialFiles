/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver;

import android.os.Build;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import java9.util.function.LongConsumer;
import me.zhanghai.android.files.compat.SeekableByteChannelCompat;
import me.zhanghai.android.files.provider.common.MoreFiles;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.java.reflected.ReflectedField;

public class ArchiveWriter implements Closeable {

    private static final ReflectedField<TarArchiveEntry> sTarArchiveEntryLinkFlagsField =
            new ReflectedField<>(TarArchiveEntry.class, "linkFlag");

    @NonNull
    private static final CompressorStreamFactory sCompressorStreamFactory =
            new CompressorStreamFactory();
    @NonNull
    private static final ArchiveStreamFactory sArchiveStreamFactory = new ArchiveStreamFactory();

    @NonNull
    private final ArchiveOutputStream mArchiveOutputStream;

    public ArchiveWriter(@NonNull String archiveType, @Nullable String compressorType,
                         @NonNull SeekableByteChannel channel) throws IOException {
        switch (archiveType) {
            case ArchiveStreamFactory.SEVEN_Z:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    throw new IOException(new UnsupportedOperationException("SevenZOutputFile"));
                }
                mArchiveOutputStream = new SevenZArchiveOutputStream(new SevenZOutputFile(
                        SeekableByteChannelCompat.from(channel)));
                break;
            default: {
                boolean successful = false;
                OutputStream outputStream = null;
                BufferedOutputStream bufferedOutputStream = null;
                OutputStream compressorOutputStream = null;
                try {
                    outputStream = Channels.newOutputStream(channel);
                    bufferedOutputStream = new BufferedOutputStream(outputStream);
                    compressorOutputStream = compressorType != null ?
                            sCompressorStreamFactory.createCompressorOutputStream(compressorType,
                                    bufferedOutputStream) : bufferedOutputStream;
                    // Use the platform default encoding (which is UTF-8) instead of the user-set
                    // one, because that one is for reading archives instead of creating.
                    mArchiveOutputStream = sArchiveStreamFactory.createArchiveOutputStream(
                            archiveType, compressorOutputStream);
                    successful = true;
                } catch (org.apache.commons.compress.archivers.ArchiveException e) {
                    throw new ArchiveException(e);
                } catch (CompressorException e) {
                    throw new ArchiveException(e);
                } finally {
                    if (!successful) {
                        if (compressorOutputStream != null) {
                            compressorOutputStream.close();
                        }
                        if (bufferedOutputStream != null) {
                            bufferedOutputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                }
            }
        }
    }

    public void write(@NonNull Path file, @NonNull Path entryName, @Nullable LongConsumer listener,
                      long intervalMillis) throws IOException {
        ArchiveEntry entry = mArchiveOutputStream.createArchiveEntry(new PathFile(file),
                entryName.toString());
        BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        boolean writeData;
        if (attributes.isRegularFile()) {
            writeData = true;
        } else if (attributes.isDirectory()) {
            writeData = false;
        } else {
            if (attributes.isSymbolicLink()) {
                if (entry instanceof ZipArchiveEntry) {
                    ZipArchiveEntry zipEntry = (ZipArchiveEntry) entry;
                    zipEntry.setUnixMode(UnixStat.LINK_FLAG | UnixStat.DEFAULT_LINK_PERM);
                    writeData = true;
                } else if (entry instanceof TarArchiveEntry) {
                    TarArchiveEntry tarEntry = (TarArchiveEntry) entry;
                    sTarArchiveEntryLinkFlagsField.setByte(tarEntry, TarConstants.LF_SYMLINK);
                    tarEntry.setLinkName(MoreFiles.readSymbolicLink(file).toString());
                    writeData = false;
                } else {
                    throw new IOException(new UnsupportedOperationException("symbolic link"));
                }
            } else {
                throw new IOException(new UnsupportedOperationException("type"));
            }
        }
        if (entry instanceof TarArchiveEntry && attributes instanceof PosixFileAttributes) {
            TarArchiveEntry tarEntry = (TarArchiveEntry) entry;
            PosixFileAttributes posixAttributes = (PosixFileAttributes) attributes;
            Set<PosixFileModeBit> mode = posixAttributes.mode();
            if (mode != null) {
                tarEntry.setMode(PosixFileMode.toInt(mode));
            }
            PosixUser owner = posixAttributes.owner();
            if (owner != null) {
                tarEntry.setUserId(owner.getId());
                String ownerName = owner.getName();
                if (ownerName != null) {
                    tarEntry.setUserName(ownerName);
                }
            }
            PosixGroup group = posixAttributes.group();
            if (group != null) {
                tarEntry.setGroupId(group.getId());
                String groupName = group.getName();
                if (groupName != null) {
                    tarEntry.setGroupName(groupName);
                }
            }
        }
        mArchiveOutputStream.putArchiveEntry(entry);
        boolean listenerNotified = false;
        if (writeData) {
            if (attributes.isSymbolicLink()) {
                byte[] target = MoreFiles.readSymbolicLink(file).getOwnedBytes();
                mArchiveOutputStream.write(target);
            } else {
                try (InputStream inputStream = MoreFiles.newInputStream(file,
                        LinkOption.NOFOLLOW_LINKS)) {
                    MoreFiles.copy(inputStream, mArchiveOutputStream, listener, intervalMillis);
                }
                listenerNotified = true;
            }
        }
        mArchiveOutputStream.closeArchiveEntry();
        if (listener != null && !listenerNotified) {
            listener.accept(attributes.size());
        }
    }

    @Override
    public void close() throws IOException {
        mArchiveOutputStream.finish();
        mArchiveOutputStream.close();
    }

    private static class SevenZArchiveOutputStream extends ArchiveOutputStream {

        @NonNull
        private final SevenZOutputFile mFile;

        public SevenZArchiveOutputStream(@NonNull SevenZOutputFile file) {
            mFile = file;
        }

        @NonNull
        @Override
        public ArchiveEntry createArchiveEntry(@NonNull File file, @NonNull String entryName)
                throws IOException {
            return mFile.createArchiveEntry(file, entryName);
        }

        @Override
        public void putArchiveEntry(@NonNull ArchiveEntry entry) throws IOException {
            mFile.putArchiveEntry(entry);
        }

        @Override
        public void write(int b) throws IOException {
            mFile.write(b);
        }

        @Override
        public void write(@NonNull byte[] b) throws IOException {
            mFile.write(b);
        }

        @Override
        public void write(@NonNull byte[] b, int off, int len) throws IOException {
            mFile.write(b, off, len);
        }

        @Override
        public void closeArchiveEntry() throws IOException {
            mFile.closeArchiveEntry();
        }

        @Override
        public void finish() throws IOException {
            mFile.finish();
        }

        @Override
        public void close() throws IOException {
            mFile.close();
        }
    }

    // {@link ArchiveOutputStream#createArchiveEntry(File, String)} doesn't actually need a real
    // file.
    private static class PathFile extends File {

        @NonNull
        private final Path mPath;

        public PathFile(@NonNull Path path) {
            super(path.toString());

            mPath = path;
        }

        @Override
        public boolean isDirectory() {
            return Files.isDirectory(mPath, LinkOption.NOFOLLOW_LINKS);
        }

        @Override
        public boolean isFile() {
            return Files.isRegularFile(mPath, LinkOption.NOFOLLOW_LINKS);
        }

        @Override
        public long lastModified() {
            try {
                return Files.getLastModifiedTime(mPath, LinkOption.NOFOLLOW_LINKS).toMillis();
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        public long length() {
            try {
                return MoreFiles.size(mPath, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }
}
