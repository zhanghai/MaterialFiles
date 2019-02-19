/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import android.app.Service;
import android.content.Intent;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.CopyOption;
import java8.nio.file.FileAlreadyExistsException;
import java8.nio.file.FileVisitResult;
import java8.nio.file.Files;
import java8.nio.file.Path;
import java8.nio.file.SimpleFileVisitor;
import java8.nio.file.StandardCopyOption;
import java8.nio.file.attribute.BasicFileAttributes;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.filelist.FileItem;
import me.zhanghai.android.files.promise.Promise;
import me.zhanghai.android.files.provider.common.InvalidFileNameException;

public class FileJobs {

    private FileJobs() {}

    private abstract static class Base extends FileJob {

        protected void copy(@NonNull Path source, @NonNull Path target) throws IOException {
            copyOrMove(source, target, true, false);
        }

        // @see https://github.com/GNOME/nautilus/blob/master/src/nautilus-file-operations.c
        //      copy_move_file
        private void copyOrMove(@NonNull Path source, @NonNull Path target, boolean copy,
                                boolean copyAttributes) throws IOException {
            Path targetParent = target.getParent();
            if (targetParent != null && targetParent.startsWith(source)) {
                // Don't allow copy/move into the source itself.
                Service service = getService();
                ActionResult result = showActionDialog(
                        service.getString(copy ? R.string.file_job_cannot_copy_into_itself_title
                                : R.string.file_job_cannot_move_into_itself_title),
                        service.getString(R.string.file_job_cannot_copy_move_into_itself_message),
                        true,
                        service.getString(R.string.file_job_action_skip),
                        service.getString(android.R.string.cancel),
                        null);
                switch (result.getAction()) {
                    case POSITIVE:
                    case CANCELED:
                        if (result.isAll()) {
                            // TODO: Turn on all.
                        }
                        return;
                    case NEGATIVE:
                        throw new InterruptedIOException();
                    default:
                        throw new AssertionError(result.getAction());
                }
            }
            if (source.startsWith(target)) {
                // Don't allow copy/move over the source itself or its ancestors.
                Service service = getService();
                ActionResult result = showActionDialog(
                        service.getString(copy ? R.string.file_job_cannot_copy_over_itself_title
                                : R.string.file_job_cannot_move_over_itself_title),
                        service.getString(R.string.file_job_cannot_copy_move_over_itself_message),
                        true,
                        service.getString(R.string.file_job_action_skip),
                        service.getString(android.R.string.cancel),
                        null);
                switch (result.getAction()) {
                    case POSITIVE:
                    case CANCELED:
                        if (result.isAll()) {
                            // TODO: Turn on all.
                        }
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
                if (copyAttributes) {
                    optionList.add(StandardCopyOption.COPY_ATTRIBUTES);
                }
                if (replaceExisting) {
                    optionList.add(StandardCopyOption.REPLACE_EXISTING);
                }
                CopyOption[] options = optionList.toArray(new CopyOption[0]);
                try {
                    if (copy) {
                        Files.copy(source, target, options);
                    } else {
                        Files.move(source, target, options);
                    }
                } catch (FileAlreadyExistsException e) {
                    FileItem sourceFile = FileItem.load(source);
                    FileItem targetFile = FileItem.load(target);
                    if (!sourceFile.getAttributesNoFollowLinks().isDirectory()
                            && targetFile.getAttributesNoFollowLinks().isDirectory()) {
                        // TODO: Don't allow replace directory with file.
                        throw e;
                    } else {
                        ConflictResult result = showConflictDialog(sourceFile, targetFile, copy);
                        switch (result.getAction()) {
                            case REPLACE_OR_MERGE:
                                if (result.isAll()) {
                                    // TODO: Turn on all.
                                }
                                replaceExisting = true;
                                retry = true;
                                continue;
                            case RENAME:
                                target = target.resolveSibling(result.getName());
                                retry = true;
                                continue;
                            case SKIP:
                            case CANCELED:
                                if (result.isAll()) {
                                    // TODO: Turn on all.
                                }
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
                    Service service = getService();
                    ActionResult result = showActionDialog(
                            service.getString(copy ? R.string.file_job_copy_error_title_format
                                    : R.string.file_job_move_error_title_format,
                                    source.getFileName()),
                            service.getString(copy ? R.string.file_job_copy_error_message_format
                                    : R.string.file_job_move_error_message_format,
                                    targetParent.getFileName(), e.getLocalizedMessage()),
                            true,
                            service.getString(R.string.file_job_action_retry),
                            service.getString(R.string.file_job_action_skip),
                            service.getString(android.R.string.cancel));
                    switch (result.getAction()) {
                        case POSITIVE:
                            retry = true;
                            continue;
                        case NEGATIVE:
                        case CANCELED:
                            if (result.isAll()) {
                                // TODO: Turn on all.
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

        protected void copyWithAttributes(@NonNull Path source, @NonNull Path target)
                throws IOException {
            copyOrMove(source, target, true, true);
        }

        protected void createDirectory(@NonNull Path path) throws IOException {
            Files.createDirectory(path);
        }

        protected void createFile(@NonNull Path path) throws IOException {
            Files.createFile(path);
        }

        protected void delete(@NonNull Path path) throws IOException {
            boolean retry;
            do {
                retry = false;
                try {
                    Files.delete(path);
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    Service service = getService();
                    ActionResult result = showActionDialog(
                            service.getString(R.string.file_job_delete_error_title),
                            service.getString(R.string.file_job_delete_error_message_format,
                                    path.getFileName(), e.getLocalizedMessage()),
                            true,
                            service.getString(R.string.file_job_action_retry),
                            service.getString(R.string.file_job_action_skip),
                            service.getString(android.R.string.cancel));
                    switch (result.getAction()) {
                        case POSITIVE:
                            retry = true;
                            continue;
                        case NEGATIVE:
                        case CANCELED:
                            if (result.isAll()) {
                                // TODO: Turn on all.
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

        protected void moveAtomically(@NonNull Path source, @NonNull Path target)
                throws IOException {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        }

        protected void moveByCopy(@NonNull Path source, @NonNull Path target) throws IOException {
            copyOrMove(source, target, false, true);
        }

        protected void throwIfInterrupted() throws InterruptedIOException {
            if (Thread.interrupted()) {
                throw new InterruptedIOException();
            }
        }

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
            for (Path source : mSources) {
                Path target = mTargetDirectory.resolve(source.getFileName());
                copyRecursively(source, target);
                throwIfInterrupted();
            }
        }

        private void copyRecursively(@NonNull Path source, @NonNull Path target)
                throws IOException {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                         @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path directoryInTarget = target.resolve(source.relativize(directory));
                    copy(directory, directoryInTarget);
                    throwIfInterrupted();
                    return FileVisitResult.CONTINUE;
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path fileInTarget = target.resolve(source.relativize(file));
                    copy(file, fileInTarget);
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
            for (Path path : mPaths) {
                deleteRecursively(path);
                throwIfInterrupted();
            }
        }

        private void deleteRecursively(@NonNull Path path) throws IOException {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    delete(file);
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
                    delete(directory);
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
                Path target = mTargetDirectory.resolve(source.getFileName());
                try {
                    moveAtomically(source, target);
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    sourcesToMove.add(source);
                }
                throwIfInterrupted();
            }
            for (Path source : sourcesToMove) {
                Path target = mTargetDirectory.resolve(source.getFileName());
                moveRecursively(source, target);
                throwIfInterrupted();
            }
        }

        private void moveRecursively(@NonNull Path source, @NonNull Path target)
                throws IOException {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @NonNull
                @Override
                public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                         @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path directoryInTarget = target.resolve(source.relativize(directory));
                    try {
                        moveAtomically(directory, directoryInTarget);
                        throwIfInterrupted();
                        return FileVisitResult.SKIP_SUBTREE;
                    } catch (InterruptedIOException e) {
                        throw e;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    copyWithAttributes(directory, directoryInTarget);
                    throwIfInterrupted();
                    return FileVisitResult.CONTINUE;
                }
                @NonNull
                @Override
                public FileVisitResult visitFile(@NonNull Path file,
                                                 @NonNull BasicFileAttributes attributes)
                        throws IOException {
                    Path fileInTarget = target.resolve(source.relativize(file));
                    try {
                        moveAtomically(file, fileInTarget);
                        throwIfInterrupted();
                        return FileVisitResult.CONTINUE;
                    } catch (InterruptedIOException e) {
                        throw e;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    moveByCopy(file, fileInTarget);
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
                    delete(directory);
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
