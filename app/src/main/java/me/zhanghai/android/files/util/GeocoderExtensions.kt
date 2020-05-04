/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.NullPointerException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

val isGeocoderPresent by lazy { Geocoder.isPresent() }

@Throws(IOException::class)
suspend fun Geocoder.awaitGetFromLocation(
    latitude :Double,
    longitude: Double,
    maxResults: Int
): List<Address> =
    withContext(Dispatchers.IO) {
        // TODO: kotlinc: Type inference failed: Not enough information to infer parameter T in
        //  suspend inline fun <T> suspendCoroutine(crossinline block: (Continuation<T>) -> Unit): T
        //  Please specify it explicitly.
        suspendCoroutine<List<Address>> { continuation ->
            val addresses = try {
                getFromLocation(latitude, longitude, maxResults)
                    ?: throw IOException(NullPointerException())
            } catch (t: Throwable) {
                continuation.resumeWithException(t)
                return@suspendCoroutine
            }
            continuation.resume(addresses)
        }
    }
