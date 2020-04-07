/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Binder
import android.os.IInterface
import android.os.RemoteException
import java.io.IOException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
@Throws(IOException::class)
fun <T : IInterface, R> T.call(block: T.(ParcelableException) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val exception = ParcelableException()
    return try {
        block(exception)
    } catch (e: RemoteException) {
        throw RemoteFileSystemException(e)
    }.also { exception.value?.let { throw it } }
}

@OptIn(ExperimentalContracts::class)
fun <T, R> T.tryRun(exception: ParcelableException, block: T.() -> R): R?
    where T : IInterface, T : Binder {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block()
    } catch (e: IOException) {
        exception.value = e
        null
    } catch (e: RuntimeException) {
        exception.value = e
        null
    }
}
