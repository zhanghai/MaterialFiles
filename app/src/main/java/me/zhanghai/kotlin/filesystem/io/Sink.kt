package me.zhanghai.kotlin.filesystem.io

import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

public interface Sink : AsyncCloseable, AsyncFlushable {
    @Throws(CancellationException::class, IOException::class)
    public suspend fun write(source: Buffer, byteCount: Long)
}

internal fun Sink.withCloseable(closeable: AsyncCloseable): Sink = CloseableSink(this, closeable)

private class CloseableSink(private val sink: Sink, private val closeable: AsyncCloseable) : Sink {
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
