/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.AccessMode;
import java8.nio.file.CopyOption;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.ProviderMismatchException;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.attribute.FileOwnerAttributeView;
import java8.nio.file.attribute.GroupPrincipal;
import java8.nio.file.attribute.UserPrincipal;
import java8.nio.file.spi.FileSystemProvider;
import java9.util.function.Consumer;
import java9.util.function.LongConsumer;
import me.zhanghai.android.files.util.IoUtils;

public class MoreFiles {

    private static final int BUFFER_SIZE = 8192;

    private MoreFiles() {}

    public static void checkAccess(@NonNull Path path, @NonNull AccessMode... modes)
            throws IOException {
        provider(path).checkAccess(path, modes);
    }

    // Can handle ProgressCopyOption.
    public static void copy(@NonNull Path source, @NonNull Path target,
                            @NonNull CopyOption... options) throws IOException {
        FileSystemProvider sourceProvider = provider(source);
        if (sourceProvider == provider(target)) {
            sourceProvider.copy(source, target, options);
        } else {
            ForeignCopyMove.copy(source, target, options);
        }
    }

    public static void copy(@NonNull InputStream inputStream, @NonNull OutputStream outputStream,
                            @Nullable LongConsumer listener, long intervalMillis)
            throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long lastProgressMillis = System.currentTimeMillis();
        long copiedSize = 0;
        while (true) {
            int readSize = inputStream.read(buffer);
            if (readSize == -1) {
                break;
            }
            outputStream.write(buffer, 0, readSize);
            copiedSize += readSize;
            throwIfInterrupted();
            long currentTimeMillis = System.currentTimeMillis();
            if (listener != null && currentTimeMillis >= lastProgressMillis + intervalMillis) {
                listener.accept(copiedSize);
                lastProgressMillis = currentTimeMillis;
                copiedSize = 0;
            }
        }
        if (listener != null) {
            listener.accept(copiedSize);
        }
    }

    private static void throwIfInterrupted() throws InterruptedIOException {
        if (Thread.interrupted()) {
            throw new InterruptedIOException();
        }
    }

    @Nullable
    public static Set<PosixFileModeBit> getMode(@NonNull Path path, @NonNull LinkOption... options)
            throws IOException {
        return Files.readAttributes(path, PosixFileAttributes.class, options).mode();
    }

    // Can handle ProgressCopyOption.
    public static void move(@NonNull Path source, @NonNull Path target,
                            @NonNull CopyOption... options) throws IOException {
        FileSystemProvider sourceProvider = provider(source);
        if (sourceProvider == provider(target)) {
            sourceProvider.move(source, target, options);
        } else {
            ForeignCopyMove.move(source, target, options);
        }
    }

    @NonNull
    public static BufferedReader newBufferedReader(@NonNull Path path, @NonNull Charset charset,
                                                   @NonNull OpenOption... options)
            throws IOException {
        Objects.requireNonNull(charset);
        return new BufferedReader(new InputStreamReader(newInputStream(path, options),
                charset.newDecoder()));
    }

    @NonNull
    public static BufferedWriter newBufferedWriter(@NonNull Path path, @NonNull Charset charset,
                                                   @NonNull OpenOption... options)
            throws IOException {
        return new BufferedWriter(new OutputStreamWriter(newOutputStream(path, options),
                charset.newEncoder()));
    }

    @NonNull
    public static SeekableByteChannel newByteChannel(@NonNull Path path,
                                                     @NonNull Set<? extends OpenOption> options,
                                                     @NonNull FileAttribute<?>... attributes)
            throws IOException {
        try {
            return Files.newByteChannel(path, options, attributes);
        } catch (UnsupportedOperationException e) {
            throw new IOException(e);
        }
    }

    @NonNull
    public static SeekableByteChannel newByteChannel(@NonNull Path path,
                                                     @NonNull OpenOption... options)
            throws IOException {
        try {
            return Files.newByteChannel(path, options);
        } catch (UnsupportedOperationException e) {
            throw new IOException(e);
        }
    }

    @NonNull
    public static InputStream newInputStream(@NonNull Path path, @NonNull OpenOption... options)
            throws IOException {
        return new InterruptedIOExceptionInputStream(Files.newInputStream(path, options));
    }

    @NonNull
    public static OutputStream newOutputStream(@NonNull Path path, @NonNull OpenOption... options)
            throws IOException {
        return new InterruptedIOExceptionOutputStream(Files.newOutputStream(path, options));
    }

    @NonNull
    public static PathObservable observePath(@NonNull Path path, long intervalMillis)
            throws IOException {
        return ((PathObservableProvider) provider(path)).observePath(path, intervalMillis);
    }

    // TODO: Just use Files.readAllBytes(), if all our providers support
    //  newByteChannel()?
    // Uses newInputStream() instead of newByteChannel().
    @NonNull
    public static byte[] readAllBytes(@NonNull Path path) throws IOException {
        long sizeLong = size(path);
        if (sizeLong > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("size " + sizeLong);
        }
        int size = (int) sizeLong;
        try (InputStream inputStream = newInputStream(path)) {
            return IoUtils.inputStreamToByteArray(inputStream, size);
        }
    }

    @NonNull
    public static ByteString readSymbolicLink(@NonNull Path link) throws IOException {
        Path target = Files.readSymbolicLink(link);
        if (!(target instanceof ByteStringPath)) {
            throw new ProviderMismatchException(target.toString());
        }
        ByteStringPath targetPath = (ByteStringPath) target;
        return targetPath.toByteString();
    }

    // Can resolve path in a foreign provider.
    @NonNull
    public static Path resolve(@NonNull Path path, @NonNull Path other) {
        ByteStringListPath byteStringPath = requireByteStringListPath(path);
        ByteStringListPath otherPath = requireByteStringListPath(other);
        if (provider(byteStringPath) == provider(otherPath)) {
            return byteStringPath.resolve(otherPath);
        }
        if (otherPath.isAbsolute()) {
            return otherPath;
        }
        if (otherPath.isEmpty()) {
            return byteStringPath;
        }
        ByteStringListPath result = byteStringPath;
        for (int i = 0, count = otherPath.getNameCount(); i < count; ++i) {
            ByteString name = otherPath.getName(i).toByteString();
            result = result.resolve(name);
        }
        return result;
    }

    public static void restoreSeLinuxContext(@NonNull Path path, @NonNull LinkOption... options)
            throws IOException {
        PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class,
                options);
        if (view == null) {
            throw new UnsupportedOperationException();
        }
        view.restoreSeLinuxContext();
    }

    public static void search(@NonNull Path directory, @NonNull String query,
                              @NonNull Consumer<List<Path>> listener, long intervalMillis)
            throws IOException {
        ((Searchable) provider(directory)).search(directory, query, listener, intervalMillis);
    }

    public static void setGroup(@NonNull Path path, @NonNull GroupPrincipal group,
                                @NonNull LinkOption... options) throws IOException {
        java8.nio.file.attribute.PosixFileAttributeView view = Files.getFileAttributeView(path,
                java8.nio.file.attribute.PosixFileAttributeView.class, options);
        if (view == null) {
            throw new UnsupportedOperationException();
        }
        view.setGroup(group);
    }

    public static void setMode(@NonNull Path path, @NonNull Set<PosixFileModeBit> mode)
            throws IOException {
        PosixFileAttributeView view = Files.getFileAttributeView(path,
                PosixFileAttributeView.class);
        if (view == null) {
            throw new UnsupportedOperationException();
        }
        view.setMode(mode);
    }

    public static void setOwner(@NonNull Path path, @NonNull UserPrincipal owner,
                                @NonNull LinkOption... options) throws IOException {
        FileOwnerAttributeView view = Files.getFileAttributeView(path, FileOwnerAttributeView.class,
                options);
        if (view == null) {
            throw new UnsupportedOperationException();
        }
        view.setOwner(owner);
    }

    public static void setSeLinuxContext(@NonNull Path path, @NonNull ByteString seLinuxContext,
                                         @NonNull LinkOption... options)
            throws IOException {
        PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class,
                options);
        if (view == null) {
            throw new UnsupportedOperationException();
        }
        view.setSeLinuxContext(seLinuxContext);
    }

    // Can accept link options.
    public static long size(@NonNull Path path, @NonNull LinkOption... options) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class, options).size();
    }

    @NonNull
    public static ByteString toByteString(@NonNull Path path) {
        ByteStringListPath byteStringPath = requireByteStringListPath(path);
        return byteStringPath.toByteString();
    }

    @NonNull
    private static FileSystemProvider provider(@NonNull Path path) {
        Objects.requireNonNull(path);
        return path.getFileSystem().provider();
    }

    @NonNull
    public static ByteStringListPath requireByteStringListPath(@NonNull Path path) {
        if (!(path instanceof ByteStringListPath)) {
            throw new ProviderMismatchException(path.toString());
        }
        return (ByteStringListPath) path;
    }

    private static InterruptedIOException newInterruptedIOException(
            @NonNull ClosedByInterruptException e) {
        Thread.interrupted();
        InterruptedIOException exception = new InterruptedIOException();
        exception.initCause(e);
        return exception;
    }

    private static class InterruptedIOExceptionInputStream extends InputStream {

        @NonNull
        private final InputStream mInputStream;

        public InterruptedIOExceptionInputStream(@NonNull InputStream inputStream) {
            mInputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            try {
                return mInputStream.read();
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public int read(@NonNull byte[] b) throws IOException {
            try {
                return mInputStream.read(b);
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public int read(@NonNull byte[] b, int off, int len) throws IOException {
            try {
                return mInputStream.read(b, off, len);
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public long skip(long n) throws IOException {
            try {
                return mInputStream.skip(n);
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public int available() throws IOException {
            try {
                return mInputStream.available();
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public void close() throws IOException {
            try {
                mInputStream.close();
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public void mark(int readlimit) {
            mInputStream.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            try {
                mInputStream.reset();
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public boolean markSupported() {
            return mInputStream.markSupported();
        }
    }

    private static class InterruptedIOExceptionOutputStream extends OutputStream {

        @NonNull
        private final OutputStream mOutputStream;

        public InterruptedIOExceptionOutputStream(@NonNull OutputStream outputStream) {
            mOutputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            try {
                mOutputStream.write(b);
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public void write(@NonNull byte[] b) throws IOException {
            try {
                mOutputStream.write(b);
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public void write(@NonNull byte[] b, int off, int len) throws IOException {
            try {
                mOutputStream.write(b, off, len);
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public void flush() throws IOException {
            try {
                mOutputStream.flush();
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }

        @Override
        public void close() throws IOException {
            try {
                mOutputStream.close();
            } catch (ClosedByInterruptException e) {
                throw newInterruptedIOException(e);
            }
        }
    }
}
