/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.net.Uri
import android.os.Build
import android.os.Parcel
import androidx.annotation.RequiresApi
import kotlinx.parcelize.Parceler
import me.zhanghai.android.files.hiddenapi.RestrictedHiddenApi
import me.zhanghai.android.files.util.lazyReflectedMethod

// Work around Uri parceling changes in
// https://android.googlesource.com/platform/frameworks/base/+/97f621d81fc51de240ba73bc008d997e0eea7939
// (and the String8 change in API 30).
object UriParcelerCompat : Parceler<Uri?> {
    private const val NULL_TYPE_ID = 0
    private const val STRING_URI_TYPE_ID = 1
    private const val OPAQUE_URI_TYPE_ID = 2
    private const val HIERARCHICAL_URI_TYPE_ID = 3

    private const val REPRESENTATION_BOTH = 0
    private const val REPRESENTATION_ENCODED = 1
    private const val REPRESENTATION_DECODED = 2

    @get:RequiresApi(Build.VERSION_CODES.R)
    @RestrictedHiddenApi
    private val parcelReadString8Method by lazyReflectedMethod(Parcel::class.java, "readString8")

    override fun create(parcel: Parcel): Uri? {
        // Parcel.readParcelableCreator()
        parcel.readString() ?: return null
        // Uri.CREATOR.createFromParcel()
        val uriString = when (val typeId = parcel.readInt()) {
            NULL_TYPE_ID -> return null
            // Uri.StringUri.readFrom()
            STRING_URI_TYPE_ID -> parcel.readUriString()
            OPAQUE_URI_TYPE_ID -> {
                // Uri.OpaqueUri.readFrom()
                val scheme = parcel.readUriString()!!
                // Assume that we never persist a Uri with only a scheme.
                if (scheme.contains(':')) {
                    scheme
                } else {
                    val encodedSsp = readEncodedPart(parcel)
                    val encodedFragment = readEncodedPart(parcel)
                    // Uri.OpaqueUri.toString()
                    buildString {
                        append(scheme)
                        append(':')
                        append(encodedSsp)
                        if (!encodedFragment.isNullOrEmpty()) {
                            append('#')
                            append(encodedFragment)
                        }
                    }
                }
            }
            HIERARCHICAL_URI_TYPE_ID -> {
                // Uri.HierarchicalUri.readFrom()
                // Scheme can be null for HierarchicalUri.
                val scheme = parcel.readUriString() as String?
                // Assume that we never persist a Uri with only a scheme.
                if (scheme != null && scheme.contains(':')) {
                    scheme
                } else {
                    val encodedAuthority = readEncodedPart(parcel)
                    val hasSchemeOrAuthority = !scheme.isNullOrEmpty() ||
                        !encodedAuthority.isNullOrEmpty()
                    val encodedPath = readEncodedPathPart(hasSchemeOrAuthority, parcel)
                    val encodedQuery = readEncodedPart(parcel)
                    val encodedFragment = readEncodedPart(parcel)
                    // Uri.HierarchicalUri.toString()
                    buildString {
                        if (scheme != null) {
                            append(scheme)
                            append(':')
                        }
                        if (encodedAuthority != null) {
                            append("//")
                            append(encodedAuthority)
                        }
                        if (encodedPath != null) {
                            append(encodedPath)
                        }
                        if (!encodedQuery.isNullOrEmpty()) {
                            append('?')
                            append(encodedQuery)
                        }
                        if (!encodedFragment.isNullOrEmpty()) {
                            append('#')
                            append(encodedFragment)
                        }
                    }
                }
            }
            else -> error("Unknown type ID $typeId")
        }
        return Uri.parse(uriString)
    }

    private fun Parcel.readUriString(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            parcelReadString8Method.invoke(this) as String?
        } else {
            readString()
        }

    // Uri.Part.readFrom()
    private fun readEncodedPart(parcel: Parcel): String? =
        when (val representation = parcel.readInt()) {
            REPRESENTATION_BOTH -> parcel.readUriString().also { parcel.readUriString() }
            REPRESENTATION_ENCODED -> parcel.readUriString()
            REPRESENTATION_DECODED -> Uri.encode(parcel.readUriString())
            else -> error("Unknown representation $representation")
        }

    // Uri.PathPart.readFrom()
    private fun readEncodedPathPart(hasSchemeOrAuthority: Boolean, parcel: Parcel): String? {
        val encodedPathPart = when (val representation = parcel.readInt()) {
            REPRESENTATION_BOTH -> parcel.readUriString().also { parcel.readUriString() }
            REPRESENTATION_ENCODED -> parcel.readUriString()
            REPRESENTATION_DECODED -> Uri.encode(parcel.readUriString(), "/")
            else -> error("Unknown representation $representation")
        }
        return if (hasSchemeOrAuthority) {
            makeEncodedPathPartAbsolute(encodedPathPart)
        } else {
            encodedPathPart
        }
    }

    // Uri.PathPart.makeAbsolute()
    private fun makeEncodedPathPartAbsolute(encodedPathPart: String?): String? =
        if (encodedPathPart.isNullOrEmpty() || encodedPathPart.startsWith("/")) {
            encodedPathPart
        } else {
            "/${encodedPathPart}"
        }

    override fun Uri?.write(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(this, flags)
    }
}
