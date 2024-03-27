package me.zhanghai.kotlin.filesystem.io

import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

public interface AsyncSink : AsyncCloseable, AsyncFlushable {
    @Throws(CancellationException::class, IOException::class)
    public suspend fun write(source: Buffer, byteCount: Long)
}

internal fun AsyncSink.withCloseable(closeable: AsyncCloseable): AsyncSink =
    CloseableAsyncSink(this, closeable)

private class CloseableAsyncSink(
    private val sink: AsyncSink,
    private val closeable: AsyncCloseable
) : AsyncSink {
    override suspend fun write(source: Buffer, byteCount: Long) {
        sink.write(source, byteCount)
    }

    override suspend fun flush() {
        sink.flush()
    }

    override suspend fun close() {
        sink.close()
        closeable.close()
    }
}
