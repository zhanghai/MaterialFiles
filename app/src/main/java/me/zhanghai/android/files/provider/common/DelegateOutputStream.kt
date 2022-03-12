package me.zhanghai.android.files.provider.common

import java.io.IOException
import java.io.OutputStream

open class DelegateOutputStream(private val outputStream: OutputStream) : OutputStream() {
    @Throws(IOException::class)
    override fun write(b: Int) {
        outputStream.write(b)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray) {
        outputStream.write(b)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
        outputStream.write(b, off, len)
    }

    @Throws(IOException::class)
    override fun flush() {
        outputStream.flush()
    }

    @Throws(IOException::class)
    override fun close() {
        outputStream.close()
    }
}
