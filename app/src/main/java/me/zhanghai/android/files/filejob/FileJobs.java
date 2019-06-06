/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import java8.nio.file.CopyOption;
import java8.nio.file.FileAlreadyExistsException;
import java8.nio.file.FileVisitResult;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.SimpleFileVisitor;
import java8.nio.file.StandardCopyOption;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FormatUtils;
import me.zhanghai.android.files.filelist.FileItem;
import me.zhanghai.android.files.provider.common.InvalidFileNameException;
import me.zhanghai.android.files.provider.common.MoreFiles;
import me.zhanghai.android.files.provider.common.ProgressCopyOption;
import me.zhanghai.java.promise.Promise;

public class FileJobs {

    private FileJobs() {}

    private abstract static class Base extends FileJob {

        private static final long NOTIFICATION_INTERVAL_MILLIS = 500;

        @NonNull
        protected ScanInfo scan(@NonNull List<Path> sources, @PluralsRes int notificationTitleRes)
                throws IOException {
            ScanInfo scanInfo = new ScanInfo();
            for (Path source : sources) {
                Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                    @NonNull
                    @Override
                    public FileVisitResult preVisitDirectory(
                            @NonNull Path directory, @NonNull BasicFileAttributes attributes)
                            throws IOException {
                        scanPath(attributes, scanInfo, notificationTitleRes);
                        throwIfInterrupted();
                        return FileVisitResult.CONTINUE;
                    }
                    @NonNull
                    @Override
                    public FileVisitResult visitFile(@NonNull Path file,
                                                     @NonNull BasicFileAttributes attributes)
                            throws IOException {
                        scanPath(attributes, scanInfo, notificationTitleRes);
                        throwIfInterrupted();
                        return FileVisitResult.CONTINUE;
                    }
                    @NonNull
                    @Override
                    public FileVisitResult visitFileFailed(@NonNull Path file,
                                                           @NonNull IOException exception)
                            throws IOException {
                        // TODO: Prompt retry, skip, skip-all or abort.
                        return super.visitFileFailed(file, exception);
                    }
                });
            }
            postScanNotification(scanInfo, notificationTitleRes);
            return scanInfo;
        }

        private void scanPath(@NonNull BasicFileAttributes attributes, @NonNull ScanInfo scanInfo,
                              @PluralsRes int notificationTitleRes) {
            scanInfo.incrementFileCount();
            scanInfo.addToSize(attributes.size());
            postScanNotification(scanInfo, notificationTitleRes);
        }

        private void postScanNotification(@NonNull ScanInfo scanInfo, @PluralsRes int titleRes) {
            if (!scanInfo.shouldPostNotification()) {
                return;
            }
            String size = FormatUtils.formatHumanReadableSize(scanInfo.getSize(), getService());
            int fileCount = scanInfo.getFileCount();
            String title = getQuantityString(titleRes, fileCount, fileCount, size);
            postNotification(title, null, null, null, 0, 0, true, true);
        }

        protected void copy(@NonNull Path source, @NonNull Path target,
                            @NonNull TransferInfo transferInfo,
                            @NonNull ActionAllInfo actionAllInfo) throws IOException {
            copyOrMove(source, target, true, true, false, transferInfo, actionAllInfo);
        }

        protected void copyForMove(@NonNull Path source, @NonNull Path target,
                                   @NonNull TransferInfo transferInfo,
                                   @NonNull ActionAllInfo actionAllInfo) throws IOException {
            copyOrMove(source, target, false, true, true, transferInfo, actionAllInfo);
        }

        // @see https://github.com/GNOME/nautilus/blob/master/src/nautilus-file-operations.c
        //      copy_move_file
        private void copyOrMove(@NonNull Path source, @NonNull Path target, boolean forCopy,
                                boolean useCopy, boolean copyAttributes,
                                @NonNull TransferInfo transferInfo,
                                @NonNull ActionAllInfo actionAllInfo) throws IOException {
            Path targetParent = target.getParent();
            if (targetParent.startsWith(source)) {
                // Don't allow copy/move into the source itself.
                if (actionAllInfo.skipCopyMoveIntoItself) {
                    transferInfo.skipFile(source);
                    postCopyMoveNotification(transferInfo, source, targetParent, forCopy);
                    return;
                }
                ActionResult result = showActionDialog(
                        getString(forCopy ? R.string.file_job_cannot_copy_into_itself_title
                                : R.string.file_job_cannot_move_into_itself_title),
                        getString(R.string.file_job_cannot_copy_move_into_itself_message),
                        true,
                        getString(R.string.file_job_action_skip),
                        getString(android.R.string.cancel),
                        null);
                switch (result.getAction()) {
                    case POSITIVE:
                        if (result.isAll()) {
                            actionAllInfo.skipCopyMoveIntoItself = true;
                        }
                        // Fall through!
                    case CANCELED:
                        transferInfo.skipFile(source);
                        postCopyMoveNotification(transferInfo, source, targetParent, forCopy);
                        return;
                    case NEGATIVE:
                        throw new InterruptedIOException();
                    default:
                        throw new AssertionError(result.getAction());
                }
            }
            if (source.startsWith(target)) {
                // Don't allow copy/move over the source itself or its ancestors.
                if (actionAllInfo.skipCopyMoveOverItself) {
                    transferInfo.skipFile(source);
                    postCopyMoveNotification(transferInfo, source, targetParent, forCopy);
                    return;
                }
                ActionResult result = showActionDialog(
                        getString(forCopy ? R.string.file_job_cannot_copy_over_itself_title
                                : R.string.file_job_cannot_move_over_itself_title),
                        getString(R.string.file_job_cannot_copy_move_over_itself_message),
                        true,
                        getString(R.string.file_job_action_skip),
                        getString(android.R.string.cancel),
                        null);
                switch (result.getAction()) {
                    case POSITIVE:
                        if (result.isAll()) {
                            actionAllInfo.skipCopyMoveOverItself = true;
                        }
                        // Fall through!
                    case CANCELED:
                        transferInfo.skipFile(source);
                        postCopyMoveNotification(transferInfo, source, targetParent, forCopy);
                        return;
                    case NEGATIVE:
                        throw new InterruptedIOException();
                    default:
                        throw new AssertionError(result.getAction());
                }
            }
            boolean replaceExisting = false;
            boolean retry;
            do {
                retry = false;
                List<CopyOption> optionList = new ArrayList<>();
                optionList.add(LinkOption.NOFOLLOW_LINKS);
                if (copyAttributes) {
                    optionList.add(StandardCopyOption.COPY_ATTRIBUTES);
                }
                if (replaceExisting) {
                    optionList.add(StandardCopyOption.REPLACE_EXISTING);
                }
                optionList.add(new ProgressCopyOption(size -> {
                    transferInfo.addToTransferredSize(size);
                    postCopyMoveNotification(transferInfo, source, targetParent, forCopy);
                }));
                CopyOption[] options = optionList.toArray(new CopyOption[0]);
                try {
                    postCopyMoveNotification(transferInfo, source, targetParent, forCopy);
                    if (useCopy) {
                        MoreFiles.copy(source, target, options);
                    } else {
                        MoreFiles.move(source, target, options);
                    }
                    transferInfo.incrementTransferredFileCount();
                    postCopyMoveNotification(transferInfo, source, targetParent, forCopy);
                } catch (FileAlreadyExistsException e) {
                    FileItem sourceFile = FileItem.load(source);
                    FileItem targetFile = FileItem.load(target);
                    boolean sourceIsDirectory = sourceFile.getAttributesNoFollowLinks()
                            .isDirectory();
                    boolean targetIsDirectory = targetFile.getAttributesNoFollowLinks()
                            .isDirectory();
                    if (!sourceIsDirectory && targetIsDirectory) {
                        // TODO: Don't allow replace directory with file.
                        throw e;
                    } else {
                        boolean isMerge = sourceIsDirectory && targetIsDirectory;
                        if ((isMerge && actionAllInfo.merge)
                                || (!isMerge && actionAllInfo.replace)) {
                            replaceExisting = true;
                            retry = true;
                            continue;
                        } else if ((isMerge && actionAllInfo.skipMerge)
                                || (!isMerge && actionAllInfo.skipReplace)) {
                            transferInfo.skipFile(source);
                            postCopyMoveNotification(transferInfo, source, targetParent, forCopy);
                            return;
                        }
                        ConflictResult result = showConflictDialog(sourceFile, targetFile, forCopy);
                        switch (result.getAction()) {
                            case MERGE_OR_REPLACE:
                                if (result.isAll()) {
                                    if (isMerge) {
                                        actionAllInfo.merge = true;
                                    } else {
                                        actionAllInfo.replace = true;
                                    }
                                }
                                replaceExisting = true;
                                retry = true;
                                continue;
                            case RENAME:
                                target = target.resolveSibling(result.getName());
                                retry = true;
                                continue;
                            case SKIP:
                                if (result.isAll()) {
                                    if (isMerge) {
                                        actionAllInfo.skipMerge = true;
                                    } else {
                                        actionAllInfo.skipReplace = true;
                                    }
                                }
                                // Fall through!
                            case CANCELED:
                                transferInfo.skipFile(source);
                                postCopyMoveNotification(transferInfo, source, targetParent,
                                        forCopy);
                                // TODO: Skip subtree.
                                return;
                            case CANCEL:
                                throw new InterruptedIOException();
                            default:
                                throw new AssertionError(result.getAction());
                        }
                    }
                } catch (InvalidFileNameException e) {
                    // TODO: Prompt invalid name.
                    if (false) {
                        retry = true;
                        continue;
                    }
                    throw e;
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    if (actionAllInfo.skipCopyMoveError) {
                        transferInfo.skipFile(source);
                        postCopyMoveNotification(transferInfo, source, targetParent, forCopy);
                        return;
                    }
                    ActionResult result = showActionDialog(
                            getString(forCopy ? R.string.file_job_copy_error_title_format
                                    : R.string.file_job_move_error_title_format,
                                    source.getFileName()),
                            getString(forCopy ? R.string.file_job_copy_error_message_format
                                    : R.string.file_job_move_error_message_format,
                                    targetParent.getFileName(), e.getLocalizedMessage()),
                            true,
                            getString(R.string.file_job_action_retry),
                            getString(R.string.file_job_action_skip),
                            getString(android.R.string.cancel));
                    switch (result.getAction()) {
                        case POSITIVE:
                            retry = true;
                            continue;
                        case NEGATIVE:
                            if (result.isAll()) {
                                actionAllInfo.skipCopyMoveError = true;
                            }
                            // Fall through!
                        case CANCELED:
                            transferInfo.skipFile(source);
                            postCopyMoveNotification(transferInfo, source, targetParent, forCopy);
                            return;
                        case NEUTRAL:
                            throw new InterruptedIOException();
                        default:
                            throw new AssertionError(result.getAction());
                    }
                }
            } while (retry);
        }

        private void postCopyMoveNotification(@NonNull TransferInfo transferInfo,
                                              @NonNull Path currentSource,
                                              @NonNull Path targetParent, boolean copy) {
            if (!transferInfo.shouldPostNotification()) {
                return;
            }
            String title;
            String text;
            int fileCount = transferInfo.getFileCount();
            long size = transferInfo.getSize();
            long transferredSize = transferInfo.getTransferredSize();
            if (fileCount == 1) {
                title = getString(copy ? R.string.file_job_copy_notification_title_one
                                : R.string.file_job_move_notification_title_one,
                        currentSource.getFileName(), targetParent.getFileName());
                Context context = getService();
                String sizeString = FormatUtils.formatHumanReadableSize(size, context);
                String transferredSizeString = FormatUtils.formatHumanReadableSize(transferredSize,
                        context);
                text = getString(R.string.file_job_copy_move_notification_text_one,
                        transferredSizeString, sizeString);
            } else {
                title = getQuantityString(copy ? R.plurals.file_job_copy_notification_title_multiple
                                : R.plurals.file_job_move_notification_title_multiple, fileCount,
                        fileCount, targetParent.getFileName());
                int currentFileIndex = Math.min(transferInfo.getTransferredFileCount() + 1,
                        fileCount);
                text = getString(R.string.file_job_copy_move_notification_text_multiple,
                        currentFileIndex, fileCount);
            }
            int max;
            int progress;
            if (size <= Integer.MAX_VALUE) {
                max = (int) size;
                progress = (int) transferredSize;
            } else {
                long maxLong = size;
                long progressLong = transferredSize;
                while (maxLong > Integer.MAX_VALUE) {
                    maxLong /= 2;
                    progressLong /= 2;
                }
                max = (int) maxLong;
                progress = (int) progressLong;
            }
            postNotification(title, text, null, null, max, progress, false, true);
        }

        protected void createDirectory(@NonNull Path path) throws IOException {
            Files.createDirectory(path);
        }

        protected void createFile(@NonNull Path path) throws IOException {
            Files.createFile(path);
        }

        protected void delete(@NonNull Path path, @Nullable TransferInfo transferInfo,
                              @NonNull ActionAllInfo actionAllInfo) throws IOException {
            boolean retry;
            do {
                retry = false;
                try {
                    Files.delete(path);
                    if (transferInfo != null) {
                        transferInfo.incrementTransferredFileCount();
                        postDeleteNotification(transferInfo, path);
                    }
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    if (actionAllInfo.skipDeleteError) {
                        if (transferInfo != null) {
                            transferInfo.skipFileIgnoringSize();
                            postDeleteNotification(transferInfo, path);
                        }
                        return;
                    }
                    ActionResult result = showActionDialog(
                            getString(R.string.file_job_delete_error_title),
                            getString(R.string.file_job_delete_error_message_format,
                                    path.getFileName(), e.getLocalizedMessage()),
                            true,
                            getString(R.string.file_job_action_retry),
                            getString(R.string.file_job_action_skip),
                            getString(android.R.string.cancel));
                    switch (result.getAction()) {
                        case POSITIVE:
                            retry = true;
                            continue;
                        case NEGATIVE:
                            if (result.isAll()) {
                                actionAllInfo.skipDeleteError = true;
                            }
                            // Fall through!
                        case CANCELED:
                            if (transferInfo != null) {
                                transferInfo.skipFileIgnoringSize();
                                postDeleteNotification(transferInfo, path);
                            }
                            return;
                        case NEUTRAL:
                            throw new InterruptedIOException();
                        default:
                            throw new AssertionError(result.getAction());
                    }
                }
            } while (retry);
        }

        private void postDeleteNotification(@NonNull TransferInfo transferInfo,
                                            @NonNull Path currentPath) {
            if (!transferInfo.shouldPostNotification()) {
                return;
            }
            String title;
            String text;
            int max;
            int progress;
            boolean indeterminate;
            int fileCount = transferInfo.getFileCount();
            if (fileCount == 1) {
                title = getString(R.string.file_job_delete_notification_title_one,
                        currentPath.getFileName());
                text = null;
                max = 0;
                progress = 0;
                indeterminate = true;
            } else {
                title = getQuantityString(R.plurals.file_job_delete_notification_title_multiple,
                        fileCount, fileCount);
                int transferredFileCount = transferInfo.getTransferredFileCount();
                int currentFileIndex = Math.min(transferredFileCount + 1, fileCount);
                text = getString(R.string.file_job_delete_notification_text_multiple,
                        currentFileIndex, fileCount);
                max = fileCount;
                progress = transferredFileCount;
                indeterminate = false;
            }
            postNotification(title, text, null, null, max, progress, indeterminate, true);
        }

        protected void moveAtomically(@NonNull Path source, @NonNull Path target)
                throws IOException {
            MoreFiles.move(source, target, LinkOption.NOFOLLOW_LINKS,
                    StandardCopyOption.ATOMIC_MOVE);
        }

        protected void moveByCopy(@NonNull Path source, @NonNull Path target,
                                  @NonNull TransferInfo transferInfo,
                                  @NonNull ActionAllInfo actionAllInfo) throws IOException {
            copyOrMove(source, target, false, false, true, transferInfo, actionAllInfo);
        }

        protected void throwIfInterrupted() throws InterruptedIOException {
            if (Thread.interrupted()) {
                throw new InterruptedIOException();
            }
        }

        protected void postNotification(@NonNull CharSequence title, @Nullable CharSequence text,
                                        @Nullable CharSequence subText, @Nullable CharSequence info,
                                        int max, int progress, boolean indeterminate,
                                        boolean showCancel) {
            Context context = getService();
            NotificationCompat.Builder bulider = new NotificationCompat.Builder(context,
                    FileJobNotificationManager.CHANNEL_ID)
                    .setColor(ContextCompat.getColor(context, R.color.color_primary))
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSubText(subText)
                    .setContentInfo(info)
                    .setProgress(max, progress, indeterminate)
                    .setOngoing(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    // TODO
                    //.setContentIntent();
            int id = getId();
            if (showCancel) {
                Intent intent = new Intent(context, FileJobReceiver.class)
                        .setAction(FileJobReceiver.ACTION_CANCEL)
                        .putExtra(FileJobReceiver.EXTRA_JOB_ID, id);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id + 1, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                bulider.addAction(R.drawable.close_icon_white_24dp, getString(
                        android.R.string.cancel), pendingIntent);
            }
            Notification notification = bulider.build();
            getService().getNotificationManager().notify(id, notification);
        }

        protected void cancelNotification() {
            getService().getNotificationManager().cancel(getId());
        }

        @NonNull
        protected ActionResult showActionDialog(@NonNull CharSequence title,
                                                @NonNull CharSequence message,
                                                boolean showAll,
                                                @Nullable CharSequence positiveButtonText,
                                                @Nullable CharSequence negativeButtonText,
                                                @Nullable CharSequence neutralButtonText)
                throws IOException {
            Service service = getService();
            try {
                return new Promise<ActionResult>(settler ->
                        service.startActivity(FileJobActionDialogActivity.makeIntent(title, message,
                                showAll, positiveButtonText, negativeButtonText, neutralButtonText,
                                (action, all) -> settler.resolve(new ActionResult(action, all)),
                                service)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                        .await();
            } catch (ExecutionException e) {
                throw new FileJobUiException(e);
            } catch (InterruptedException e) {
                InterruptedIOException exception = new InterruptedIOException();
                exception.initCause(e);
                throw exception;
            }
        }

        @NonNull
        protected ConflictResult showConflictDialog(@NonNull FileItem sourceFile,
                                                    @NonNull FileItem targetFile, boolean copy)
                throws IOException {
            Service service = getService();
            try {
                return new Promise<ConflictResult>(settler ->
                        service.startActivity(FileJobConflictDialogActivity.makeIntent(sourceFile,
                                targetFile, copy, (action, name, all) -> settler.resolve(
                                        new ConflictResult(action, name, all)), service)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                        .await();
            } catch (ExecutionException e) {
                throw new FileJobUiException(e);
            } catch (InterruptedException e) {
                InterruptedIOException exception = new InterruptedIOException();
                exception.initCause(e);
                throw exception;
            }
        }

        @NonNull
        public String getString(@StringRes int stringRes) {
            return getService().getString(stringRes);
        }

        @NonNull
        public String getString(@StringRes int stringRes, Object... formatArguments) {
            return getService().getString(stringRes, formatArguments);
        }

        @NonNull
        public String getQuantityString(@PluralsRes int pluralRes, int quantity,
                                        Object... formatArguments) {
            return getService().getResources().getQuantityString(pluralRes, quantity,
                    formatArguments);
        }

        protected static class ScanInfo {

            private int mFileCount;
            private long mSize;
            private long mLastNotificationTimeMillis;

            public int getFileCount() {
                return mFileCount;
            }

            public void incrementFileCount() {
                ++mFileCount;
            }

            public long getSize() {
                return mSize;
            }

            public void addToSize(long size) {
                mSize += size;
            }

            public boolean shouldPostNotification() {
                long currentTimeMillis = System.currentTimeMillis();
                if (mFileCount % 100 == 0 || mLastNotificationTimeMillis
                        + NOTIFICATION_INTERVAL_MILLIS < currentTimeMillis) {
                    mLastNotificationTimeMillis = currentTimeMillis;
                    return true;
                } else {
                    return false;
                }
            }
        }

        protected static class TransferInfo {

            private int mFileCount;
            private int mTransferredFileCount;
            private long mSize;
            private long mTransferredSize;
            private long mLastNotificationTimeMillis;

            public TransferInfo(@NonNull ScanInfo scanInfo) {
                mFileCount = scanInfo.getFileCount();
                mSize = scanInfo.getSize();
            }

            public int getFileCount() {
                return mFileCount;
            }

            public int getTransferredFileCount() {
                return mTransferredFileCount;
            }

            public void incrementTransferredFileCount() {
                ++mTransferredFileCount;
            }

            public void skipFile(@NonNull Path path) {
                --mFileCount;
                try {
                    mSize -= Files.readAttributes(path, BasicFileAttributes.class,
                            LinkOption.NOFOLLOW_LINKS).size();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void skipFileIgnoringSize() {
                --mFileCount;
            }

            public long getSize() {
                return mSize;
            }

            public long getTransferredSize() {
                return mTransferredSize;
            }

            public void addToTransferredSize(long size) {
                mTransferredSize += size;
            }

            public boolean shouldPostNotification() {
                long currentTimeMillis = System.currentTimeMillis();
                if (mLastNotificationTimeMillis + NOTIFICATION_INTERVAL_MILLIS
                        < currentTimeMillis) {
                    mLastNotificationTimeMillis = currentTimeMillis;
                    return true;
                } else {
                    return false;
                }
            }
        }

        protected static class ActionAllInfo {
            public boolean skipCopyMoveIntoItself;
            public boolean skipCopyMoveOverItself;
            public boolean merge;
            public boolean replace;
            public boolean skipMerge;
            public boolean skipReplace;
            public boolean skipCopyMoveError;
            public boolean skipDeleteError;
        }

        private static class ActionResult {

            @NonNull
            private final FileJobActionDialogFragment.Action mAction;
            private final boolean mAll;

            public ActionResult(@NonNull FileJobActionDialogFragment.Action action,
                                boolean all) {
                mAction = action;
                mAll = all;
            }

            @NonNull
            public FileJobActionDialogFragment.Action getAction() {
                return mAction;
            }

            public boolean isAll() {
                return mAll;
            }
        }

        private static class ConflictResult {

            @NonNull
            private final FileJobConflictDialogFragment.Action mAction;
            @Nullable
            private final String mName;
            private final boolean mAll;

            public ConflictResult(@NonNull FileJobConflictDialogFragment.Action action,
                                  @Nullable String name, boolean all) {
                mAction = action;
                mName = name;
                mAll = all;
            }

            @NonNull
            public FileJobConflictDialogFragment.Action getAction() {
                return mAction;
            }

            @Nullable
            public String getName() {
                return mName;
            }

            public boolean isAll() {
                return mAll;
            }
        }
    }

    public static class Copy extends Base {

        @NonNull
        private final List<Path> mSources;
        @NonNull
        private final Path mTargetDirectory;

        public Copy(@NonNull List<Path> sources, @NonNull Path targetDirectory) {
            mSources = sources;
            mTargetDirectory = targetDirectory;
        }

        @Override
        public void run() throws IOException {
            ScanInfo scanInfo = scan(mSources, R.plurals.file_job_copy_scan_notification_title);
            TransferInfo transferInfo = new TransferInfo(scanInfo);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            try {
                for (Path source : mSources) {
                    Path target = MoreFiles.resolve(mTargetDirectory, source.getFileName());
                    copyRecursively(source, target, transferInfo, actionAllInfo);
                    throwIfInterrupted();
                }
            } finally {
                cancelNotification();
            }
        }

        private void copyRecursively(@NonNull Path source, @NonNull Path target,
                                     @NonNull TransferInfo transferInfo,
                                     @NonNull ActionAllInfo actionAllInfo) throws IOException {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                         @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path directoryInTarget = MoreFiles.resolve(target, source.relativize(
                            directory));
                    copy(directory, directoryInTarget, transferInfo, actionAllInfo);
                    throwIfInterrupted();
                    return FileVisitResult.CONTINUE;
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path fileInTarget = MoreFiles.resolve(target, source.relativize(file));
                    copy(file, fileInTarget, transferInfo, actionAllInfo);
                    throwIfInterrupted();
                    return FileVisitResult.CONTINUE;
                }
                @NonNull
                @Override
                public FileVisitResult visitFileFailed(@NonNull Path file,
                                                       @NonNull IOException exception)
                        throws IOException {
                    // TODO: Prompt retry, skip, skip-all or abort.
                    return super.visitFileFailed(file, exception);
                }
            });
        }
    }

    public static class CreateDirectory extends Base {

        @NonNull
        private final Path mPath;

        public CreateDirectory(@NonNull Path path) {
            mPath = path;
        }

        @Override
        public void run() throws IOException {
            createDirectory(mPath);
        }
    }

    public static class CreateFile extends Base {

        @NonNull
        private final Path mPath;

        public CreateFile(@NonNull Path path) {
            mPath = path;
        }

        @Override
        public void run() throws IOException {
            createFile(mPath);
        }
    }

    public static class Delete extends Base {

        @NonNull
        private final List<Path> mPaths;

        public Delete(@NonNull List<Path> paths) {
            mPaths = paths;
        }

        @Override
        public void run() throws IOException {
            ScanInfo scanInfo = scan(mPaths, R.plurals.file_job_delete_scan_notification_title);
            TransferInfo transferInfo = new TransferInfo(scanInfo);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            try {
                for (Path path : mPaths) {
                    deleteRecursively(path, transferInfo, actionAllInfo);
                    throwIfInterrupted();
                }
            } finally {
                cancelNotification();
            }
        }

        private void deleteRecursively(@NonNull Path path, @NonNull TransferInfo transferInfo,
                                       @NonNull ActionAllInfo actionAllInfo)
                throws IOException {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    delete(file, transferInfo, actionAllInfo);
                    throwIfInterrupted();
                    return FileVisitResult.CONTINUE;
                }
                @NonNull
                @Override
                public FileVisitResult visitFileFailed(@NonNull Path file,
                                                       @NonNull IOException exception)
                        throws IOException {
                    // TODO: Prompt retry, skip, skip-all or abort.
                    return super.visitFileFailed(file, exception);
                }
                @NonNull
                @Override
                public FileVisitResult postVisitDirectory(@NonNull Path directory,
                                                          @Nullable IOException exception)
                        throws IOException {
                    // TODO: Prompt retry, skip, skip-all or abort.
                    if (exception != null) {
                        throw exception;
                    }
                    delete(directory, transferInfo, actionAllInfo);
                    throwIfInterrupted();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static class Move extends Base {

        @NonNull
        private final List<Path> mSources;
        @NonNull
        private final Path mTargetDirectory;

        public Move(@NonNull List<Path> sources, @NonNull Path targetDirectory) {
            mSources = sources;
            mTargetDirectory = targetDirectory;
        }

        @Override
        public void run() throws IOException {
            List<Path> sourcesToMove = new ArrayList<>();
            for (Path source : mSources) {
                Path target = MoreFiles.resolve(mTargetDirectory, source.getFileName());
                try {
                    moveAtomically(source, target);
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    sourcesToMove.add(source);
                }
                throwIfInterrupted();
            }
            ScanInfo scanInfo = scan(sourcesToMove,
                    R.plurals.file_job_move_scan_notification_title);
            TransferInfo transferInfo = new TransferInfo(scanInfo);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            try {
                for (Path source : sourcesToMove) {
                    Path target = MoreFiles.resolve(mTargetDirectory, source.getFileName());
                    moveRecursively(source, target, transferInfo, actionAllInfo);
                    throwIfInterrupted();
                }
            } finally {
                cancelNotification();
            }
        }

        private void moveRecursively(@NonNull Path source, @NonNull Path target,
                                     @NonNull TransferInfo transferInfo,
                                     @NonNull ActionAllInfo actionAllInfo) throws IOException {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                         @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path directoryInTarget = MoreFiles.resolve(target, source.relativize(
                            directory));
                    try {
                        moveAtomically(directory, directoryInTarget);
                        throwIfInterrupted();
                        return FileVisitResult.SKIP_SUBTREE;
                    } catch (InterruptedIOException e) {
                        throw e;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    copyForMove(directory, directoryInTarget, transferInfo, actionAllInfo);
                    throwIfInterrupted();
                    return FileVisitResult.CONTINUE;
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path fileInTarget = MoreFiles.resolve(target, source.relativize(file));
                    try {
                        moveAtomically(file, fileInTarget);
                        throwIfInterrupted();
                        return FileVisitResult.CONTINUE;
                    } catch (InterruptedIOException e) {
                        throw e;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    moveByCopy(file, fileInTarget, transferInfo, actionAllInfo);
                    throwIfInterrupted();
                    return FileVisitResult.CONTINUE;
                }
                @NonNull
                @Override
                public FileVisitResult visitFileFailed(@NonNull Path file,
                                                       @NonNull IOException exception)
                        throws IOException {
                    // TODO: Prompt retry, skip, skip-all or abort.
                    return super.visitFileFailed(file, exception);
                }
                @Override
                public FileVisitResult postVisitDirectory(@NonNull Path directory,
                                                          @Nullable IOException exception)
                        throws IOException {
                    if (exception != null) {
                        throw exception;
                    }
                    delete(directory, null, actionAllInfo);
                    throwIfInterrupted();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static class Rename extends Base {

        @NonNull
        private final Path mPath;
        @NonNull
        private final String mNewName;

        public Rename(@NonNull Path path, @NonNull String newName) {
            mPath = path;
            mNewName = newName;
        }

        @Override
        public void run() throws IOException {
            Path newPath = mPath.resolveSibling(mNewName);
            moveAtomically(mPath, newPath);
        }
    }
}
