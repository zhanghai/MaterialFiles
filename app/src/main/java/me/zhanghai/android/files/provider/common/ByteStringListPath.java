/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.InvalidPathException;
import java8.nio.file.Path;
import java8.nio.file.ProviderMismatchException;
import me.zhanghai.android.files.util.CollectionUtils;

public abstract class ByteStringListPath<PathType extends ByteStringListPath<PathType>>
        extends AbstractPath<PathType> implements Parcelable {

    private static final ByteString BYTE_STRING_DOT = ByteString.fromString(".");
    private static final ByteString BYTE_STRING_DOT_DOT = ByteString.fromString("..");

    private final byte mSeparator;

    private final boolean mAbsolute;

    @NonNull
    private final List<ByteString> mNames;

    @Nullable
    private ByteString mByteStringCache;

    public ByteStringListPath(byte separator, @NonNull ByteString path) {
        Objects.requireNonNull(path);
        if (separator == '\0') {
            throw new IllegalArgumentException("Separator cannot be a nul character");
        }
        mSeparator = separator;
        requireNoNulCharacter(path);
        List<ByteString> names = new ArrayList<>();
        if (path.isEmpty()) {
            names.add(ByteString.EMPTY);
        } else {
            for (int start = 0, end, length = path.length(); start < length; ) {
                while (start < length && path.byteAt(start) == mSeparator) {
                    ++start;
                }
                if (start == length) {
                    break;
                }
                end = start + 1;
                while (end < length && path.byteAt(end) != mSeparator) {
                    ++end;
                }
                names.add(path.substring(start, end));
                start = end;
            }
        }
        mNames = Collections.unmodifiableList(names);
        mAbsolute = isPathAbsolute(path);
        requireNameIfNonAbsolute();
    }

    private static void requireNoNulCharacter(@NonNull ByteString input) {
        for (int i = 0, length = input.length(); i < length; ++i) {
            if (input.byteAt(i) == '\0') {
                throw new InvalidPathException(input.toString(),
                        "Path cannot contain nul character", i);
            }
        }
    }

    protected ByteStringListPath(byte separator, boolean absolute,
                                 @NonNull List<ByteString> names) {
        mSeparator = separator;
        mAbsolute = absolute;
        mNames = Collections.unmodifiableList(names);
        requireNameIfNonAbsolute();
    }

    private void requireNameIfNonAbsolute() {
        if (!mAbsolute && mNames.isEmpty()) {
            throw new AssertionError("Non-absolute path must have at least one name");
        }
    }

    protected final byte getSeparator() {
        return mSeparator;
    }

    @Override
    public final boolean isAbsolute() {
        return mAbsolute;
    }

    @Nullable
    public final ByteString getByteStringFileName() {
        int nameCount = mNames.size();
        if (nameCount == 0) {
            return null;
        }
        return getByteStringName(nameCount - 1);
    }

    @Override
    public final int getNameCount() {
        return mNames.size();
    }

    @NonNull
    @Override
    public final PathType getName(int index) {
        ByteString name = getByteStringName(index);
        return createPath(false, Collections.singletonList(name));
    }

    @NonNull
    public final ByteString getByteStringName(int index) {
        if (index < 0 || index >= mNames.size()) {
            throw new IllegalArgumentException();
        }
        return mNames.get(index);
    }

    @NonNull
    @Override
    public PathType subpath(int beginIndex, int endIndex) {
        int namesSize = mNames.size();
        if (!(beginIndex >= 0 && beginIndex < endIndex && endIndex <= namesSize)) {
            throw new IllegalArgumentException();
        }
        List<ByteString> subNames = new ArrayList<>(mNames.subList(beginIndex, endIndex));
        return createPath(false, subNames);
    }

    @Override
    public boolean startsWith(@NonNull Path other) {
        Objects.requireNonNull(other);
        if (other == this) {
            return true;
        }
        if (other.getClass() != getClass()) {
            return false;
        }
        //noinspection unchecked
        ByteStringListPath<PathType> otherPath = (ByteStringListPath<PathType>) other;
        return CollectionUtils.startsWith(mNames, otherPath.mNames);
    }

    public boolean startsWith(@NonNull ByteString other) {
        Objects.requireNonNull(other);
        return startsWith(createPath(other));
    }

    @Override
    public boolean endsWith(@NonNull Path other) {
        Objects.requireNonNull(other);
        if (other == this) {
            return true;
        }
        if (other.getClass() != getClass()) {
            return false;
        }
        //noinspection unchecked
        ByteStringListPath<PathType> otherPath = (ByteStringListPath<PathType>) other;
        return CollectionUtils.endsWith(mNames, otherPath.mNames);
    }

    public boolean endsWith(@NonNull ByteString other) {
        Objects.requireNonNull(other);
        return endsWith(createPath(other));
    }

    @NonNull
    @Override
    public PathType normalize() {
        List<ByteString> normalizedNames = new ArrayList<>();
        for (ByteString name : mNames) {
            if (Objects.equals(name, BYTE_STRING_DOT)) {
                // Ignored.
            } else if (Objects.equals(name, BYTE_STRING_DOT_DOT)) {
                int normalizedNamesSize = normalizedNames.size();
                if (normalizedNamesSize == 0) {
                    if (!mAbsolute) {
                        normalizedNames.add(name);
                    }
                } else {
                    int normalizedNamesLastIndex = normalizedNamesSize - 1;
                    if (Objects.equals(normalizedNames.get(normalizedNamesLastIndex),
                            BYTE_STRING_DOT_DOT)) {
                        normalizedNames.add(name);
                    } else {
                        normalizedNames.remove(normalizedNamesLastIndex);
                    }
                }
            } else {
                normalizedNames.add(name);
            }
        }
        if (!mAbsolute && normalizedNames.isEmpty()) {
            return createEmptyPath();
        }
        return createPath(mAbsolute, normalizedNames);
    }

    @NonNull
    @Override
    public PathType resolve(@NonNull Path other) {
        Objects.requireNonNull(other);
        PathType otherPath = requireSameClassPath(other);
        ByteStringListPath<PathType> otherByteStringPath = otherPath;
        if (otherByteStringPath.mAbsolute) {
            return otherPath;
        }
        if (otherPath.isEmpty()) {
            //noinspection unchecked
            return (PathType) this;
        }
        if (isEmpty()) {
            return otherPath;
        }
        List<ByteString> resolvedNames = new ArrayList<>(CollectionUtils.join(mNames,
                otherByteStringPath.mNames));
        return createPath(mAbsolute, resolvedNames);
    }

    @NonNull
    public PathType resolve(@NonNull ByteString other) {
        Objects.requireNonNull(other);
        return resolve(createPath(other));
    }

    @NonNull
    public PathType resolveSibling(@NonNull ByteString other) {
        Objects.requireNonNull(other);
        return resolveSibling(createPath(other));
    }

    @NonNull
    @Override
    public PathType relativize(@NonNull Path other) {
        Objects.requireNonNull(other);
        PathType otherPath = requireSameClassPath(other);
        ByteStringListPath<PathType> otherByteStringPath = otherPath;
        if (otherByteStringPath.mAbsolute != mAbsolute) {
            throw new IllegalArgumentException("The other path must be as absolute as this path");
        }
        if (isEmpty()) {
            return otherPath;
        }
        if (equals(otherPath)) {
            return createEmptyPath();
        }
        int namesSize = mNames.size();
        int otherNamesSize = otherByteStringPath.mNames.size();
        int lesserNamesSize = Math.min(namesSize, otherNamesSize);
        int commonNamesSize = 0;
        while (commonNamesSize < lesserNamesSize && Objects.equals(mNames.get(commonNamesSize),
                otherByteStringPath.mNames.get(commonNamesSize))) {
            ++commonNamesSize;
        }
        List<ByteString> relativeNames = new ArrayList<>();
        int dotDotCount = namesSize - commonNamesSize;
        if (dotDotCount > 0) {
            relativeNames.addAll(Collections.nCopies(dotDotCount, BYTE_STRING_DOT_DOT));
        }
        if (commonNamesSize < otherNamesSize) {
            relativeNames.addAll(otherByteStringPath.mNames.subList(commonNamesSize,
                    otherNamesSize));
        }
        return createPath(false, relativeNames);
    }

    @NonNull
    private PathType requireSameClassPath(@NonNull Path path) {
        if (path.getClass() != getClass()) {
            throw new ProviderMismatchException(path.toString());
        }
        //noinspection unchecked
        return (PathType) path;
    }

    @NonNull
    @Override
    public URI toUri() {
        String scheme = getFileSystem().provider().getScheme();
        return ByteStringUriUtils.createUri(scheme, getUriSchemeSpecificPart(), getUriFragment());
    }

    @NonNull
    @Override
    public PathType toAbsolutePath() {
        if (mAbsolute) {
            //noinspection unchecked
            return (PathType) this;
        } else {
            return getDefaultDirectory().resolve(this);
        }
    }

    @NonNull
    public ByteString toByteString() {
        // We are okay with the potential race condition here.
        if (mByteStringCache == null) {
            ByteStringBuilder builder = new ByteStringBuilder();
            if (mAbsolute && getRoot() != null) {
                builder.append(mSeparator);
            }
            boolean first = true;
            for (ByteString name : mNames) {
                if (first) {
                    first = false;
                } else {
                    builder.append(mSeparator);
                }
                builder.append(name);
            }
            mByteStringCache = builder.toByteString();
        }
        return mByteStringCache;
    }

    /**
     * @deprecated Use {@link #toByteString()} instead.
     */
    @NonNull
    @Override
    public String toString() {
        return toByteString().toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ByteStringListPath that = (ByteStringListPath) object;
        return mSeparator == that.mSeparator
                && mAbsolute == that.mAbsolute
                && Objects.equals(mNames, that.mNames)
                && Objects.equals(getFileSystem(), that.getFileSystem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(mSeparator, mAbsolute, mNames, getFileSystem());
    }

    @Override
    public int compareTo(@NonNull Path other) {
        Objects.requireNonNull(other);
        ByteStringListPath otherPath = requireByteStringListPath(other);
        return toByteString().compareTo(otherPath.toByteString());
    }

    @NonNull
    public final Iterator<ByteString> byteStringIterator() {
        return new ByteStringNameIterator();
    }

    @NonNull
    private ByteStringListPath requireByteStringListPath(@NonNull Path path) {
        if (!(path instanceof ByteStringListPath)) {
            throw new ProviderMismatchException(path.toString());
        }
        return (ByteStringListPath) path;
    }

    protected boolean isEmpty() {
        return !mAbsolute && mNames.size() == 1 && Objects.equals(mNames.get(0), ByteString.EMPTY);
    }

    protected abstract boolean isPathAbsolute(@NonNull ByteString path);

    protected abstract PathType createPath(@NonNull ByteString path);

    @NonNull
    protected abstract PathType createPath(boolean absolute, @NonNull List<ByteString> names);

    @NonNull
    private PathType createEmptyPath() {
        return createPath(false, Collections.singletonList(ByteString.EMPTY));
    }

    @Nullable
    protected ByteString getUriSchemeSpecificPart() {
        return toAbsolutePath().toByteString();
    }

    @Nullable
    protected ByteString getUriFragment() {
        return null;
    }

    @NonNull
    protected abstract PathType getDefaultDirectory();

    private class ByteStringNameIterator implements Iterator<ByteString> {

        private int mNameIndex = 0;

        @Override
        public boolean hasNext() {
            return mNameIndex < getNameCount();
        }

        @NonNull
        @Override
        public ByteString next() {
            if (mNameIndex >= getNameCount()) {
                throw new NoSuchElementException();
            }
            ByteString name = getByteStringName(mNameIndex);
            ++mNameIndex;
            return name;
        }
    }


    protected ByteStringListPath(Parcel in) {
        mSeparator = in.readByte();
        mAbsolute = in.readByte() != 0;
        mNames = Collections.unmodifiableList(in.createTypedArrayList(ByteString.CREATOR));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(mSeparator);
        dest.writeByte(mAbsolute ? (byte) 1 : (byte) 0);
        dest.writeTypedList(mNames);
    }
}
