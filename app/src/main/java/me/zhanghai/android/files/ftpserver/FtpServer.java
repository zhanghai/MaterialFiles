/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;

class FtpServer {

    @NonNull
    private final String mUsername;
    @Nullable
    private final String mPassword;
    private final int mPort;
    @NonNull
    private final Path mHomeDirectory;
    private final boolean mWritable;

    private org.apache.ftpserver.FtpServer mServer;

    public FtpServer(@NonNull String username, @Nullable String password, int port,
                     @NonNull Path homeDirectory, boolean writable) {
        mUsername = username;
        mPassword = password;
        mPort = port;
        mHomeDirectory = homeDirectory;
        mWritable = writable;
    }

    public void start() throws FtpException, RuntimeException {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(mPort);
        serverFactory.addListener("default", listenerFactory.createListener());
        BaseUser user = new BaseUser();
        user.setName(mUsername);
        user.setPassword(mPassword);
        user.setAuthorities(mWritable ? Collections.singletonList(new WritePermission())
                : Collections.emptyList());
        user.setHomeDirectory(mHomeDirectory.toUri().toString());
        serverFactory.getUserManager().save(user);
        serverFactory.setFileSystem(new ProviderFileSystemFactory());
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);
        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
        mServer = serverFactory.createServer();
        mServer.start();
    }

    public void stop() {
        mServer.stop();
        mServer = null;
    }
}
