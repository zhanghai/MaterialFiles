package me.zhanghai.kotlin.filesystem.io

import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

public interface AsyncFlushable {
    @Throws(CancellationException::class, IOException::class) public suspend fun flush()
}
