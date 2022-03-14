/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.ftp.client

import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.provider.common.DelegateInputStream
import me.zhanghai.android.files.provider.common.DelegateOutputStream
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPClientConfig
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.net.ftp.FTPSClient
import org.threeten.bp.ZoneOffset
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Collections
import java.util.Locale
import java.util.WeakHashMap

object Client {
    private val LAST_MODIFIED_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT)
            .withChronology(IsoChronology.INSTANCE)
            .withZone(ZoneOffset.UTC)

    @Volatile
    lateinit var authenticator: Authenticator

    private val clientPool = mutableMapOf<Authority, MutableList<FTPClient>>()

    private val directoryFilesCache = Collections.synchronizedMap(WeakHashMap<Path, FTPFile>())

    @Throws(IOException::class)
    private fun acquireClient(authority: Authority): FTPClient {
        while (true) {
            val client = acquireClientUnchecked(authority) ?: break
            if (!client.isConnected) {
                client.disconnect()
                continue
            }
            val isAlive = try {
                client.sendNoOp()
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
            if (!isAlive) {
                closeClient(client)
                continue
            }
            return client
        }
        return createClient(authority)
    }

    private fun acquireClientUnchecked(authority: Authority): FTPClient? =
        synchronized(clientPool) {
            val pooledClients = clientPool[authority] ?: return null
            pooledClients.removeLastOrNull().also {
                if (pooledClients.isEmpty()) {
                    clientPool -= authority
                }
            }
        }

    @Throws(IOException::class)
    private fun createClient(authority: Authority): FTPClient {
        val password = authenticator.getPassword(authority)
            ?: throw IOException("No password found for $authority")
        return authority.protocol.createClient().apply {
            configure(FTPClientConfig(""))
            listHiddenFiles = true
            connect(authority.host, authority.port)
            try {
                if (!FTPReply.isPositiveCompletion(replyCode)) {
                    throwNegativeReplyCodeException()
                }
                // This has to be called after connect() despite being entirely local.
                enterLocalPassiveMode()
                if (this is FTPSClient) {
                    // @see https://datatracker.ietf.org/doc/html/rfc4217#section-9
                    execPBSZ(0)
                    execPROT("P")
                }
                if (!setFileType(FTPClient.BINARY_FILE_TYPE)) {
                    throwNegativeReplyCodeException()
                }
                if (!login(authority.username, password)) {
                    throwNegativeReplyCodeException()
                }
            } catch (t: Throwable) {
                disconnect()
                throw t
            }
        }
    }

    private fun releaseClient(authority: Authority, client: FTPClient) {
        if (!client.isConnected) {
            client.disconnect()
            return
        }
        // FIXME: Disconnect clients based on time.
        if (false) {
            closeClient(client)
            return
        }
        synchronized(clientPool) {
            clientPool.getOrPut(authority) { mutableListOf() } += client
        }
    }

    private fun closeClient(client: FTPClient) {
        try {
            client.logout()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        client.disconnect()
    }

    private inline fun <R> useClient(authority: Authority, block: (FTPClient) -> R): R {
        val client = acquireClient(authority)
        try {
            return block(client)
        } finally {
            releaseClient(authority, client)
        }
    }

    @Throws(IOException::class)
    fun createDirectory(path: Path) {
        useClient(path.authority) { client ->
            if (!client.makeDirectory(path.remotePath)) {
                client.throwNegativeReplyCodeException()
            }
        }
    }

    @Throws(IOException::class)
    fun createFile(path: Path) {
        storeFile(path).close()
    }

    @Throws(IOException::class)
    fun delete(path: Path) {
        val file = listFile(path, true)
        delete(path, file.isDirectory)
    }

    @Throws(IOException::class)
    fun delete(path: Path, isDirectory: Boolean) {
        if (isDirectory) {
            deleteDirectory(path)
        } else {
            deleteFile(path)
        }
    }

    @Throws(IOException::class)
    fun deleteFile(path: Path) {
        useClient(path.authority) { client ->
            if (!client.deleteFile(path.remotePath)) {
                client.throwNegativeReplyCodeException()
            }
        }
        directoryFilesCache -= path
    }

    @Throws(IOException::class)
    fun deleteDirectory(path: Path) {
        useClient(path.authority) { client ->
            if (!client.removeDirectory(path.remotePath)) {
                client.throwNegativeReplyCodeException()
            }
        }
        directoryFilesCache -= path
    }

    @Throws(IOException::class)
    fun renameFile(source: Path, target: Path) {
        if (source.authority != target.authority) {
            throw IOException("Paths aren't on the same authority")
        }
        useClient(source.authority) { client ->
            if (!client.rename(source.remotePath, target.remotePath)) {
                client.throwNegativeReplyCodeException()
            }
        }
        directoryFilesCache -= source
        directoryFilesCache -= target
    }

    @Throws(IOException::class)
    fun retrieveFile(path: Path): InputStream {
        val authority = path.authority
        val client = acquireClient(authority)
        val inputStream = try {
            client.retrieveFileStream(path.remotePath) ?: client.throwNegativeReplyCodeException()
        } catch (t: Throwable) {
            releaseClient(authority, client)
            throw t
        }
        return CompletePendingCommandInputStream(inputStream, authority, client)
    }

    @Throws(IOException::class)
    fun listDirectory(path: Path): List<Path> {
        useClient(path.authority) { client ->
            val files = client.mlistDir(path.remotePath) ?: client.throwNegativeReplyCodeException()
            return files.map { file ->
                path.resolve(file.name).also { directoryFilesCache[it] = file }
            }
        }
    }

    @Throws(IOException::class)
    fun listFileOrNull(path: Path, noFollowLinks: Boolean): FTPFile? =
        try {
            listFile(path, noFollowLinks)
        } catch (e: NegativeReplyCodeException) {
            null
        }

    @Throws(IOException::class)
    fun listFile(path: Path, noFollowLinks: Boolean): FTPFile {
        val file = listFileNoFollowLinks(path, noFollowLinks)
        if (!file.isSymbolicLink || noFollowLinks) {
            return file
        }
        val targetString = file.link ?: throw IOException("FTPFile.getLink() returned null: $file")
        val target = path.resolve(targetString)
        return listFileNoFollowLinks(target, false)
    }

    @Throws(IOException::class)
    private fun listFileNoFollowLinks(path: Path, preserveCacheForSymbolicLink: Boolean): FTPFile {
        synchronized(directoryFilesCache) {
            directoryFilesCache[path]?.let {
                if (!(it.isSymbolicLink && preserveCacheForSymbolicLink)) {
                    directoryFilesCache -= path
                }
                return it
            }
        }
        useClient(path.authority) { client ->
            return client.mlistFile(path.remotePath) ?: client.throwNegativeReplyCodeException()
        }
    }

    @Throws(IOException::class)
    fun setLastModifiedTime(path: Path, lastModifiedTime: FileTime) {
        val lastModifiedTimeString =
            LAST_MODIFIED_TIME_FORMATTER.format(lastModifiedTime.toInstant())
        useClient(path.authority) { client ->
            if (!client.setModificationTime(path.remotePath, lastModifiedTimeString)) {
                client.throwNegativeReplyCodeException()
            }
        }
    }

    @Throws(IOException::class)
    fun storeFile(path: Path): OutputStream {
        val authority = path.authority
        val client = acquireClient(authority)
        val outputStream = try {
            client.storeFileStream(path.remotePath) ?: client.throwNegativeReplyCodeException()
        } catch (t: Throwable) {
            releaseClient(authority, client)
            throw t
        }
        return CompletePendingCommandOutputStream(outputStream, authority, client)
    }

    interface Path {
        val authority: Authority
        val remotePath: String
        fun resolve(other: String): Path
    }

    private class CompletePendingCommandInputStream(
        inputStream: InputStream,
        private val authority: Authority,
        private val client: FTPClient
    ) : DelegateInputStream(inputStream) {
        @Throws(IOException::class)
        override fun close() {
            try {
                super.close()
                if (!client.completePendingCommand()) {
                    client.throwNegativeReplyCodeException()
                }
            } finally {
                releaseClient(authority, client)
            }
        }
    }

    private class CompletePendingCommandOutputStream(
        outputStream: OutputStream,
        private val authority: Authority,
        private val client: FTPClient
    ) : DelegateOutputStream(outputStream) {
        @Throws(IOException::class)
        override fun close() {
            try {
                super.close()
                if (!client.completePendingCommand()) {
                    client.throwNegativeReplyCodeException()
                }
            } finally {
                releaseClient(authority, client)
            }
        }
    }
}
