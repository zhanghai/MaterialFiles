package me.zhanghai.kotlin.filesystem.io

import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

public interface AsyncSource : AsyncCloseable {
    @Throws(CancellationException::class, IOException::class)
    public suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long
}

internal fun AsyncSource.withCloseable(closeable: AsyncCloseable): AsyncSource =
    CloseableAsyncSource(this, closeable)

private class CloseableAsyncSource(
    private val source: AsyncSource,
    private val closeable: AsyncCloseable
) : AsyncSource {
    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long =
        source.readAtMostTo(sink, byteCount)

    override suspend fun close() {
        source.close()
        closeable.close()
    }
}
