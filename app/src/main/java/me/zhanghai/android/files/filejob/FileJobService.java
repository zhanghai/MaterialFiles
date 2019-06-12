/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;

public class FileJobService extends Service {

    @Nullable
    private static FileJobService sInstance;

    @NonNull
    private static final List<FileJob> sPendingJobs = new ArrayList<>();

    @NonNull
    private final List<FileJob> mRunningJobs = Collections.synchronizedList(new ArrayList<>());

    @NonNull
    private final ExecutorService mExecutorService = Executors.newCachedThreadPool();

    private FileJobNotificationManager mNotificationManager;

    private static void startJob(@NonNull FileJob job, @NonNull Context context) {
        if (sInstance != null) {
            sInstance.startJob(job);
        } else {
            sPendingJobs.add(job);
            context.startService(new Intent(context, FileJobService.class));
        }
    }

    public static void copy(@NonNull List<Path> sources, @NonNull Path targetDirectory,
                            @NonNull Context context) {
        startJob(new FileJobs.Copy(sources, targetDirectory), context);
    }

    public static void archive(@NonNull List<Path> sources, @NonNull Path archiveFile,
                               @NonNull String archiveType, @Nullable String compressorType,
                               @NonNull Context context) {
        startJob(new FileJobs.Archive(sources, archiveFile, archiveType, compressorType), context);
    }

    public static void createFile(@NonNull Path path, @NonNull Context context) {
        startJob(new FileJobs.CreateFile(path), context);
    }

    public static void createDirectory(@NonNull Path path, @NonNull Context context) {
        startJob(new FileJobs.CreateDirectory(path), context);
    }

    public static void delete(@NonNull List<Path> paths, @NonNull Context context) {
        startJob(new FileJobs.Delete(paths), context);
    }

    public static void move(@NonNull List<Path> sources, @NonNull Path targetDirectory,
                            @NonNull Context context) {
        startJob(new FileJobs.Move(sources, targetDirectory), context);
    }

    public static void open(@NonNull Path file, @NonNull String mimeType,
                            @NonNull Context context) {
        startJob(new FileJobs.Open(file, mimeType), context);
    }

    public static void rename(@NonNull Path path, @NonNull String newName,
                              @NonNull Context context) {
        startJob(new FileJobs.Rename(path, newName), context);
    }

    @MainThread
    public static void cancelRunningJob(int jobId) {
        if (sInstance != null) {
            sInstance.cancelJob(jobId);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = new FileJobNotificationManager(this);

        sInstance = this;

        Iterator<FileJob> iterator = sPendingJobs.iterator();
        while (iterator.hasNext()) {
            FileJob job = iterator.next();
            iterator.remove();
            startJob(job);
        }
    }

    private void startJob(@NonNull FileJob job) {
        mRunningJobs.add(job);
        Future<?> future = mExecutorService.submit(() -> {
            job.run(this);
            mRunningJobs.remove(job);
        });
        job.setFuture(future);
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void cancelJob(int jobId) {
        synchronized (mRunningJobs) {
            Iterator<FileJob> iterator = mRunningJobs.iterator();
            while (iterator.hasNext()) {
                FileJob job = iterator.next();
                if (job.getId() == jobId) {
                    job.cancel();
                    iterator.remove();
                    return;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sInstance = null;

        stopJobs();
        mExecutorService.shutdownNow();
    }

    private void stopJobs() {
        synchronized (mRunningJobs) {
            Iterator<FileJob> iterator = mRunningJobs.iterator();
            while (iterator.hasNext()) {
                FileJob job = iterator.next();
                job.cancel();
                iterator.remove();
            }
        }
    }

    public FileJobNotificationManager getNotificationManager() {
        return mNotificationManager;
    }
}
