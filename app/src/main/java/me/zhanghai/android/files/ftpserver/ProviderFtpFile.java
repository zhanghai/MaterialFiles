/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.impl.WriteRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.DirectoryStream;
import java8.nio.file.Files;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileTime;
import java8.nio.file.attribute.PosixFileAttributeView;
import me.zhanghai.android.files.provider.common.MoreFiles;
import me.zhanghai.java.functional.Functional;

class ProviderFtpFile implements Comparable<ProviderFtpFile>, FtpFile {

    @NonNull
    private final Path mPath;
    @NonNull
    private final Path mRelativePath;
    @NonNull
    private final User mUser;

    public ProviderFtpFile(@NonNull Path path, @NonNull Path relativePath, @NonNull User user) {
        mPath = path;
        mRelativePath = relativePath;
        mUser = user;
    }

    @Override
    public String getAbsolutePath() {
        String path = mRelativePath.toString();
        return "/" + path;
    }

    @Override
    public String getName() {
        String name = mRelativePath.getFileName().toString();
        return !name.isEmpty() ? name : "/";
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(mPath);
    }

    @Override
    public boolean isFile() {
        return Files.isRegularFile(mPath);
    }

    @Override
    public boolean doesExist() {
        return Files.exists(mPath);
    }

    @Override
    public boolean isReadable() {
        return Files.isReadable(mPath);
    }

    @Override
    public boolean isWritable() {
        if (mUser.authorize(new WriteRequest(getAbsolutePath())) == null) {
            return false;
        }
        return !Files.exists(mPath) || Files.isWritable(mPath);
    }

    @Override
    public boolean isRemovable() {
        if (mRelativePath.getNameCount() == 1 && mRelativePath.getName(0).toString().isEmpty()) {
            return false;
        }
        if (mUser.authorize(new WriteRequest(getAbsolutePath())) == null) {
            return false;
        }
        return Files.isWritable(mPath.getParent());
    }

    @Override
    public String getOwnerName() {
        String ownerName = null;
        try {
            ownerName = Files.getOwner(mPath).getName();
        } catch (UnsupportedOperationException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ownerName == null) {
            ownerName = "user";
        }
        return ownerName;
    }

    @Override
    public String getGroupName() {
        String groupName = null;
        PosixFileAttributeView attributeView = Files.getFileAttributeView(mPath,
                PosixFileAttributeView.class);
        if (attributeView != null) {
            try {
                groupName = attributeView.readAttributes().group().getName();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (groupName == null) {
            groupName = "group";
        }
        return groupName;
    }

    @Override
    public int getLinkCount() {
        return isDirectory() ? 3 : 1;
    }

    @Override
    public long getLastModified() {
        try {
            return Files.getLastModifiedTime(mPath).toMillis();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean setLastModified(long time) {
        if (!isWritable()) {
            return false;
        }
        try {
            Files.setLastModifiedTime(mPath, FileTime.fromMillis(time));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long getSize() {
        try {
            return Files.size(mPath);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @NonNull
    @Override
    public Path getPhysicalFile() {
        return mPath;
    }

    @Override
    public boolean mkdir() {
        if (!isWritable()) {
            return false;
        }
        try {
            Files.createDirectory(mPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete() {
        if (!isRemovable()) {
            return false;
        }
        try {
            Files.delete(mPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean move(@NonNull FtpFile destination) {
        if (!(isRemovable() && destination.isWritable())) {
            return false;
        }
        Path targetPath = ((ProviderFtpFile) destination).mPath;
        try {
            MoreFiles.move(mPath, targetPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Nullable
    @Override
    public List<ProviderFtpFile> listFiles() {
        DirectoryStream<Path> directoryStream;
        try {
            directoryStream = Files.newDirectoryStream(mPath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        List<ProviderFtpFile> files = Functional.map(directoryStream, path -> new ProviderFtpFile(
                mPath.resolve(path), mRelativePath.resolve(path), mUser));
        Collections.sort(files);
        return files;
    }

    @NonNull
    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        if (!isWritable()) {
            throw new IOException("Not writable: " + getAbsolutePath());
        }
        if (offset == 0) {
            return Files.newOutputStream(mPath);
        } else {
            SeekableByteChannel channel = MoreFiles.newByteChannel(mPath);
            boolean successful = false;
            try {
                channel.position(offset);
                OutputStream outputStream = Channels.newOutputStream(channel);
                successful = true;
                return outputStream;
            } finally {
                if (!successful) {
                    channel.close();
                }
            }
        }
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        if (offset == 0) {
            return Files.newInputStream(mPath);
        } else {
            SeekableByteChannel channel = MoreFiles.newByteChannel(mPath);
            boolean successful = false;
            try {
                channel.position(offset);
                InputStream inputStream = Channels.newInputStream(channel);
                successful = true;
                return inputStream;
            } finally {
                if (!successful) {
                    channel.close();
                }
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ProviderFtpFile that = (ProviderFtpFile) object;
        return mPath.equals(that.mPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPath);
    }

    @Override
    public int compareTo(@NonNull ProviderFtpFile other) {
        Objects.requireNonNull(other);
        return mPath.compareTo(other.mPath);
    }
}
