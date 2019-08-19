/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.User;

import java.net.URI;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import java8.nio.file.Paths;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;

class ProviderFileSystemView implements FileSystemView {

    @NonNull
    private final User mUser;

    @NonNull
    private final ProviderFtpFile mHomeDirectory;

    @NonNull
    private ProviderFtpFile mWorkingDirectory;

    public ProviderFileSystemView(@NonNull User user) {
        mUser = user;

        String homeDirectoryString = user.getHomeDirectory();
        URI homeDirectoryUri;
        try {
            homeDirectoryUri = new URI(homeDirectoryString);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
        Path homeDirectoryPath = Paths.get(homeDirectoryUri);
        mHomeDirectory = new ProviderFtpFile(homeDirectoryPath, homeDirectoryPath.relativize(
                homeDirectoryPath), mUser);
        mWorkingDirectory = mHomeDirectory;
    }

    @NonNull
    @Override
    public ProviderFtpFile getHomeDirectory() {
        return mHomeDirectory;
    }

    @NonNull
    @Override
    public ProviderFtpFile getWorkingDirectory() {
        return mWorkingDirectory;
    }

    @Override
    public boolean changeWorkingDirectory(@NonNull String directoryString) {
        ProviderFtpFile directory = getFile(directoryString);
        if (!directory.isDirectory()) {
            return false;
        }
        mWorkingDirectory = directory;
        return true;
    }

    @Override
    public ProviderFtpFile getFile(@NonNull String fileString) {
        boolean absolute = fileString.startsWith("/");
        if (absolute) {
            fileString = fileString.substring(1);
        }
        Path homeDirectoryPath = mHomeDirectory.getPhysicalFile();
        Path parentPath = absolute ? homeDirectoryPath : mWorkingDirectory.getPhysicalFile();
        Path filePath = parentPath.resolve(fileString);
        filePath = filePath.normalize();
        if (!filePath.startsWith(homeDirectoryPath)) {
            return mHomeDirectory;
        }
        return new ProviderFtpFile(filePath, filePath.relativize(homeDirectoryPath), mUser);
    }

    @Override
    public boolean isRandomAccessible() {
        // TODO: Better way of determining if the provider is random accessible.
        return !ArchiveFileSystemProvider.isArchivePath(mHomeDirectory.getPhysicalFile());
    }

    @Override
    public void dispose() {}
}
