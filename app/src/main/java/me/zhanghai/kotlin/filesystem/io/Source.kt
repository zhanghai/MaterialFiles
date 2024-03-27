package me.zhanghai.kotlin.filesystem.io

import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

public interface Source : AsyncCloseable {
    @Throws(CancellationException::class, IOException::class)
    public suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long
}

internal fun Source.withCloseable(closeable: AsyncCloseable): Source =
    CloseableSource(this, closeable)

private class CloseableSource(private val source: Source, private val closeable: AsyncCloseable) :
    Source {
    override suspend fun readAtMostTo(sink: Buffer, byteCount: Long): Long =
        source.readAtMostTo(sink, byteCount)

    override suspend fun close() {
        source.close()
        closeable.close()
    }
}
