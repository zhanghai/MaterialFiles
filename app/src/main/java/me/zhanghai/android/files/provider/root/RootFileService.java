/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import android.content.Context;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import eu.chainfire.librootjava.Debugger;
import eu.chainfire.librootjava.RootIPC;
import eu.chainfire.librootjava.RootIPCReceiver;
import eu.chainfire.librootjava.RootJava;
import eu.chainfire.libsuperuser.Shell;
import me.zhanghai.android.files.AppApplication;
import me.zhanghai.android.files.BuildConfig;
import me.zhanghai.android.files.provider.FileSystemProviders;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;
import me.zhanghai.android.files.provider.remote.IRemoteFileService;
import me.zhanghai.android.files.provider.remote.RemoteFileService;
import me.zhanghai.android.files.provider.remote.RemoteFileServiceInterface;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException;
import me.zhanghai.android.files.provider.remote.RemoteInterfaceHolder;
import me.zhanghai.android.files.util.LogUtils;

public class RootFileService extends RemoteFileService {

    @NonNull
    private static final RootFileService sInstance = new RootFileService();

    private static final int TIMEOUT_MILLIS = 10 * 1000;

    @NonNull
    private final Object mLock = new Object();

    private Shell.Interactive mShell;

    public RootFileService() {
        super(new RemoteInterfaceHolder<>(() -> getInstance().launchRemoteInterface()));
    }

    @NonNull
    public static RootFileService getInstance() {
        return sInstance;
    }

    @NonNull
    private IRemoteFileService launchRemoteInterface() throws RemoteFileSystemException {
        synchronized (mLock) {
            Context context = AppApplication.getInstance();
            if (mShell != null && !mShell.isRunning()) {
                mShell.close();
                mShell = null;
            }
            if (mShell == null) {
                mShell = new Shell.Builder()
                        .useSU()
                        .open();
            }
            RootJava.cleanupCache(context);
            Debugger.setEnabled(BuildConfig.DEBUG);
            CountDownLatch latch = new CountDownLatch(1);
            IRemoteFileService[] remoteFileServiceHolder = new IRemoteFileService[1];
            new RootIPCReceiver<IRemoteFileService>(context, 0) {
                @Override
                public void onConnect(@NonNull IRemoteFileService ipc) {
                    remoteFileServiceHolder[0] = ipc;
                    latch.countDown();
                }
                @Override
                public void onDisconnect(@NonNull IRemoteFileService ipc) {}
            };
            String syscallsLibraryPath = getLibraryPath(Syscalls.getLibraryName(), context);
            mShell.addCommand(RootJava.getLaunchScript(context, getClass(), null, null,
                    new String[] { syscallsLibraryPath }, BuildConfig.APPLICATION_ID + ":root"));
            try {
                latch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RemoteFileSystemException(e);
            }
            return remoteFileServiceHolder[0];
        }
    }

    @NonNull
    private static String getLibraryPath(@NonNull String libraryName, @NonNull Context context)
            throws RemoteFileSystemException {
        String libraryPath = RootJava.getLibraryPath(context, libraryName);
        if (libraryPath == null) {
            throw new RemoteFileSystemException("Cannot get path for library: " + libraryName);
        }
        return libraryPath;
    }

    public static void main(@NonNull String[] args) {

        LogUtils.i("Loading native libraries");
        for (String libraryPath : args) {
            System.load(libraryPath);
        }
        RootJava.restoreOriginalLdLibraryPath();

        LogUtils.i("Installing file system providers");
        FileSystemProviders.install();

        LogUtils.i("Sending Binder");
        RemoteFileServiceInterface remoteInterface = new RemoteFileServiceInterface();
        try {
            new RootIPC(BuildConfig.APPLICATION_ID, remoteInterface, 0, TIMEOUT_MILLIS, true);
        } catch (RootIPC.TimeoutException e) {
            e.printStackTrace();
        }
    }
}
