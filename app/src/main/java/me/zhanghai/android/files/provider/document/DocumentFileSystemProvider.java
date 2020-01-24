/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.channels.FileChannel;
import java8.nio.channels.SeekableByteChannel;
import java8.nio.file.AccessDeniedException;
import java8.nio.file.AccessMode;
import java8.nio.file.CopyOption;
import java8.nio.file.DirectoryStream;
import java8.nio.file.FileAlreadyExistsException;
import java8.nio.file.FileStore;
import java8.nio.file.FileSystem;
import java8.nio.file.FileSystemAlreadyExistsException;
import java8.nio.file.FileSystemNotFoundException;
import java8.nio.file.LinkOption;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.ProviderMismatchException;
import java8.nio.file.StandardOpenOption;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttribute;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import java9.util.Objects;
import java9.util.function.Consumer;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.provider.common.AccessModes;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringPath;
import me.zhanghai.android.files.provider.common.ByteStringUriUtils;
import me.zhanghai.android.files.provider.common.CopyOptions;
import me.zhanghai.android.files.provider.common.MoreFileChannels;
import me.zhanghai.android.files.provider.common.OpenOptions;
import me.zhanghai.android.files.provider.common.PathListDirectoryStream;
import me.zhanghai.android.files.provider.common.PathObservable;
import me.zhanghai.android.files.provider.common.PathObservableProvider;
import me.zhanghai.android.files.provider.common.Searchable;
import me.zhanghai.android.files.provider.common.WalkFileTreeSearchable;
import me.zhanghai.android.files.provider.content.resolver.Resolver;
import me.zhanghai.android.files.provider.content.resolver.ResolverException;
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver;

public class DocumentFileSystemProvider extends FileSystemProvider
        implements PathObservableProvider, Searchable {

    static final String SCHEME = "document";

    private static final ByteString HIDDEN_FILE_NAME_PREFIX = ByteString.fromString(".");

    private static DocumentFileSystemProvider sInstance;
    private static final Object sInstanceLock = new Object();

    @NonNull
    private final Map<Uri, DocumentFileSystem> mFileSystems = new HashMap<>();

    private final Object mLock = new Object();

    private DocumentFileSystemProvider() {}

    public static void install() {
        synchronized (sInstanceLock) {
            if (sInstance != null) {
                throw new IllegalStateException();
            }
            sInstance = new DocumentFileSystemProvider();
            FileSystemProvider.installProvider(sInstance);
        }
    }

    public static boolean isDocumentPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        return path instanceof DocumentPath;
    }

    @NonNull
    public static Uri getDocumentUri(@NonNull Path path) throws IOException {
        DocumentPath documentPath = requireDocumentPath(path);
        try {
            return DocumentResolver.getDocumentUri(documentPath);
        } catch (ResolverException e) {
            throw e.toFileSystemException(path.toString());
        }
    }

    @NonNull
    public static Uri getTreeUri(@NonNull Path path) {
        DocumentPath documentPath = requireDocumentPath(path);
        return documentPath.getTreeUri();
    }

    @NonNull
    public static Path getRootPathForTreeUri(@NonNull Uri treeUri) {
        Objects.requireNonNull(treeUri);
        return getOrNewFileSystem(treeUri).getRootDirectory();
    }

    @NonNull
    static DocumentFileSystem getOrNewFileSystem(@NonNull Uri treeUri) {
        synchronized (sInstance.mLock) {
            DocumentFileSystem fileSystem = sInstance.mFileSystems.get(treeUri);
            if (fileSystem != null) {
                return fileSystem;
            }
            return sInstance.newFileSystemLocked(treeUri);
        }
    }

    void removeFileSystem(@NonNull DocumentFileSystem fileSystem) {
        synchronized (sInstance.mLock) {
            Uri treeUri = fileSystem.getTreeUri();
            sInstance.mFileSystems.remove(treeUri);
        }
    }

    @NonNull
    @Override
    public String getScheme() {
        return SCHEME;
    }

    @NonNull
    @Override
    public FileSystem newFileSystem(@NonNull URI uri, @NonNull Map<String, ?> env) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        Objects.requireNonNull(env);
        Uri treeUri = getTreeUriFromUri(uri);
        synchronized (mLock) {
            DocumentFileSystem fileSystem = mFileSystems.get(treeUri);
            if (fileSystem != null) {
                throw new FileSystemAlreadyExistsException(treeUri.toString());
            }
            return newFileSystemLocked(treeUri);
        }
    }

    @NonNull
    private DocumentFileSystem newFileSystemLocked(@NonNull Uri treeUri) {
        DocumentFileSystem fileSystem = new DocumentFileSystem(this, treeUri);
        mFileSystems.put(treeUri, fileSystem);
        return fileSystem;
    }

    @NonNull
    @Override
    public FileSystem getFileSystem(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        Uri treeUri = getTreeUriFromUri(uri);
        synchronized (mLock) {
            DocumentFileSystem fileSystem = mFileSystems.get(treeUri);
            if (fileSystem == null) {
                throw new FileSystemNotFoundException(treeUri.toString());
            }
            return fileSystem;
        }
    }

    @NonNull
    private static Uri getTreeUriFromUri(@NonNull URI uri) {
        ByteString schemeSpecificPart = ByteStringUriUtils.getDecodedSchemeSpecificPart(uri);
        if (schemeSpecificPart == null) {
            throw new IllegalArgumentException("URI must have a scheme specific part");
        }
        return Uri.parse(schemeSpecificPart.toString());
    }

    @NonNull
    @Override
    public Path getPath(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        Uri treeUri = getTreeUriFromUri(uri);
        ByteString fragment = ByteStringUriUtils.getDecodedFragment(uri);
        if (fragment == null) {
            throw new IllegalArgumentException("URI must have a fragment");
        }
        return getOrNewFileSystem(treeUri).getPath(fragment);
    }

    private static void requireSameScheme(@NonNull URI uri) {
        if (!Objects.equals(uri.getScheme(), SCHEME)) {
            throw new IllegalArgumentException("URI scheme must be \"" + SCHEME + "\"");
        }
    }

    @NonNull
    @Override
    public InputStream newInputStream(@NonNull Path file, @NonNull OpenOption... options)
            throws IOException {
        DocumentPath documentFile = requireDocumentPath(file);
        Objects.requireNonNull(options);
        Set<OpenOption> optionsSet = new HashSet<>(Arrays.asList(options));
        boolean hasCreate = optionsSet.remove(StandardOpenOption.CREATE);
        boolean hasCreateNew = optionsSet.remove(StandardOpenOption.CREATE_NEW);
        OpenOptions openOptions = OpenOptions.fromSet(optionsSet);
        if (openOptions.hasWrite()) {
            throw new UnsupportedOperationException(StandardOpenOption.WRITE.toString());
        }
        if (openOptions.hasAppend()) {
            throw new UnsupportedOperationException(StandardOpenOption.APPEND.toString());
        }
        String mode = DocumentOpenOptions.toMode(openOptions);
        if (hasCreate || hasCreateNew) {
            boolean exists = DocumentResolver.exists(documentFile);
            if (hasCreateNew && exists) {
                throw new FileAlreadyExistsException(file.toString());
            }
            if (!exists) {
                Uri uri;
                try {
                    // TODO: Allow passing in a mime type?
                    uri = DocumentResolver.create(documentFile, MimeTypes.GENERIC_MIME_TYPE);
                } catch (ResolverException e) {
                    throw e.toFileSystemException(file.toString());
                }
                try {
                    return Resolver.openInputStream(uri, mode);
                } catch (ResolverException e) {
                    throw e.toFileSystemException(uri.toString());
                }
            }
        }
        try {
            return DocumentResolver.openInputStream(documentFile, mode);
        } catch (ResolverException e) {
            throw e.toFileSystemException(file.toString());
        }
    }

    @NonNull
    @Override
    public OutputStream newOutputStream(@NonNull Path file, @NonNull OpenOption... options)
            throws IOException {
        DocumentPath documentFile = requireDocumentPath(file);
        Objects.requireNonNull(options);
        Set<OpenOption> optionsSet = new HashSet<>(Arrays.asList(options));
        if (optionsSet.isEmpty()) {
            optionsSet.add(StandardOpenOption.CREATE);
            optionsSet.add(StandardOpenOption.TRUNCATE_EXISTING);
        }
        optionsSet.add(StandardOpenOption.WRITE);
        boolean hasCreate = optionsSet.remove(StandardOpenOption.CREATE);
        boolean hasCreateNew = optionsSet.remove(StandardOpenOption.CREATE_NEW);
        OpenOptions openOptions = OpenOptions.fromSet(optionsSet);
        String mode = DocumentOpenOptions.toMode(openOptions);
        if (hasCreate || hasCreateNew) {
            boolean exists = DocumentResolver.exists(documentFile);
            if (hasCreateNew && exists) {
                throw new FileAlreadyExistsException(file.toString());
            }
            if (!exists) {
                Uri uri;
                try {
                    // TODO: Allow passing in a mime type?
                    uri = DocumentResolver.create(documentFile, MimeTypes.GENERIC_MIME_TYPE);
                } catch (ResolverException e) {
                    throw e.toFileSystemException(file.toString());
                }
                try {
                    return Resolver.openOutputStream(uri, mode);
                } catch (ResolverException e) {
                    throw e.toFileSystemException(uri.toString());
                }
            }
        }
        try {
            return DocumentResolver.openOutputStream(documentFile, mode);
        } catch (ResolverException e) {
            throw e.toFileSystemException(file.toString());
        }
    }

    @NonNull
    @Override
    public FileChannel newFileChannel(@NonNull Path file,
                                      @NonNull Set<? extends OpenOption> options,
                                      @NonNull FileAttribute<?>... attributes) throws IOException {
        DocumentPath documentFile = requireDocumentPath(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        boolean hasCreate = options.remove(StandardOpenOption.CREATE);
        boolean hasCreateNew = options.remove(StandardOpenOption.CREATE_NEW);
        OpenOptions openOptions = OpenOptions.fromSet(options);
        String mode = DocumentOpenOptions.toMode(openOptions);
        if (attributes.length > 0) {
            throw new UnsupportedOperationException(Arrays.toString(attributes));
        }
        ParcelFileDescriptor pfd = null;
        if (hasCreate || hasCreateNew) {
            boolean exists = DocumentResolver.exists(documentFile);
            if (hasCreateNew && exists) {
                throw new FileAlreadyExistsException(file.toString());
            }
            if (!exists) {
                Uri uri;
                try {
                    // TODO: Allow passing in a mime type?
                    uri = DocumentResolver.create(documentFile, MimeTypes.GENERIC_MIME_TYPE);
                } catch (ResolverException e) {
                    throw e.toFileSystemException(file.toString());
                }
                try {
                    pfd = Resolver.openParcelFileDescriptor(uri, mode);
                } catch (ResolverException e) {
                    throw e.toFileSystemException(uri.toString());
                }
            }
        }
        if (pfd == null) {
            try {
                pfd = DocumentResolver.openParcelFileDescriptor(documentFile, mode);
            } catch (ResolverException e) {
                throw e.toFileSystemException(file.toString());
            }
        }
        return MoreFileChannels.open(pfd, mode);
    }

    @NonNull
    @Override
    public SeekableByteChannel newByteChannel(@NonNull Path file,
                                              @NonNull Set<? extends OpenOption> options,
                                              @NonNull FileAttribute<?>... attributes)
            throws IOException {
        requireDocumentPath(file);
        Objects.requireNonNull(options);
        Objects.requireNonNull(attributes);
        return newFileChannel(file, options, attributes);
    }

    @NonNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(
            @NonNull Path directory, @NonNull DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        DocumentPath documentDirectory = requireDocumentPath(directory);
        Objects.requireNonNull(filter);
        List<Path> children;
        try {
            //noinspection unchecked
            children = (List<Path>) (List<?>) DocumentResolver.queryChildren(documentDirectory);
        } catch (ResolverException e) {
            throw e.toFileSystemException(directory.toString());
        }
        // TODO: Handle DocumentsContract.EXTRA_LOADING, EXTRA_INFO and EXTRA_ERROR.
        return new PathListDirectoryStream(children, filter);
    }

    @Override
    public void createDirectory(@NonNull Path directory, @NonNull FileAttribute<?>... attributes)
            throws IOException {
        DocumentPath documentDirectory = requireDocumentPath(directory);
        Objects.requireNonNull(attributes);
        if (attributes.length > 0) {
            throw new UnsupportedOperationException(Arrays.toString(attributes));
        }
        try {
            DocumentResolver.create(documentDirectory, MimeTypes.DIRECTORY_MIME_TYPE);
        } catch (ResolverException e) {
            throw e.toFileSystemException(directory.toString());
        }
    }

    @Override
    public void createSymbolicLink(@NonNull Path link, @NonNull Path target,
                                   @NonNull FileAttribute<?>... attributes) {
        requireDocumentPath(link);
        requireDocumentOrByteStringPath(target);
        Objects.requireNonNull(attributes);
        throw new UnsupportedOperationException();
    }

    @Override
    public void createLink(@NonNull Path link, @NonNull Path existing) {
        requireDocumentPath(link);
        requireDocumentPath(existing);
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(@NonNull Path path) throws IOException {
        DocumentPath documentPath = requireDocumentPath(path);
        try {
            DocumentResolver.remove(documentPath);
        } catch (ResolverException e) {
            throw e.toFileSystemException(path.toString());
        }
    }

    @NonNull
    @Override
    public Path readSymbolicLink(@NonNull Path link) {
        requireDocumentPath(link);
        throw new UnsupportedOperationException();
    }

    @Override
    public void copy(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        DocumentPath documentSource = requireDocumentPath(source);
        DocumentPath documentTarget = requireDocumentPath(target);
        Objects.requireNonNull(options);
        CopyOptions copyOptions = CopyOptions.fromArray(options);
        DocumentCopyMove.copy(documentSource, documentTarget, copyOptions);
    }

    @Override
    public void move(@NonNull Path source, @NonNull Path target, @NonNull CopyOption... options)
            throws IOException {
        DocumentPath documentSource = requireDocumentPath(source);
        DocumentPath documentTarget = requireDocumentPath(target);
        Objects.requireNonNull(options);
        CopyOptions copyOptions = CopyOptions.fromArray(options);
        DocumentCopyMove.move(documentSource, documentTarget, copyOptions);
    }

    @Override
    public boolean isSameFile(@NonNull Path path, @NonNull Path path2) {
        requireDocumentPath(path);
        Objects.requireNonNull(path2);
        // TODO: DocumentsContract.findDocumentPath()?
        return Objects.equals(path, path2);
    }

    @Override
    public boolean isHidden(@NonNull Path path) {
        DocumentPath documentPath = requireDocumentPath(path);
        DocumentPath fileName = documentPath.getFileName();
        if (fileName == null) {
            return false;
        }
        ByteString fileNameBytes = fileName.toByteString();
        return fileNameBytes.startsWith(HIDDEN_FILE_NAME_PREFIX);
    }

    @NonNull
    @Override
    public FileStore getFileStore(@NonNull Path path) {
        requireDocumentPath(path);
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        DocumentPath documentPath = requireDocumentPath(path);
        Objects.requireNonNull(modes);
        AccessModes accessModes = AccessModes.fromArray(modes);
        if (accessModes.hasExecute()) {
            throw new AccessDeniedException(path.toString());
        }
        if (accessModes.hasWrite()) {
            try (OutputStream outputStream = DocumentResolver.openOutputStream(documentPath, "w")) {
                // Do nothing.
            } catch (ResolverException e) {
                throw e.toFileSystemException(path.toString());
            }
        }
        if (accessModes.hasRead()) {
            try (InputStream inputStream = DocumentResolver.openInputStream(documentPath, "r")) {
                // Do nothing.
            } catch (ResolverException e) {
                throw e.toFileSystemException(path.toString());
            }
        }
        if (!(accessModes.hasRead() || accessModes.hasWrite())) {
            try {
                DocumentResolver.checkExistence(documentPath);
            } catch (ResolverException e) {
                throw e.toFileSystemException(path.toString());
            }
        }
    }

    @Nullable
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(@NonNull Path path,
                                                                @NonNull Class<V> type,
                                                                @NonNull LinkOption... options) {
        requireDocumentPath(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        if (!supportsFileAttributeView(type)) {
            return null;
        }
        //noinspection unchecked
        return (V) getFileAttributeView(path);
    }

    static boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return type.isAssignableFrom(DocumentFileAttributeView.class);
    }

    @NonNull
    @Override
    public <A extends BasicFileAttributes> A readAttributes(@NonNull Path path,
                                                            @NonNull Class<A> type,
                                                            @NonNull LinkOption... options)
            throws IOException {
        requireDocumentPath(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        if (!type.isAssignableFrom(DocumentFileAttributes.class)) {
            throw new UnsupportedOperationException(type.toString());
        }
        //noinspection unchecked
        return (A) getFileAttributeView(path).readAttributes();
    }

    private static DocumentFileAttributeView getFileAttributeView(@NonNull Path path) {
        DocumentPath documentPath = requireDocumentPath(path);
        return new DocumentFileAttributeView(documentPath);
    }

    @NonNull
    @Override
    public Map<String, Object> readAttributes(@NonNull Path path, @NonNull String attributes,
                                              @NonNull LinkOption... options) {
        requireDocumentPath(path);
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(@NonNull Path path, @NonNull String attribute, @NonNull Object value,
                             @NonNull LinkOption... options) {
        requireDocumentPath(path);
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(value);
        Objects.requireNonNull(options);
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public PathObservable observePath(@NonNull Path path, long intervalMillis) throws IOException {
        DocumentPath documentDirectory = requireDocumentPath(path);
        return new DocumentPathObservable(documentDirectory, intervalMillis);
    }

    @Override
    public void search(@NonNull Path directory, @NonNull String query,
                       @NonNull Consumer<List<Path>> listener, long intervalMillis)
            throws IOException {
        requireDocumentPath(directory);
        Objects.requireNonNull(query);
        Objects.requireNonNull(listener);
        WalkFileTreeSearchable.search(directory, query, listener, intervalMillis);
    }

    private static DocumentPath requireDocumentPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (!(path instanceof DocumentPath)) {
            throw new ProviderMismatchException(path.toString());
        }
        return (DocumentPath) path;
    }

    private static ByteString requireDocumentOrByteStringPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (path instanceof DocumentPath) {
            return ((DocumentPath) path).toByteString();
        } else if (path instanceof ByteStringPath) {
            return ((ByteStringPath) path).toByteString();
        } else {
            throw new ProviderMismatchException(path.toString());
        }
    }
}
