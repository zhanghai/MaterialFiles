/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.system.OsConstants;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.BasicFileAttributes;

public class PosixFileTypes {

    private PosixFileTypes() {}

    @NonNull
    public static PosixFileType fromArchiveEntry(@NonNull ArchiveEntry entry) {
        if (entry instanceof DumpArchiveEntry) {
            return fromDumpArchiveEntry((DumpArchiveEntry) entry);
        } else if (entry instanceof TarArchiveEntry) {
            return fromTarArchiveEntry((TarArchiveEntry) entry);
        } else if (entry instanceof ZipArchiveEntry) {
            return fromZipArchiveEntry((ZipArchiveEntry) entry);
        } else {
            if (entry.isDirectory()) {
                return PosixFileType.DIRECTORY;
            } else {
                return PosixFileType.REGULAR_FILE;
            }
        }
    }

    @NonNull
    private static PosixFileType fromDumpArchiveEntry(@NonNull DumpArchiveEntry entry) {
        switch (entry.getType()) {
            case SOCKET:
                return PosixFileType.SOCKET;
            case LINK:
                return PosixFileType.SYMBOLIC_LINK;
            case FILE:
                return PosixFileType.REGULAR_FILE;
            case BLKDEV:
                return PosixFileType.BLOCK_DEVICE;
            case DIRECTORY:
                return PosixFileType.DIRECTORY;
            case CHRDEV:
                return PosixFileType.CHARACTER_DEVICE;
            case FIFO:
                return PosixFileType.FIFO;
            case WHITEOUT:
            case UNKNOWN:
            default:
                return PosixFileType.UNKNOWN;
        }
    }

    @NonNull
    private static PosixFileType fromTarArchiveEntry(@NonNull TarArchiveEntry entry) {
        if (entry.isDirectory()) {
            return PosixFileType.DIRECTORY;
        } else if (entry.isFile()) {
            return PosixFileType.REGULAR_FILE;
        } else if (entry.isSymbolicLink()) {
            return PosixFileType.SYMBOLIC_LINK;
        } else if (entry.isCharacterDevice()) {
            return PosixFileType.CHARACTER_DEVICE;
        } else if (entry.isBlockDevice()) {
            return PosixFileType.BLOCK_DEVICE;
        } else if (entry.isFIFO()) {
            return PosixFileType.FIFO;
        } else {
            return PosixFileType.UNKNOWN;
        }
    }

    @NonNull
    private static PosixFileType fromZipArchiveEntry(@NonNull ZipArchiveEntry entry) {
        if (entry.isDirectory()) {
            return PosixFileType.DIRECTORY;
        } else if (entry.isUnixSymlink()) {
            return PosixFileType.SYMBOLIC_LINK;
        } else {
            return PosixFileType.REGULAR_FILE;
        }
    }

    @NonNull
    public static PosixFileType fromMode(int mode) {
        return OsConstants.S_ISDIR(mode) ? PosixFileType.DIRECTORY
                : OsConstants.S_ISCHR(mode) ? PosixFileType.CHARACTER_DEVICE
                : OsConstants.S_ISBLK(mode) ? PosixFileType.BLOCK_DEVICE
                : OsConstants.S_ISREG(mode) ? PosixFileType.REGULAR_FILE
                : OsConstants.S_ISFIFO(mode) ? PosixFileType.FIFO
                : OsConstants.S_ISLNK(mode) ? PosixFileType.SYMBOLIC_LINK
                : OsConstants.S_ISSOCK(mode) ? PosixFileType.SOCKET
                : PosixFileType.UNKNOWN;
    }

    @NonNull
    public static PosixFileType fromFileAttributes(@NonNull BasicFileAttributes attributes) {
        if (attributes instanceof PosixFileAttributes) {
            PosixFileAttributes posixAttributes = (PosixFileAttributes) attributes;
            return posixAttributes.type();
        } else {
            if (attributes.isRegularFile()) {
                return PosixFileType.REGULAR_FILE;
            } else if (attributes.isDirectory()) {
                return PosixFileType.DIRECTORY;
            } else if (attributes.isSymbolicLink()) {
                return PosixFileType.SYMBOLIC_LINK;
            } else {
                return PosixFileType.UNKNOWN;
            }
        }
    }
}
