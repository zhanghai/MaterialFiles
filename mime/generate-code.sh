#!/bin/bash
set -e

awk '
BEGIN {
    print "private val extensionToMimeTypeMap = mapOf("
}
{
    print "    \"" $1 "\" to \"" $2 "\","
}
END {
    print ")"
}
' 'android.extensions' \
    >MimeTypeMapCompat.kt
