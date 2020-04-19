/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import android.content.Context;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import androidx.annotation.NonNull;
import eu.chainfire.librootjava.Debugger;
import eu.chainfire.librootjava.RootIPC;
import eu.chainfire.librootjava.RootIPCReceiver;
import eu.chainfire.librootjava.RootJava;
import eu.chainfire.libsuperuser.Debug;
import eu.chainfire.libsuperuser.Shell;
import me.zhanghai.android.files.AppProvider;
import me.zhanghai.android.files.BuildConfig;
import me.zhanghai.android.files.provider.FileSystemProviders;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;
import me.zhanghai.android.files.provider.remote.IRemoteFileService;
import me.zhanghai.android.files.provider.remote.RemoteFileService;
import me.zhanghai.android.files.provider.remote.RemoteFileServiceInterface;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException;
import me.zhanghai.android.files.provider.remote.RemoteInterfaceHolder;
import me.zhanghai.android.files.util.LogUtils;
import me.zhanghai.android.libselinux.SeLinux;

public class RootFileService extends RemoteFileService {

    @NonNull
    private static final RootFileService sInstance = new RootFileService();

    private static final int TIMEOUT_MILLIS = 10 * 1000;

    @NonNull
    private final Object mLock = new Object();

    private Shell.Interactive mShell;

    static {
        Debug.setDebug(BuildConfig.DEBUG);
        Debugger.setEnabled(BuildConfig.DEBUG);
    }

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
            Shell.Interactive shell = getSuShellLocked();
            Context context = AppProvider.requireContext();
            RootJava.cleanupCache(context);
            CountDownLatch latch = new CountDownLatch(1);
            IRemoteFileService[] remoteInterfaceHolder = new IRemoteFileService[1];
            RootIPCReceiver ipcReceiver = new RootIPCReceiver<IRemoteFileService>(context, 0) {
                @NonNull
                private final Object mReleaseLock = new Object();
                private boolean mInRelease;
                @Override
                public void onConnect(@NonNull IRemoteFileService ipc) {
                    remoteInterfaceHolder[0] = ipc;
                    latch.countDown();
                }
                @Override
                public void onDisconnect(@NonNull IRemoteFileService ipc) {
                    release();
                }
                @Override
                public void release() {
                    synchronized (mReleaseLock) {
                        if (mInRelease) {
                            return;
                        }
                        mInRelease = true;
                        super.release();
                        mInRelease = false;
                    }
                }
            };
            try {
                String[] libraryPaths = {
                        getLibraryPath(Syscalls.getLibraryName(), context),
                        getLibraryPath(SeLinux.getLibraryName(), context)
                };
                shell.addCommand(RootJava.getLaunchScript(context, getClass(), null, null,
                        libraryPaths, BuildConfig.APPLICATION_ID + ":root"));
                try {
                    if (!latch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                        throw new RemoteFileSystemException(new TimeoutException(
                                "Timeout while connecting to root process"));
                    }
                } catch (InterruptedException e) {
                    throw new RemoteFileSystemException(e);
                }
                return remoteInterfaceHolder[0];
            } catch (Exception e) {
                ipcReceiver.release();
                throw e;
            }
        }
    }

    @NonNull
    private Shell.Interactive getSuShellLocked() throws RemoteFileSystemException {
        if (mShell != null) {
            if (mShell.isRunning()) {
                if (!mShell.isIdle()) {
                    mShell.waitForIdle();
                }
                return mShell;
            } else {
                mShell.close();
                mShell = null;
            }
        }
        mShell = launchSuShell();
        return mShell;
    }

    @NonNull
    private static Shell.Interactive launchSuShell() throws RemoteFileSystemException {
        boolean[] successfulHolder = new boolean[1];
        int[] exitCodeHolder = new int[1];
        Shell.Interactive shell = new Shell.Builder()
                .useSU()
                .open((successful, exitCode) -> {
                    successfulHolder[0] = successful;
                    exitCodeHolder[0] = exitCode;
                });
        shell.waitForIdle();
        if (!successfulHolder[0]) {
            if (shell.isRunning()) {
                shell.closeImmediately();
            }
            throw new RemoteFileSystemException("Cannot launch su shell, exit code: "
                    + exitCodeHolder[0]);
        }
        return shell;
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
        FileSystemProviders.setOverflowWatchEvents(true);

        LogUtils.i("Sending Binder");
        RemoteFileServiceInterface remoteInterface = new RemoteFileServiceInterface();
        try {
            new RootIPC(BuildConfig.APPLICATION_ID, remoteInterface, 0, TIMEOUT_MILLIS, true);
        } catch (RootIPC.TimeoutException e) {
            e.printStackTrace();
        }
    }
}
