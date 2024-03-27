package me.zhanghai.kotlin.filesystem.io

import kotlinx.io.IOException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

public interface AsyncCloseable {
    @Throws(CancellationException::class, IOException::class) public suspend fun close()
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun <T : AsyncCloseable?, R> T.use(block: (T) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    var throwable: Throwable? = null
    try {
        return block(this)
    } catch (t: Throwable) {
        throwable = t
        throw t
    } finally {
        // Work around compiler error about smart cast and "captured by a changing closure"
        @Suppress("NAME_SHADOWING") val throwable = throwable
        when {
            this == null -> {}
            throwable == null -> close()
            else ->
                try {
                    close()
                } catch (closeThrowable: Throwable) {
                    throwable.addSuppressed(closeThrowable)
                }
        }
    }
}
