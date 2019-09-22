/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MimeTypeInfo {

    @NonNull
    public final String type;
    @NonNull
    public final String subtype;
    @Nullable
    public final String parameters;

    public MimeTypeInfo(@NonNull String type, @NonNull String subtype,
                        @Nullable String parameters) {
        this.type = type;
        this.subtype = subtype;
        this.parameters = parameters;
    }

    @Nullable
    public static MimeTypeInfo parse(@NonNull String mimeType) {
        int indexOfSlash = mimeType.indexOf('/');
        if (indexOfSlash == -1 || indexOfSlash == 0 || indexOfSlash == mimeType.length() - 1) {
            return null;
        }
        String type = mimeType.substring(0, indexOfSlash);
        String subtype;
        String parameters;
        int indexOfSemicolon = mimeType.indexOf(';', indexOfSlash);
        if (indexOfSemicolon == -1) {
            subtype = mimeType.substring(indexOfSlash + 1);
            parameters = null;
        } else {
            if (indexOfSemicolon == indexOfSlash + 1 || indexOfSemicolon == mimeType.length() - 1) {
                return null;
            }
            subtype = mimeType.substring(indexOfSlash + 1, indexOfSemicolon);
            parameters = mimeType.substring(indexOfSemicolon + 1);
        }
        return new MimeTypeInfo(type, subtype, parameters);
    }

    public boolean matches(@NonNull MimeTypeInfo spec) {
        if (!(Objects.equals(spec.type, "*") || Objects.equals(type, spec.type))) {
            return false;
        }
        if (!(Objects.equals(spec.subtype, "*") || Objects.equals(subtype, spec.subtype))) {
            return false;
        }
        if (!(spec.parameters == null || Objects.equals(parameters, spec.parameters))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        MimeTypeInfo that = (MimeTypeInfo) object;
        return type.equals(that.type)
                && subtype.equals(that.subtype)
                && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, subtype, parameters);
    }

    @NonNull
    @Override
    public String toString() {
        return type + "/" + subtype + (parameters != null ? ";" + parameters : "");
    }
}
