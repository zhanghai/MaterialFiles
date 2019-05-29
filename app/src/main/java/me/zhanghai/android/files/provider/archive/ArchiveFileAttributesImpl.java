/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.threeten.bp.Instant;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixFileType;
import me.zhanghai.android.files.provider.common.PosixFileTypes;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;

class ArchiveFileAttributesImpl implements PosixFileAttributes {

    @NonNull
    private final Path mArchiveFile;
    @NonNull
    private final ArchiveEntry mEntry;

    ArchiveFileAttributesImpl(@NonNull Path archiveFile, @NonNull ArchiveEntry entry) {
        mArchiveFile = archiveFile;
        mEntry = entry;
    }

    @NonNull
    public String getEntryName() {
        return mEntry.getName();
    }

    @NonNull
    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(Instant.ofEpochMilli(mEntry.getLastModifiedDate().getTime()));
    }

    @NonNull
    @Override
    public FileTime lastAccessTime() {
        if (mEntry instanceof DumpArchiveEntry) {
            DumpArchiveEntry dumpEntry = (DumpArchiveEntry) mEntry;
            return FileTime.from(Instant.ofEpochMilli(dumpEntry.getAccessTime().getTime()));
        } else if (mEntry instanceof SevenZArchiveEntry) {
            SevenZArchiveEntry sevenZEntry = (SevenZArchiveEntry) mEntry;
            if (sevenZEntry.getHasAccessDate()) {
                return FileTime.from(Instant.ofEpochMilli(sevenZEntry.getAccessDate().getTime()));
            }
        } else if (mEntry instanceof TarArchiveEntry) {
            TarArchiveEntry tarEntry = (TarArchiveEntry) mEntry;
            Long atimeMillis = getTarEntryTimeMillis(tarEntry, "atime");
            if (atimeMillis != null) {
                return FileTime.from(Instant.ofEpochMilli(atimeMillis));
            }
        }
        return lastModifiedTime();
    }

    @NonNull
    @Override
    public FileTime creationTime() {
        if (mEntry instanceof DumpArchiveEntry) {
            DumpArchiveEntry dumpEntry = (DumpArchiveEntry) mEntry;
            return FileTime.from(Instant.ofEpochMilli(dumpEntry.getCreationTime().getTime()));
        } else if (mEntry instanceof SevenZArchiveEntry) {
            SevenZArchiveEntry sevenZEntry = (SevenZArchiveEntry) mEntry;
            if (sevenZEntry.getHasCreationDate()) {
                return FileTime.from(Instant.ofEpochMilli(sevenZEntry.getCreationDate().getTime()));
            }
        } else if (mEntry instanceof TarArchiveEntry) {
            TarArchiveEntry tarEntry = (TarArchiveEntry) mEntry;
            Long ctimeMillis = getTarEntryTimeMillis(tarEntry, "ctime");
            if (ctimeMillis != null) {
                return FileTime.from(Instant.ofEpochMilli(ctimeMillis));
            }
        }
        return lastModifiedTime();
    }

    @Nullable
    private static Long getTarEntryTimeMillis(@NonNull TarArchiveEntry entry,
                                              @NonNull String name) {
        String atime = entry.getExtraPaxHeader(name);
        if (atime == null) {
            return null;
        }
        double atimeSeconds;
        try {
            atimeSeconds = Double.parseDouble(atime);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return (long) (atimeSeconds * 1000);
    }

    @NonNull
    public PosixFileType type() {
        return PosixFileTypes.fromArchiveEntry(mEntry);
    }

    @Override
    public long size() {
        return mEntry.getSize();
    }

    @NonNull
    @Override
    public ArchiveFileKey fileKey() {
        return new ArchiveFileKey(mArchiveFile, mEntry.getName());
    }

    @Nullable
    @Override
    public PosixUser owner() {
        if (mEntry instanceof DumpArchiveEntry) {
            DumpArchiveEntry dumpEntry = (DumpArchiveEntry) mEntry;
            //noinspection deprecation
            return new PosixUser(dumpEntry.getUserId(), null);
        } else if (mEntry instanceof TarArchiveEntry) {
            TarArchiveEntry tarEntry = (TarArchiveEntry) mEntry;
            //noinspection deprecation
            return new PosixUser(tarEntry.getUserId(), ByteString.fromStringOrNull(
                    tarEntry.getUserName()));
        }
        return null;
    }

    @Nullable
    @Override
    public PosixGroup group() {
        if (mEntry instanceof DumpArchiveEntry) {
            DumpArchiveEntry dumpEntry = (DumpArchiveEntry) mEntry;
            //noinspection deprecation
            return new PosixGroup(dumpEntry.getGroupId(), null);
        } else if (mEntry instanceof TarArchiveEntry) {
            TarArchiveEntry tarEntry = (TarArchiveEntry) mEntry;
            //noinspection deprecation
            return new PosixGroup(tarEntry.getGroupId(), ByteString.fromStringOrNull(
                    tarEntry.getGroupName()));
        }
        return null;
    }

    @Nullable
    public Set<PosixFileModeBit> mode() {
        if (mEntry instanceof DumpArchiveEntry) {
            DumpArchiveEntry dumpEntry = (DumpArchiveEntry) mEntry;
            return PosixFileMode.fromInt(dumpEntry.getMode());
        } else if (mEntry instanceof TarArchiveEntry) {
            TarArchiveEntry tarEntry = (TarArchiveEntry) mEntry;
            return PosixFileMode.fromInt(tarEntry.getMode());
        } else if (mEntry instanceof ZipArchiveEntry) {
            ZipArchiveEntry zipEntry = (ZipArchiveEntry) mEntry;
            if (zipEntry.getPlatform() == ZipArchiveEntry.PLATFORM_UNIX) {
                return PosixFileMode.fromInt(zipEntry.getUnixMode());
            }
        }
        return null;
    }

    @Nullable
    @Override
    public ByteString seLinuxContext() {
        return null;
    }
}
