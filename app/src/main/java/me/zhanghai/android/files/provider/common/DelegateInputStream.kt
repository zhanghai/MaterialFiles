package me.zhanghai.android.files.provider.common

import java.io.IOException
import java.io.InputStream

open class DelegateInputStream(private val inputStream: InputStream) : InputStream() {
    @Throws(IOException::class)
    override fun read(): Int = inputStream.read()

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int = inputStream.read(b)

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int = inputStream.read(b, off, len)

    @Throws(IOException::class)
    override fun skip(n: Long): Long = inputStream.skip(n)

    @Throws(IOException::class)
    override fun available(): Int = inputStream.available()

    @Throws(IOException::class)
    override fun close() {
        inputStream.close()
    }

    override fun mark(readlimit: Int) {
        inputStream.mark(readlimit)
    }

    @Throws(IOException::class)
    override fun reset() {
        inputStream.reset()
    }

    override fun markSupported(): Boolean = inputStream.markSupported()
}
