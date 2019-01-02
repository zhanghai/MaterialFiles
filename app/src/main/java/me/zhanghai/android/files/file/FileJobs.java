/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

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
import me.zhanghai.android.files.provider.common.InvalidFileNameException;

public class FileJobs {

    private FileJobs() {}

    private abstract static class Base extends FileJob {

        protected static void copy(@NonNull Path source, @NonNull Path target)
                throws IOException {
            copyOrMove(source, target, true, false);
        }

        // @see https://github.com/GNOME/nautilus/blob/master/src/nautilus-file-operations.c
        //      copy_move_file
        private static void copyOrMove(@NonNull Path source, @NonNull Path target, boolean copy,
                                       boolean copyAttributes) throws IOException {
            Path targetParent = target.getParent();
            if (targetParent != null && targetParent.startsWith(source)) {
                // Don't allow copy/move into the source itself.
                // TODO: Prompt skip, skip-all or abort.
                throw new IOException(new IllegalArgumentException(
                        "Cannot copy/move a folder into itself"));
            }
            if (source.startsWith(target)) {
                // Don't allow copy/move over the source itself or its ancestors.
                // TODO: Prompt skip, skip-all or abort.
                throw new IOException(new IllegalArgumentException(
                        "Cannot copy/move over a path over itself"));
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
                    // TODO: Prompt overwrite, skip, skip-all, abort, or merge.
                    if (false) {
                        replaceExisting = true;
                        retry = true;
                        continue;
                    }
                    throw e;
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
                    // TODO: Prompt skip, skip-all or abort.
                    if (false) {
                        retry = true;
                        continue;
                    }
                    throw e;
                }
            } while (retry);
        }

        protected static void copyWithAttributes(@NonNull Path source, @NonNull Path target)
                throws IOException {
            copyOrMove(source, target, true, true);
        }

        protected void createDirectory(@NonNull Path path) throws IOException {
            Files.createDirectory(path);
        }

        protected void createFile(@NonNull Path path) throws IOException {
            Files.createFile(path);
        }

        protected static void delete(@NonNull Path path) throws IOException {
            boolean retry;
            do {
                retry = false;
                try {
                    Files.delete(path);
                } catch (InterruptedIOException e) {
                    throw e;
                } catch (IOException e) {
                    // TODO: Prompt skip, skip-all or abort.
                    if (false) {
                        retry = true;
                        continue;
                    }
                    throw e;
                }
            } while (retry);
        }

        protected static void moveAtomically(@NonNull Path source, @NonNull Path target)
                throws IOException {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        }

        protected static void moveByCopy(@NonNull Path source, @NonNull Path target)
                throws IOException {
            copyOrMove(source, target, false, true);
        }

        protected static void throwIfInterrupted() throws InterruptedIOException {
            if (Thread.interrupted()) {
                throw new InterruptedIOException();
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

        private static void copyRecursively(@NonNull Path source, @NonNull Path target)
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

        private static void deleteRecursively(@NonNull Path path) throws IOException {
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

        private static void moveRecursively(@NonNull Path source, @NonNull Path target)
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
