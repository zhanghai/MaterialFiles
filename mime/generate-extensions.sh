#!/bin/bash
set -e

BRANCH=main

debian_mime_types=$(mktemp)
android_mime_types=$(mktemp)
trap "rm ${debian_mime_types} ${android_mime_types}" EXIT

curl "https://android.googlesource.com/platform/external/mime-support/+/${BRANCH}/mime.types?format=TEXT" \
    | base64 -d \
    | sed -nr '/^#/! s/^(\S+)\s+(.+)$/\1 \2/p' \
    >"${debian_mime_types}"

curl "https://android.googlesource.com/platform/frameworks/base/+/${BRANCH}/mime/java-res/android.mime.types?format=TEXT" \
    | base64 -d \
    | sed -nr '/^#/! s/^(\S+)\s+(.+)$/\1 \2/p' \
    >"${android_mime_types}"

# See also https://android.googlesource.com/platform/external/mime-support/+/master/mime.types
# See also https://android.googlesource.com/platform/frameworks/base/+/master/mime/java-res/android.mime.types
# See also https://android.googlesource.com/platform/frameworks/base/+/master/mime/java/android/content/type/DefaultMimeMapFactory.java
# See also https://android.googlesource.com/platform/libcore/+/master/luni/src/main/java/libcore/content/type/MimeMap.java
awk '
BEGIN {
    extensions_size = 0
}
{
    mime_type = $1
    if (mime_type ~ /^?/) {
        mime_type = substr(mime_type, 2)
    }
    for (i = 2; i <= NF; ++i) {
        extension = $i
        if (extension ~ /^?/) {
            extension = substr(extension, 2)
            if (extension in mime_types) {
                continue
            }
        }
        if (!(extension in mime_types)) {
            ++extensions_size
            extensions[extensions_size] = extension
        }
        mime_types[extension] = mime_type
    }
}
END {
    for (i = 1; i <= extensions_size; ++i) {
        extension = extensions[i]
        print extension " " mime_types[extension]
    }
}
' "${debian_mime_types}" "${android_mime_types}" \
    | LC_ALL=C sort \
    >android.extensions
