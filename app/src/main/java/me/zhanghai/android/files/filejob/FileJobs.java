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
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.CopyOption;
import java8.nio.file.DirectoryIteratorException;
import java8.nio.file.DirectoryStream;
import java8.nio.file.FileAlreadyExistsException;
import java8.nio.file.FileVisitResult;
import java8.nio.file.FileVisitor;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.Paths;
import java8.nio.file.SimpleFileVisitor;
import java8.nio.file.StandardCopyOption;
import java8.nio.file.StandardOpenOption;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.file.FileItem;
import me.zhanghai.android.files.file.FileProvider;
import me.zhanghai.android.files.file.FormatUtils;
import me.zhanghai.android.files.filelist.OpenFileAsDialogActivity;
import me.zhanghai.android.files.notification.Notifications;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.archive.archiver.ArchiveWriter;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringBuilder;
import me.zhanghai.android.files.provider.common.ByteStringListPath;
import me.zhanghai.android.files.provider.common.InvalidFileNameException;
import me.zhanghai.android.files.provider.common.MoreFiles;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixPrincipal;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.android.files.provider.common.ProgressCopyOption;
import me.zhanghai.android.files.util.BackgroundActivityStarter;
import me.zhanghai.android.files.util.IntentPathUtils;
import me.zhanghai.android.files.util.IntentUtils;
import me.zhanghai.android.files.util.PathFileNameUtils;
import me.zhanghai.java.functional.Functional;
import me.zhanghai.java.promise.Promise;

public class FileJobs {

    private static final long PROGRESS_INTERVAL_MILLIS = 200;

    private FileJobs() {}

    abstract static class Base extends FileJob {

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

        @NonNull
        protected ScanInfo scan(@NonNull Path source, @PluralsRes int notificationTitleRes)
                throws IOException {
            return scan(Collections.singletonList(source), notificationTitleRes);
        }

        @NonNull
        protected ScanInfo scan(@NonNull Path source, boolean recursive,
                                @PluralsRes int notificationTitleRes) throws IOException {
            if (recursive) {
                return scan(source, notificationTitleRes);
            }
            ScanInfo scanInfo = new ScanInfo();
            BasicFileAttributes attributes = Files.readAttributes(source, BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
            scanPath(attributes, scanInfo, notificationTitleRes);
            throwIfInterrupted();
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

        @NonNull
        protected static Path getTargetFileName(@NonNull Path source) {
            if (ArchiveFileSystemProvider.isArchivePath(source)) {
                Path archiveFile = ArchiveFileSystemProvider.getArchiveFile(source);
                Path archiveRoot = ArchiveFileSystemProvider.getRootPathForArchiveFile(archiveFile);
                if (Objects.equals(source, archiveRoot)) {
                    return PathFileNameUtils.getFullBaseName(archiveFile.getFileName());
                }
            }
            return source.getFileName();
        }

        protected void archive(@NonNull Path file, @NonNull ArchiveWriter writer,
                               @NonNull Path entryName, @NonNull Path archiveFile,
                               @NonNull TransferInfo transferInfo)
                throws IOException {
            try {
                postArchiveNotification(transferInfo, file);
                writer.write(file, entryName, size -> {
                    transferInfo.addToTransferredSize(size);
                    postArchiveNotification(transferInfo, file);
                }, PROGRESS_INTERVAL_MILLIS);
                transferInfo.incrementTransferredFileCount();
                postArchiveNotification(transferInfo, file);
            } catch (InterruptedIOException e) {
                throw e;
            } catch (IOException e) {
                e.printStackTrace();
                ActionResult result = showActionDialog(
                        getString(R.string.file_job_archive_error_title_format, getFileName(file)),
                        getString(R.string.file_job_archive_error_message_format,
                                getFileName(archiveFile), e.getLocalizedMessage()),
                        false,
                        null,
                        getString(android.R.string.cancel),
                        null);
                switch (result.getAction()) {
                    case NEGATIVE:
                    case CANCELED:
                        throw new InterruptedIOException();
                    case POSITIVE:
                    case NEUTRAL:
                    default:
                        throw new AssertionError(result.getAction());
                }
            }
        }

        private void postArchiveNotification(@NonNull TransferInfo transferInfo,
                                             @NonNull Path currentFile) {
            postTransferSizeNotification(transferInfo, currentFile,
                    R.string.file_job_archive_notification_title_one,
                    R.plurals.file_job_archive_notification_title_multiple);
        }

        protected boolean copy(@NonNull Path source, @NonNull Path target, boolean isExtract,
                               @NonNull TransferInfo transferInfo,
                               @NonNull ActionAllInfo actionAllInfo) throws IOException {
            return copyOrMove(source, target, isExtract ? CopyMoveType.EXTRACT : CopyMoveType.COPY,
                    true, false, transferInfo, actionAllInfo);
        }

        protected boolean copyForMove(@NonNull Path source, @NonNull Path target,
                                      @NonNull TransferInfo transferInfo,
                                      @NonNull ActionAllInfo actionAllInfo) throws IOException {
            return copyOrMove(source, target, CopyMoveType.MOVE, true, true, transferInfo,
                    actionAllInfo);
        }

        // @see https://github.com/GNOME/nautilus/blob/master/src/nautilus-file-operations.c
        //      copy_move_file
        private boolean copyOrMove(@NonNull Path source, @NonNull Path target,
                                   @NonNull CopyMoveType type, boolean useCopy,
                                   boolean copyAttributes, @NonNull TransferInfo transferInfo,
                                   @NonNull ActionAllInfo actionAllInfo) throws IOException {
            Path targetParent = target.getParent();
            if (targetParent.startsWith(source)) {
                // Don't allow copy/move into the source itself.
                if (actionAllInfo.skipCopyMoveIntoItself) {
                    transferInfo.skipFile(source);
                    postCopyMoveNotification(transferInfo, source, type);
                    return false;
                }
                ActionResult result = showActionDialog(
                        getString(type.getResource(R.string.file_job_cannot_copy_into_itself_title,
                                R.string.file_job_cannot_extract_into_itself_title,
                                R.string.file_job_cannot_move_into_itself_title)),
                        getString(R.string.file_job_cannot_copy_move_into_itself_message),
                        true,
                        getString(R.string.skip),
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
                        postCopyMoveNotification(transferInfo, source, type);
                        return false;
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
                    postCopyMoveNotification(transferInfo, source, type);
                    return false;
                }
                ActionResult result = showActionDialog(
                        getString(type.getResource(R.string.file_job_cannot_copy_over_itself_title,
                                R.string.file_job_cannot_extract_over_itself_title,
                                R.string.file_job_cannot_move_over_itself_title)),
                        getString(R.string.file_job_cannot_copy_move_over_itself_message),
                        true,
                        getString(R.string.skip),
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
                        postCopyMoveNotification(transferInfo, source, type);
                        return false;
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
                    postCopyMoveNotification(transferInfo, source, type);
                }, PROGRESS_INTERVAL_MILLIS));
                CopyOption[] options = optionList.toArray(new CopyOption[0]);
                try {
                    postCopyMoveNotification(transferInfo, source, type);
                    if (useCopy) {
                        MoreFiles.copy(source, target, options);
                    } else {
                        MoreFiles.move(source, target, options);
                    }
                    transferInfo.incrementTransferredFileCount();
                    postCopyMoveNotification(transferInfo, source, type);
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
                            postCopyMoveNotification(transferInfo, source, type);
                            return false;
                        }
                        ConflictResult result = showConflictDialog(sourceFile, targetFile, type);
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
                                postCopyMoveNotification(transferInfo, source, type);
                                return false;
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
                    e.printStackTrace();
                    if (actionAllInfo.skipCopyMoveError) {
                        transferInfo.skipFile(source);
                        postCopyMoveNotification(transferInfo, source, type);
                        return false;
                    }
                    ActionResult result = showActionDialog(
                            getString(type.getResource(R.string.file_job_copy_error_title_format,
                                    R.string.file_job_extract_error_title_format,
                                    R.string.file_job_move_error_title_format),
                                    getFileName(source)),
                            getString(type.getResource(R.string.file_job_copy_error_message_format,
                                    R.string.file_job_extract_error_message_format,
                                    R.string.file_job_move_error_message_format),
                                    getFileName(targetParent), e.getLocalizedMessage()),
                            true,
                            getString(R.string.retry),
                            getString(R.string.skip),
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
                            postCopyMoveNotification(transferInfo, source, type);
                            return false;
                        case NEUTRAL:
                            throw new InterruptedIOException();
                        default:
                            throw new AssertionError(result.getAction());
                    }
                }
            } while (retry);
            return true;
        }

        private void postCopyMoveNotification(@NonNull TransferInfo transferInfo,
                                              @NonNull Path currentSource,
                                              @NonNull CopyMoveType type) {
            postTransferSizeNotification(transferInfo, currentSource,
                    type.getResource(R.string.file_job_copy_notification_title_one,
                            R.string.file_job_extract_notification_title_one,
                            R.string.file_job_move_notification_title_one),
                    type.getResource(
                            R.plurals.file_job_copy_notification_title_multiple,
                            R.plurals.file_job_extract_notification_title_multiple,
                            R.plurals.file_job_move_notification_title_multiple));
        }

        private void postTransferSizeNotification(@NonNull TransferInfo transferInfo,
                                                  @NonNull Path currentSource,
                                                  @StringRes int titleOneRes,
                                                  @PluralsRes int titleMultipleRes) {
            if (!transferInfo.shouldPostNotification()) {
                return;
            }
            String title;
            String text;
            int fileCount = transferInfo.getFileCount();
            Path target = transferInfo.getTarget();
            long size = transferInfo.getSize();
            long transferredSize = transferInfo.getTransferredSize();
            if (fileCount == 1) {
                title = getString(titleOneRes, getFileName(currentSource), getFileName(target));
                Context context = getService();
                String sizeString = FormatUtils.formatHumanReadableSize(size, context);
                String transferredSizeString = FormatUtils.formatHumanReadableSize(transferredSize,
                        context);
                text = getString(R.string.file_job_transfer_size_notification_text_one,
                        transferredSizeString, sizeString);
            } else {
                title = getQuantityString(titleMultipleRes, fileCount, fileCount, getFileName(
                        target));
                int currentFileIndex = Math.min(transferInfo.getTransferredFileCount() + 1,
                        fileCount);
                text = getString(R.string.file_job_transfer_size_notification_text_multiple,
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
                    e.printStackTrace();
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
                                    getFileName(path), e.getLocalizedMessage()),
                            true,
                            getString(R.string.retry),
                            getString(R.string.skip),
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
            postTransferCountNotification(transferInfo, currentPath,
                    R.string.file_job_delete_notification_title_one,
                    R.plurals.file_job_delete_notification_title_multiple);
        }

        private static String getFileName(@NonNull Path path) {
            return path.isAbsolute() && path.getNameCount() == 0 ?
                    path.getFileSystem().getSeparator() : path.getFileName().toString();
        }

        protected void moveAtomically(@NonNull Path source, @NonNull Path target)
                throws IOException {
            MoreFiles.move(source, target, LinkOption.NOFOLLOW_LINKS,
                    StandardCopyOption.ATOMIC_MOVE);
        }

        protected boolean moveByCopy(@NonNull Path source, @NonNull Path target,
                                     @NonNull TransferInfo transferInfo,
                                     @NonNull ActionAllInfo actionAllInfo) throws IOException {
            return copyOrMove(source, target, CopyMoveType.MOVE, false, true, transferInfo,
                    actionAllInfo);
        }

        protected void restoreSeLinuxContext(@NonNull Path path, boolean followLinks,
                                             @NonNull TransferInfo transferInfo,
                                             @NonNull ActionAllInfo actionAllInfo)
                throws IOException {
            boolean retry;
            do {
                retry = false;
                try {
                    LinkOption[] options = followLinks ? new LinkOption[0]
                            : new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
                    MoreFiles.restoreSeLinuxContext(path, options);
                    transferInfo.incrementTransferredFileCount();
                    postRestoreSeLinuxContextNotification(transferInfo, path);
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    e.printStackTrace();
                    if (actionAllInfo.skipRestoreSeLinuxContextError) {
                        transferInfo.skipFileIgnoringSize();
                        postRestoreSeLinuxContextNotification(transferInfo, path);
                        return;
                    }
                    ActionResult result = showActionDialog(
                            getString(R.string.file_job_restore_selinux_context_error_title),
                            getString(
                                    R.string.file_job_restore_selinux_context_error_message_format,
                                    getFileName(path), e.getLocalizedMessage()),
                            true,
                            getString(R.string.retry),
                            getString(R.string.skip),
                            getString(android.R.string.cancel));
                    switch (result.getAction()) {
                        case POSITIVE:
                            retry = true;
                            continue;
                        case NEGATIVE:
                            if (result.isAll()) {
                                actionAllInfo.skipRestoreSeLinuxContextError = true;
                            }
                            // Fall through!
                        case CANCELED:
                            transferInfo.skipFileIgnoringSize();
                            postRestoreSeLinuxContextNotification(transferInfo, path);
                            return;
                        case NEUTRAL:
                            throw new InterruptedIOException();
                        default:
                            throw new AssertionError(result.getAction());
                    }
                }
            } while (retry);
        }

        private void postRestoreSeLinuxContextNotification(@NonNull TransferInfo transferInfo,
                                                           @NonNull Path currentPath) {
            postTransferCountNotification(transferInfo, currentPath,
                    R.string.file_job_restore_selinux_context_notification_title_one,
                    R.plurals.file_job_restore_selinux_context_notification_title_multiple);
        }

        protected void setGroup(@NonNull Path path, @NonNull PosixGroup group, boolean followLinks,
                                @NonNull TransferInfo transferInfo,
                                @NonNull ActionAllInfo actionAllInfo) throws IOException {
            boolean retry;
            do {
                retry = false;
                try {
                    LinkOption[] options = followLinks ? new LinkOption[0]
                            : new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
                    MoreFiles.setGroup(path, group, options);
                    transferInfo.incrementTransferredFileCount();
                    postSetGroupNotification(transferInfo, path);
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    e.printStackTrace();
                    if (actionAllInfo.skipSetGroupError) {
                        transferInfo.skipFileIgnoringSize();
                        postSetGroupNotification(transferInfo, path);
                        return;
                    }
                    ActionResult result = showActionDialog(
                            getString(R.string.file_job_set_group_error_title_format,
                                    getFileName(path)),
                            getString(R.string.file_job_set_group_error_message_format,
                                    getPrincipalName(group), e.getLocalizedMessage()),
                            true,
                            getString(R.string.retry),
                            getString(R.string.skip),
                            getString(android.R.string.cancel));
                    switch (result.getAction()) {
                        case POSITIVE:
                            retry = true;
                            continue;
                        case NEGATIVE:
                            if (result.isAll()) {
                                actionAllInfo.skipSetGroupError = true;
                            }
                            // Fall through!
                        case CANCELED:
                            transferInfo.skipFileIgnoringSize();
                            postSetGroupNotification(transferInfo, path);
                            return;
                        case NEUTRAL:
                            throw new InterruptedIOException();
                        default:
                            throw new AssertionError(result.getAction());
                    }
                }
            } while (retry);
        }

        private void postSetGroupNotification(@NonNull TransferInfo transferInfo,
                                              @NonNull Path currentPath) {
            postTransferCountNotification(transferInfo, currentPath,
                    R.string.file_job_set_group_notification_title_one,
                    R.plurals.file_job_set_group_notification_title_multiple);
        }

        protected void setMode(@NonNull Path path, @NonNull Set<PosixFileModeBit> mode,
                               @NonNull TransferInfo transferInfo,
                               @NonNull ActionAllInfo actionAllInfo) throws IOException {
            boolean retry;
            do {
                retry = false;
                try {
                    // This will always follow symbolic links.
                    MoreFiles.setMode(path, mode);
                    transferInfo.incrementTransferredFileCount();
                    postSetModeNotification(transferInfo, path);
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    e.printStackTrace();
                    if (actionAllInfo.skipSetModeError) {
                        transferInfo.skipFileIgnoringSize();
                        postSetModeNotification(transferInfo, path);
                        return;
                    }
                    ActionResult result = showActionDialog(
                            getString(R.string.file_job_set_mode_error_title_format,
                                    getFileName(path)),
                            getString(R.string.file_job_set_mode_error_message_format,
                                    PosixFileMode.toString(mode), e.getLocalizedMessage()),
                            true,
                            getString(R.string.retry),
                            getString(R.string.skip),
                            getString(android.R.string.cancel));
                    switch (result.getAction()) {
                        case POSITIVE:
                            retry = true;
                            continue;
                        case NEGATIVE:
                            if (result.isAll()) {
                                actionAllInfo.skipSetModeError = true;
                            }
                            // Fall through!
                        case CANCELED:
                            transferInfo.skipFileIgnoringSize();
                            postSetModeNotification(transferInfo, path);
                            return;
                        case NEUTRAL:
                            throw new InterruptedIOException();
                        default:
                            throw new AssertionError(result.getAction());
                    }
                }
            } while (retry);
        }

        private void postSetModeNotification(@NonNull TransferInfo transferInfo,
                                                       @NonNull Path currentPath) {
            postTransferCountNotification(transferInfo, currentPath,
                    R.string.file_job_set_mode_notification_title_one,
                    R.plurals.file_job_set_mode_notification_title_multiple);
        }

        protected void setOwner(@NonNull Path path, @NonNull PosixUser owner, boolean followLinks,
                                @NonNull TransferInfo transferInfo,
                                @NonNull ActionAllInfo actionAllInfo) throws IOException {
            boolean retry;
            do {
                retry = false;
                try {
                    LinkOption[] options = followLinks ? new LinkOption[0]
                            : new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
                    MoreFiles.setOwner(path, owner, options);
                    transferInfo.incrementTransferredFileCount();
                    postSetOwnerNotification(transferInfo, path);
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    e.printStackTrace();
                    if (actionAllInfo.skipSetOwnerError) {
                        transferInfo.skipFileIgnoringSize();
                        postSetOwnerNotification(transferInfo, path);
                        return;
                    }
                    ActionResult result = showActionDialog(
                            getString(R.string.file_job_set_owner_error_title_format,
                                    getFileName(path)),
                            getString(R.string.file_job_set_owner_error_message_format,
                                    getPrincipalName(owner), e.getLocalizedMessage()),
                            true,
                            getString(R.string.retry),
                            getString(R.string.skip),
                            getString(android.R.string.cancel));
                    switch (result.getAction()) {
                        case POSITIVE:
                            retry = true;
                            continue;
                        case NEGATIVE:
                            if (result.isAll()) {
                                actionAllInfo.skipSetOwnerError = true;
                            }
                            // Fall through!
                        case CANCELED:
                            transferInfo.skipFileIgnoringSize();
                            postSetOwnerNotification(transferInfo, path);
                            return;
                        case NEUTRAL:
                            throw new InterruptedIOException();
                        default:
                            throw new AssertionError(result.getAction());
                    }
                }
            } while (retry);
        }

        private void postSetOwnerNotification(@NonNull TransferInfo transferInfo,
                                              @NonNull Path currentPath) {
            postTransferCountNotification(transferInfo, currentPath,
                    R.string.file_job_set_owner_notification_title_one,
                    R.plurals.file_job_set_owner_notification_title_multiple);
        }

        @NonNull
        private static String getPrincipalName(@NonNull PosixPrincipal principal) {
            String name = principal.getName();
            if (name == null) {
                name = String.valueOf(principal.getId());
            }
            return name;
        }

        protected void setSeLinuxContext(@NonNull Path path, @NonNull String seLinuxContext,
                                         boolean followLinks, @NonNull TransferInfo transferInfo,
                                         @NonNull ActionAllInfo actionAllInfo) throws IOException {
            boolean retry;
            do {
                retry = false;
                try {
                    LinkOption[] options = followLinks ? new LinkOption[0]
                            : new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
                    MoreFiles.setSeLinuxContext(path, ByteString.fromString(seLinuxContext),
                            options);
                    transferInfo.incrementTransferredFileCount();
                    postSetSeLinuxContextNotification(transferInfo, path);
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    e.printStackTrace();
                    if (actionAllInfo.skipSetSeLinuxContextError) {
                        transferInfo.skipFileIgnoringSize();
                        postSetSeLinuxContextNotification(transferInfo, path);
                        return;
                    }
                    ActionResult result = showActionDialog(
                            getString(R.string.file_job_set_selinux_context_error_title_format,
                                    getFileName(path)),
                            getString(R.string.file_job_set_selinux_context_error_message_format,
                                    seLinuxContext, e.getLocalizedMessage()),
                            true,
                            getString(R.string.retry),
                            getString(R.string.skip),
                            getString(android.R.string.cancel));
                    switch (result.getAction()) {
                        case POSITIVE:
                            retry = true;
                            continue;
                        case NEGATIVE:
                            if (result.isAll()) {
                                actionAllInfo.skipSetSeLinuxContextError = true;
                            }
                            // Fall through!
                        case CANCELED:
                            transferInfo.skipFileIgnoringSize();
                            postSetSeLinuxContextNotification(transferInfo, path);
                            return;
                        case NEUTRAL:
                            throw new InterruptedIOException();
                        default:
                            throw new AssertionError(result.getAction());
                    }
                }
            } while (retry);
        }

        private void postSetSeLinuxContextNotification(@NonNull TransferInfo transferInfo,
                                              @NonNull Path currentPath) {
            postTransferCountNotification(transferInfo, currentPath,
                    R.string.file_job_set_selinux_context_notification_title_one,
                    R.plurals.file_job_set_selinux_context_notification_title_multiple);
        }

        private void postTransferCountNotification(@NonNull TransferInfo transferInfo,
                                                   @NonNull Path currentPath,
                                                   @StringRes int titleOneRes,
                                                   @PluralsRes int titleMultipleRes) {
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
                title = getString(titleOneRes, getFileName(currentPath));
                text = null;
                max = 0;
                progress = 0;
                indeterminate = true;
            } else {
                title = getQuantityString(titleMultipleRes, fileCount, fileCount);
                int transferredFileCount = transferInfo.getTransferredFileCount();
                int currentFileIndex = Math.min(transferredFileCount + 1, fileCount);
                text = getString(R.string.file_job_transfer_count_notification_text_multiple,
                        currentFileIndex, fileCount);
                max = fileCount;
                progress = transferredFileCount;
                indeterminate = false;
            }
            postNotification(title, text, null, null, max, progress, indeterminate, true);
        }

        // The attributes for start path prefers following links, but falls back to not following.
        // FileVisitResult returned from visitor may be ignored and always considered CONTINUE.
        protected void walkFileTreeForSettingAttributes(@NonNull Path start, boolean recursive,
                                                        @NonNull FileVisitor<? super Path> visitor)
                throws IOException {
            BasicFileAttributes attributes = null;
            try {
                // Try to follow links first.
                attributes = Files.readAttributes(start, BasicFileAttributes.class);
            } catch (IOException ignored) {}
            if (attributes == null) {
                try {
                    attributes = Files.readAttributes(start, BasicFileAttributes.class,
                            LinkOption.NOFOLLOW_LINKS);
                } catch (IOException e) {
                    visitor.visitFileFailed(start, e);
                    return;
                }
            }
            if (!recursive || !attributes.isDirectory()) {
                visitor.visitFile(start, attributes);
                return;
            }
            DirectoryStream<Path> directoryStream;
            try {
                directoryStream = Files.newDirectoryStream(start);
            } catch (IOException e) {
                visitor.visitFileFailed(start, e);
                return;
            }
            IOException exception = null;
            try {
                visitor.preVisitDirectory(start, attributes);
                try {
                    for (Path path : directoryStream) {
                        Files.walkFileTree(path, visitor);
                    }
                } catch (DirectoryIteratorException e) {
                    exception = e.getCause();
                } catch (IOException e) {
                    exception = e;
                }
            } finally {
                directoryStream.close();
            }
            visitor.postVisitDirectory(start, exception);
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
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                    FileJobNotificationManager.CHANNEL_ID)
                    .setColor(ContextCompat.getColor(context, R.color.color_primary))
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSubText(subText)
                    .setContentInfo(info)
                    .setProgress(max, progress, indeterminate)
                    // TODO
                    //.setContentIntent();
                    .setOngoing(true)
                    .setCategory(Notifications.Channels.FILE_JOB.CATEGORY)
                    .setPriority(Notifications.Channels.FILE_JOB.PRIORITY);
            if (showCancel) {
                int id = getId();
                Intent intent = new Intent(context, FileJobReceiver.class)
                        .setAction(FileJobReceiver.ACTION_CANCEL)
                        .putExtra(FileJobReceiver.EXTRA_JOB_ID, id);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id + 1, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.close_icon_white_24dp, getString(
                        android.R.string.cancel), pendingIntent);
            }
            Notification notification = builder.build();
            postNotification(notification);
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
                return new Promise<ActionResult>(settler -> BackgroundActivityStarter.startActivity(
                        FileJobActionDialogActivity.newIntent(title, message, showAll,
                                positiveButtonText, negativeButtonText, neutralButtonText,
                                (action, all) -> settler.resolve(new ActionResult(action, all)),
                                service), title, message, service))
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
                                                    @NonNull FileItem targetFile,
                                                    @NonNull CopyMoveType type)
                throws IOException {
            Service service = getService();
            try {
                return new Promise<ConflictResult>(settler ->
                        BackgroundActivityStarter.startActivity(
                                FileJobConflictDialogActivity.newIntent(sourceFile, targetFile,
                                        type, (action, name, all) -> settler.resolve(
                                                new ConflictResult(action, name, all)), service),
                                FileJobConflictDialogActivity.getTitle(sourceFile, targetFile,
                                        service),
                                FileJobConflictDialogActivity.getMessage(sourceFile, targetFile,
                                        type, service),
                                service))
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

        enum CopyMoveType {

            COPY,
            EXTRACT,
            MOVE;

            public int getResource(@AnyRes int copyRes, @AnyRes int extractRes,
                                   @AnyRes int moveRes) {
                switch (this) {
                    case COPY:
                        return copyRes;
                    case EXTRACT:
                        return extractRes;
                    case MOVE:
                        return moveRes;
                    default:
                        throw new AssertionError(this);
                }
            }
        }

        protected static class TransferInfo {

            private int mFileCount;
            private int mTransferredFileCount;
            private long mSize;
            private long mTransferredSize;
            private final Path mTarget;

            private long mLastNotificationTimeMillis;

            public TransferInfo(@NonNull ScanInfo scanInfo, Path target) {
                mFileCount = scanInfo.getFileCount();
                mSize = scanInfo.getSize();
                mTarget = target;
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

            public Path getTarget() {
                return mTarget;
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
            public boolean skipRestoreSeLinuxContextError;
            public boolean skipSetGroupError;
            public boolean skipSetOwnerError;
            public boolean skipSetModeError;
            public boolean skipSetSeLinuxContextError;
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

    public static class Archive extends Base {

        @NonNull
        private final List<Path> mSources;
        @NonNull
        private final Path mArchiveFile;
        @NonNull
        private final String mArchiveType;
        @Nullable
        private final String mCompressorType;

        public Archive(@NonNull List<Path> sources, @NonNull Path archiveFile,
                       @NonNull String archiveType, @Nullable String compressorType) {
            mSources = sources;
            mArchiveFile = archiveFile;
            mArchiveType = archiveType;
            mCompressorType = compressorType;
        }

        @Override
        public void run() throws IOException {
            ScanInfo scanInfo = scan(mSources, R.plurals.file_job_archive_scan_notification_title);
            SeekableByteChannel channel = MoreFiles.newByteChannel(mArchiveFile,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            boolean successful = false;
            try (ArchiveWriter writer = new ArchiveWriter(mArchiveType, mCompressorType,
                    channel)) {
                TransferInfo transferInfo = new TransferInfo(scanInfo, mArchiveFile);
                for (Path source : mSources) {
                    Path target = getTargetFileName(source);
                    archiveRecursively(source, writer, target, transferInfo);
                    throwIfInterrupted();
                }
                successful = true;
            } finally {
                try {
                    channel.close();
                } finally {
                    if (!successful) {
                        try {
                            Files.deleteIfExists(mArchiveFile);
                        } catch (IOException | UnsupportedOperationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private void archiveRecursively(@NonNull Path source, @NonNull ArchiveWriter writer,
                                        @NonNull Path target, @NonNull TransferInfo transferInfo)
                throws IOException {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                         @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path directoryInTarget = MoreFiles.resolve(target, source.relativize(
                            directory));
                    archive(directory, writer, directoryInTarget, mArchiveFile, transferInfo);
                    throwIfInterrupted();
                    return FileVisitResult.CONTINUE;
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path fileInTarget = MoreFiles.resolve(target, source.relativize(file));
                    archive(file, writer, fileInTarget, mArchiveFile, transferInfo);
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
            boolean isExtract = Functional.every(mSources,
                    ArchiveFileSystemProvider::isArchivePath);
            ScanInfo scanInfo = scan(mSources, isExtract ?
                    R.plurals.file_job_extract_scan_notification_title
                    : R.plurals.file_job_copy_scan_notification_title);
            TransferInfo transferInfo = new TransferInfo(scanInfo, mTargetDirectory);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            for (Path source : mSources) {
                Path target;
                if (Objects.equals(source.getParent(), mTargetDirectory)) {
                    target = getTargetPathForDuplicate(source);
                } else {
                    target = MoreFiles.resolve(mTargetDirectory, getTargetFileName(source));
                }
                copyRecursively(source, target, isExtract, transferInfo, actionAllInfo);
                throwIfInterrupted();
            }
        }

        @NonNull
        private static Path getTargetPathForDuplicate(@NonNull Path source) {
            ByteStringListPath byteStringSource = MoreFiles.requireByteStringListPath(source);
            ByteString sourceFileName = MoreFiles.toByteString(source.getFileName());
            int extensionSeparatorIndex;
            // We do want to follow symbolic links here.
            if (Files.isDirectory(source)) {
                extensionSeparatorIndex = -1;
            } else {
                extensionSeparatorIndex = PathFileNameUtils.indexOfFullExtensionSeparator(
                        sourceFileName);
            }
            int countEndIndex = extensionSeparatorIndex > 0 ? extensionSeparatorIndex
                    : sourceFileName.length();
            DuplicateCountInfo countInfo = getDuplicateCountInfo(sourceFileName, countEndIndex);
            for (int i = countInfo.count + 1; i > 0; ++i) {
                ByteString targetFileName = setDuplicateCount(sourceFileName, countInfo, i);
                Path target = byteStringSource.resolveSibling(targetFileName);
                if (!Files.exists(target)) {
                    return target;
                }
            }
            // Just leave it to conflict handling logic.
            return source;
        }

        @NonNull
        private static DuplicateCountInfo getDuplicateCountInfo(@NonNull ByteString fileName,
                                                                int countEnd) {
            while (true) {
                // /(?<=.) \(\d+\)$/
                int index = countEnd - 1;
                // \)
                if (index < 0 || fileName.byteAt(index) != ')') {
                    break;
                }
                --index;
                // \d+
                int digitsEndInclusive = index;
                while (index >= 0) {
                    byte b = fileName.byteAt(index);
                    if (b < '0' || b > '9') {
                        break;
                    }
                    --index;
                }
                if (index == digitsEndInclusive) {
                    break;
                }
                String countString = fileName.substring(index + 1, digitsEndInclusive + 1)
                        .toString();
                int count;
                try {
                    count = Integer.parseInt(countString);
                } catch (NumberFormatException e) {
                    break;
                }
                // \(
                if (index < 0 || fileName.byteAt(index) != '(') {
                    break;
                }
                --index;
                //
                if (index < 0 || fileName.byteAt(index) != ' ') {
                    break;
                }
                // (?<=.)
                if (index == 0) {
                    break;
                }
                return new DuplicateCountInfo(index, countEnd, count);
            }
            return new DuplicateCountInfo(countEnd, countEnd, 0);
        }

        @NonNull
        private static ByteString setDuplicateCount(@NonNull ByteString fileName,
                                                    @NonNull DuplicateCountInfo countInfo,
                                                    int count) {
            return new ByteStringBuilder(fileName.substring(0, countInfo.countStart))
                    .append(ByteString.fromString(" (" + count + ")"))
                    .append(fileName.substring(countInfo.countEnd))
                    .toByteString();
        }

        private static class DuplicateCountInfo {

            public final int countStart;
            public final int countEnd;
            public final int count;

            public DuplicateCountInfo(int countStart, int countEnd, int count) {
                this.countStart = countStart;
                this.countEnd = countEnd;
                this.count = count;
            }
        }

        private void copyRecursively(@NonNull Path source, @NonNull Path target, boolean isExtract,
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
                    boolean copied = copy(directory, directoryInTarget, isExtract, transferInfo,
                            actionAllInfo);
                    throwIfInterrupted();
                    return copied ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path fileInTarget = MoreFiles.resolve(target, source.relativize(file));
                    copy(file, fileInTarget, isExtract, transferInfo, actionAllInfo);
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
            TransferInfo transferInfo = new TransferInfo(scanInfo, null);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            for (Path path : mPaths) {
                deleteRecursively(path, transferInfo, actionAllInfo);
                throwIfInterrupted();
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
            TransferInfo transferInfo = new TransferInfo(scanInfo, mTargetDirectory);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            for (Path source : sourcesToMove) {
                Path target = MoreFiles.resolve(mTargetDirectory, source.getFileName());
                moveRecursively(source, target, transferInfo, actionAllInfo);
                throwIfInterrupted();
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
                    boolean copied = copyForMove(directory, directoryInTarget, transferInfo,
                            actionAllInfo);
                    throwIfInterrupted();
                    return copied ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
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

    public static class Open extends Base {

        @NonNull
        private final Path mFile;
        @NonNull
        private final String mMimeType;
        private final boolean mWithChooser;

        public Open(@NonNull Path file, @NonNull String mimeType, boolean withChooser) {
            mFile = file;
            mMimeType = mimeType;
            mWithChooser = withChooser;
        }

        @Override
        public void run() throws IOException {
            boolean isExtract = ArchiveFileSystemProvider.isArchivePath(mFile);
            ScanInfo scanInfo = scan(mFile, isExtract ?
                    R.plurals.file_job_extract_scan_notification_title
                    : R.plurals.file_job_copy_scan_notification_title);
            Context context = getService();
            Path cacheDirectory = Paths.get(getCacheDirectory().getPath(), "open_cache");
            Files.createDirectories(cacheDirectory);
            Path targetFileName = getTargetFileName(mFile);
            Path targetFile = MoreFiles.resolve(cacheDirectory, targetFileName);
            TransferInfo transferInfo = new TransferInfo(scanInfo, cacheDirectory);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            actionAllInfo.replace = true;
            copy(mFile, targetFile, isExtract, transferInfo, actionAllInfo);
            Uri uri = FileProvider.getUriForPath(targetFile);
            Intent intent = IntentUtils.makeView(uri, mMimeType)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            IntentPathUtils.putExtraPath(intent, targetFile);
            if (mWithChooser) {
                intent = IntentUtils.withChooser(intent);
                intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[] {
                        OpenFileAsDialogActivity.newIntent(targetFile, context)
                });
            }
            BackgroundActivityStarter.startActivity(intent,
                    getString(R.string.file_open_from_background_title_format, targetFileName),
                    getString(R.string.file_open_from_background_text), context);
        }

        @NonNull
        private File getCacheDirectory() {
            Context context = getService();
            File externalCacheDirectory = context.getExternalCacheDir();
            if (externalCacheDirectory != null && Objects.equals(
                    Environment.getExternalStorageState(externalCacheDirectory),
                    Environment.MEDIA_MOUNTED)) {
                return externalCacheDirectory;
            }
            return context.getCacheDir();
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

    public static class RestoreSeLinuxContext extends Base {

        @NonNull
        private final Path mPath;
        private final boolean mRecursive;

        public RestoreSeLinuxContext(@NonNull Path path, boolean recursive) {
            mPath = path;
            mRecursive = recursive;
        }

        @Override
        public void run() throws IOException {
            ScanInfo scanInfo = scan(mPath, mRecursive,
                    R.plurals.file_job_restore_selinux_context_scan_notification_title);
            TransferInfo transferInfo = new TransferInfo(scanInfo, null);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            walkFileTreeForSettingAttributes(mPath, mRecursive, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                         @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    return visitFile(directory, attributes);
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    restoreSeLinuxContext(file, !attributes.isSymbolicLink(), transferInfo,
                            actionAllInfo);
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
                    return super.postVisitDirectory(directory, exception);
                }
            });
        }
    }

    public static class SetGroup extends Base {

        @NonNull
        private final Path mPath;
        @NonNull
        private final PosixGroup mGroup;
        private final boolean mRecursive;

        public SetGroup(@NonNull Path path, @NonNull PosixGroup group, boolean recursive) {
            mPath = path;
            mGroup = group;
            mRecursive = recursive;
        }

        @Override
        public void run() throws IOException {
            ScanInfo scanInfo = scan(mPath, mRecursive,
                    R.plurals.file_job_set_group_scan_notification_title);
            TransferInfo transferInfo = new TransferInfo(scanInfo, null);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            walkFileTreeForSettingAttributes(mPath, mRecursive, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                         @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    return visitFile(directory, attributes);
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    setGroup(file, mGroup, !attributes.isSymbolicLink(), transferInfo,
                            actionAllInfo);
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
                    return super.postVisitDirectory(directory, exception);
                }
            });
        }
    }

    public static class SetMode extends Base {

        @NonNull
        private final Path mPath;
        @NonNull
        private final Set<PosixFileModeBit> mMode;
        private final boolean mRecursive;

        public SetMode(@NonNull Path path, @NonNull Set<PosixFileModeBit> mode, boolean recursive) {
            mPath = path;
            mMode = mode;
            mRecursive = recursive;
        }

        @Override
        public void run() throws IOException {
            ScanInfo scanInfo = scan(mPath, mRecursive,
                    R.plurals.file_job_set_mode_scan_notification_title);
            TransferInfo transferInfo = new TransferInfo(scanInfo, null);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            walkFileTreeForSettingAttributes(mPath, mRecursive, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                         @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    return visitFile(directory, attributes);
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    if (attributes.isSymbolicLink()) {
                        // We cannot set mode on symbolic links.
                        transferInfo.skipFileIgnoringSize();
                        return FileVisitResult.CONTINUE;
                    }
                    // The file may actually be a directory if we are not entering it.
                    Set<PosixFileModeBit> mode = !attributes.isDirectory() ? getFileMode(file)
                            : mMode;
                    setMode(file, mode, transferInfo, actionAllInfo);
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
                    return super.postVisitDirectory(directory, exception);
                }
            });
        }

        @NonNull
        private Set<PosixFileModeBit> getFileMode(@NonNull Path file) throws IOException {
            if (Objects.equals(file, mPath)) {
                return mMode;
            }
            Set<PosixFileModeBit> mode = EnumSet.copyOf(mMode);
            Set<PosixFileModeBit> currentMode = MoreFiles.getMode(file, LinkOption.NOFOLLOW_LINKS);
            if (!currentMode.contains(PosixFileModeBit.OWNER_EXECUTE)) {
                mode.remove(PosixFileModeBit.OWNER_EXECUTE);
            }
            if (!currentMode.contains(PosixFileModeBit.GROUP_EXECUTE)) {
                mode.remove(PosixFileModeBit.GROUP_EXECUTE);
            }
            if (!currentMode.contains(PosixFileModeBit.OTHERS_EXECUTE)) {
                mode.remove(PosixFileModeBit.OTHERS_EXECUTE);
            }
            return mode;
        }
    }

    public static class SetOwner extends Base {

        @NonNull
        private final Path mPath;
        @NonNull
        private final PosixUser mOwner;
        private final boolean mRecursive;

        public SetOwner(@NonNull Path path, @NonNull PosixUser owner, boolean recursive) {
            mPath = path;
            mOwner = owner;
            mRecursive = recursive;
        }

        @Override
        public void run() throws IOException {
            ScanInfo scanInfo = scan(mPath, mRecursive,
                    R.plurals.file_job_set_owner_scan_notification_title);
            TransferInfo transferInfo = new TransferInfo(scanInfo, null);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            walkFileTreeForSettingAttributes(mPath, mRecursive, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                         @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    return visitFile(directory, attributes);
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    setOwner(file, mOwner, !attributes.isSymbolicLink(), transferInfo,
                            actionAllInfo);
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
                    return super.postVisitDirectory(directory, exception);
                }
            });
        }
    }

    public static class SetSeLinuxContext extends Base {

        @NonNull
        private final Path mPath;
        @NonNull
        private final String mSeLinuxContext;
        private final boolean mRecursive;

        public SetSeLinuxContext(@NonNull Path path, @NonNull String seLinuxContext,
                                 boolean recursive) {
            mPath = path;
            mSeLinuxContext = seLinuxContext;
            mRecursive = recursive;
        }

        @Override
        public void run() throws IOException {
            ScanInfo scanInfo = scan(mPath, mRecursive,
                    R.plurals.file_job_set_selinux_context_scan_notification_title);
            TransferInfo transferInfo = new TransferInfo(scanInfo, null);
            ActionAllInfo actionAllInfo = new ActionAllInfo();
            walkFileTreeForSettingAttributes(mPath, mRecursive, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                         @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    return visitFile(directory, attributes);
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    setSeLinuxContext(file, mSeLinuxContext, !attributes.isSymbolicLink(),
                            transferInfo, actionAllInfo);
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
                    return super.postVisitDirectory(directory, exception);
                }
            });
        }
    }
}
