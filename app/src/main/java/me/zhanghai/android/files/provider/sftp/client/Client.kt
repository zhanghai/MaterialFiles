/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp.client

import java8.nio.channels.SeekableByteChannel
import me.zhanghai.android.files.provider.common.LocalWatchService
import me.zhanghai.android.files.provider.common.NotifyEntryModifiedSeekableByteChannel
import me.zhanghai.android.files.util.closeSafe
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.sftp.FileMode
import net.schmizz.sshj.sftp.OpenMode
import net.schmizz.sshj.sftp.RemoteFile
import net.schmizz.sshj.sftp.Response
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.sftp.SFTPException
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.UserAuthException
import java.io.IOException
import java.util.Collections
import java.util.WeakHashMap
import java8.nio.file.Path as Java8Path

object Client {
    @Volatile
    lateinit var authenticator: Authenticator

    private val clients = mutableMapOf<Authority, SFTPClient>()

    private val directoryFileAttributesCache =
        Collections.synchronizedMap(WeakHashMap<Path, FileAttributes>())

    @Throws(ClientException::class)
    fun access(path: Path, flags: Set<OpenMode>) {
        val file = open(path, flags, FileAttributes.EMPTY)
        try {
            file.close()
        } catch (e: IOException) {
            throw ClientException(e)
        }
    }

    @Throws(ClientException::class)
    fun lstat(path: Path): FileAttributes {
        val client = getClient(path.authority)
        synchronized(directoryFileAttributesCache) {
            directoryFileAttributesCache[path]?.let {
                return it.also { directoryFileAttributesCache -= path }
            }
        }
        return try {
            client.lstat(path.remotePath)
        } catch (e: IOException) {
            throw ClientException(e)
        }
    }

    @Throws(ClientException::class)
    fun mkdir(path: Path, attributes: FileAttributes) {
        val client = getClient(path.authority)
        try {
            client.sftpEngine.makeDir(path.remotePath, attributes)
        } catch (e: IOException) {
            throw ClientException(e)
        }
        LocalWatchService.onEntryCreated(path as Java8Path)
    }

    @Throws(ClientException::class)
    private fun open(path: Path, flags: Set<OpenMode>, attributes: FileAttributes): RemoteFile {
        val client = getClient(path.authority)
        return try {
            client.open(path.remotePath, flags, attributes)
        } catch (e: IOException) {
            throw ClientException(e)
        }
    }

    @Throws(ClientException::class)
    fun openByteChannel(
        path: Path,
        flags: Set<OpenMode>,
        attributes: FileAttributes
    ): SeekableByteChannel {
        val file = open(path, flags, attributes)
        return NotifyEntryModifiedSeekableByteChannel(
            FileByteChannel(file, flags.contains(OpenMode.APPEND)), path as Java8Path
        )
    }

    @Throws(ClientException::class)
    fun readlink(path: Path): String {
        val client = getClient(path.authority)
        return try {
            client.readlink(path.remotePath)
        } catch (e: IOException) {
            throw ClientException(e)
        }
    }

    @Throws(ClientException::class)
    fun realpath(path: Path): Path {
        val client = getClient(path.authority)
        val realPath = try {
            client.canonicalize(path.remotePath)
        } catch (e: IOException) {
            throw ClientException(e)
        }
        return path.resolve(realPath)
    }

    @Throws(ClientException::class)
    fun remove(path: Path) {
        val attributes = lstat(path)
        val isDirectory = attributes.type == FileMode.Type.DIRECTORY
        if (isDirectory) {
            rmdir(path)
        } else {
            unlink(path)
        }
    }

    // Note that unlike POSIX rename(), this won't overwrite an existing file.
    @Throws(ClientException::class)
    fun rename(path: Path, newPath: Path) {
        if (newPath.authority != path.authority) {
            throw ClientException(
                SFTPException(Response.StatusCode.FAILURE, "Paths aren't on the same authority")
            )
        }
        val client = getClient(path.authority)
        try {
            client.rename(path.remotePath, newPath.remotePath)
        } catch (e: IOException) {
            throw ClientException(e)
        }
        directoryFileAttributesCache -= path
        directoryFileAttributesCache -= newPath
        LocalWatchService.onEntryDeleted(path as Java8Path)
        LocalWatchService.onEntryCreated(newPath as Java8Path)
    }

    @Throws(ClientException::class)
    fun rmdir(path: Path) {
        val client = getClient(path.authority)
        try {
            client.rmdir(path.remotePath)
        } catch (e: IOException) {
            throw ClientException(e)
        }
        directoryFileAttributesCache -= path
        LocalWatchService.onEntryDeleted(path as Java8Path)
    }

    @Throws(ClientException::class)
    fun scandir(path: Path): List<Path> {
        val client = getClient(path.authority)
        val files = try {
            client.ls(path.remotePath)
        } catch (e: IOException) {
            throw ClientException(e)
        }
        return files.map { file ->
            // The attributes here are from lstat().
            // https://github.com/openssh/openssh-portable/blob/71241fc05db4bbb11bb29340b44b92e2575373d8/sftp-server.c#L1110
            path.resolve(file.name).also { directoryFileAttributesCache[it] = file.attributes }
        }
    }

    @Throws(ClientException::class)
    fun setstat(path: Path, attributes: FileAttributes) {
        val client = getClient(path.authority)
        try {
            client.setattr(path.remotePath, attributes)
        } catch (e: IOException) {
            throw ClientException(e)
        }
        directoryFileAttributesCache -= path
        LocalWatchService.onEntryModified(path as Java8Path)
    }

    @Throws(ClientException::class)
    fun stat(path: Path): FileAttributes {
        val client = getClient(path.authority)
        synchronized(directoryFileAttributesCache) {
            directoryFileAttributesCache[path]?.let {
                if (it.type != FileMode.Type.SYMLINK) {
                    return it.also { directoryFileAttributesCache -= path }
                }
            }
        }
        return try {
            client.stat(path.remotePath)
        } catch (e: IOException) {
            throw ClientException(e)
        }
    }

    @Throws(ClientException::class)
    fun symlink(link: Path, target: String) {
        val client = getClient(link.authority)
        try {
            client.symlink(link.remotePath, target)
        } catch (e: IOException) {
            throw ClientException(e)
        }
        LocalWatchService.onEntryCreated(link as Java8Path)
    }

    @Throws(ClientException::class)
    fun unlink(path: Path) {
        val client = getClient(path.authority)
        try {
            client.rm(path.remotePath)
        } catch (e: IOException) {
            throw ClientException(e)
        }
        directoryFileAttributesCache -= path
        LocalWatchService.onEntryDeleted(path as Java8Path)
    }

    @Throws(ClientException::class)
    private fun getClient(authority: Authority): SFTPClient {
        synchronized(clients) {
            var client = clients[authority]
            if (client != null) {
                if (client.sftpEngine.subsystem.isOpen) {
                    return client
                } else {
                    client.closeSafe()
                    clients -= authority
                }
            }
            val authentication = authenticator.getAuthentication(authority)
                ?: throw ClientException("No authentication found for $authority")
            val sshClient = SSHClient().apply { addHostKeyVerifier(PromiscuousVerifier()) }
            try {
                sshClient.connect(authority.host, authority.port)
            } catch (e: IOException) {
                sshClient.closeSafe()
                throw ClientException(e)
            }
            try {
                sshClient.auth(authority.username, authentication.toAuthMethod())
            } catch (e: UserAuthException) {
                sshClient.closeSafe()
                throw ClientException(e)
            } catch (e: TransportException) {
                sshClient.closeSafe()
                throw ClientException(e)
            }
            client = sshClient.newSFTPClient()
            clients[authority] = client
            return client
        }
    }

    interface Path {
        val authority: Authority
        val remotePath: String
        fun resolve(other: String): Path
    }
}
