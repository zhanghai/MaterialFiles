/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileBasicInformation
import com.hierynomus.msfscc.fileinformation.FileIdFullDirectoryInformation
import com.hierynomus.msfscc.fileinformation.FileSettableInformation
import com.hierynomus.msfscc.fileinformation.FileStandardInformation
import com.hierynomus.mssmb2.SMB2CompletionFilter
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2MessageCommandCode
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.mssmb2.messages.SMB2ChangeNotifyResponse
import com.hierynomus.protocol.commons.EnumWithValue
import com.hierynomus.smbj.ProgressListener
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.common.SMBRuntimeException
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.PipeShare
import com.hierynomus.smbj.share.PrinterShare
import com.hierynomus.smbj.share.Share
import com.rapid7.client.dcerpc.mssrvs.ServerService
import com.rapid7.client.dcerpc.transport.SMBTransportFactories
import java8.nio.channels.SeekableByteChannel
import jcifs.context.SingletonContext
import me.zhanghai.android.files.provider.common.CloseableIterator
import me.zhanghai.android.files.provider.common.copyTo
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.provider.common.newOutputStream
import me.zhanghai.android.files.util.closeSafe
import me.zhanghai.android.files.util.enumSetOf
import me.zhanghai.android.files.util.hasBits
import java.io.Closeable
import java.io.IOException
import java.net.Inet4Address
import java.net.UnknownHostException
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.Future

object Client {
    @Volatile
    lateinit var authenticator: Authenticator

    private val client = SMBClient()

    private val sessions = mutableMapOf<Authority, Session>()

    private val directoryFileInformationCache =
        Collections.synchronizedMap(WeakHashMap<Path, FileInformation>())

    @Throws(ClientException::class)
    fun openByteChannel(
        path: Path,
        desiredAccess: Set<AccessMask>,
        fileAttributes: Set<FileAttributes>,
        shareAccess: Set<SMB2ShareAccess>,
        createDisposition: SMB2CreateDisposition,
        createOptions: Set<SMB2CreateOptions>,
        isAppend: Boolean
    ): SeekableByteChannel {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val session = getSession(path.authority)
        val share = getDiskShare(session, sharePath.name)
        val file = try {
            share.openFile(
                sharePath.path, desiredAccess, fileAttributes, shareAccess, createDisposition,
                createOptions
            )
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        return FileByteChannel(file, isAppend)
    }

    @Throws(ClientException::class)
    fun openDirectoryIterator(path: Path): CloseableIterator<Path> {
        val session = getSession(path.authority)
        val sharePath = path.sharePath
        if (sharePath == null) {
            val transport = try {
                SMBTransportFactories.SRVSVC.getTransport(session)
            } catch (e: IOException) {
                throw ClientException(e)
            } catch (e: SMBRuntimeException) {
                throw ClientException(e)
            }
            val serverService = ServerService(transport)
            val netShareInfos = try {
                serverService.shares1
            } catch (e: IOException) {
                throw ClientException(e)
            } catch (e: SMBRuntimeException) {
                throw ClientException(e)
            }
            val sharePaths = netShareInfos.mapNotNull {
                if (!(it.type.hasBits(ShareTypes.STYPE_PRINTQ.value)
                        || it.type.hasBits(ShareTypes.STYPE_DEVICE.value)
                        || it.type.hasBits(ShareTypes.STYPE_IPC.value))) {
                    path.resolve(it.netName)
                } else {
                    null
                }
            }
            return object : CloseableIterator<Path>, Iterator<Path> by sharePaths.iterator() {
                override fun close() {}
            }
        } else {
            val share = getDiskShare(session, sharePath.name)
            val directory = try {
                share.openDirectory(
                    sharePath.path, enumSetOf(
                        AccessMask.FILE_LIST_DIRECTORY, AccessMask.FILE_READ_ATTRIBUTES,
                        AccessMask.FILE_READ_EA
                    ), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null
                )
            } catch (e: SMBRuntimeException) {
                throw ClientException(e)
            }
            val directoryIterator = directory.iterator(FileIdFullDirectoryInformation::class.java)
                .asSequence()
                .filter { fileInformation ->
                    !fileInformation.fileName.let { it == "." || it == ".." }
                }
                .map { fileInformation ->
                    path.resolve(fileInformation.fileName).also {
                        directoryFileInformationCache[it] = fileInformation.toFileInformation()
                    }
                }
                .iterator()
            return object : CloseableIterator<Path>, Iterator<Path> by directoryIterator,
                Closeable by directory {}
        }
    }

    // @see https://gitlab.com/samba-team/devel/samba/-/blob/master/source3/libsmb/cli_smb2_fnum.c
    // cli_smb2_mkdir_send
    @Throws(ClientException::class)
    fun createDirectory(path: Path, fileAttributes: Set<FileAttributes>? = null) {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val session = getSession(path.authority)
        val share = getDiskShare(session, sharePath.name)
        val directory = try {
            share.openDirectory(
                sharePath.path,
                enumSetOf(AccessMask.FILE_READ_ATTRIBUTES, AccessMask.FILE_READ_EA),
                enumSetOf(FileAttributes.FILE_ATTRIBUTE_DIRECTORY)
                    .apply { fileAttributes?.let { addAll(it) } }, SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_CREATE,
                enumSetOf(SMB2CreateOptions.FILE_OPEN_REPARSE_POINT)
            )
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        try {
            directory.close()
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
    }

    // @see https://gitlab.com/samba-team/devel/samba/-/blob/master/source3/libsmb/clisymlink.c
    //      cli_symlink_send
    @Throws(ClientException::class)
    fun createSymbolicLink(
        path: Path,
        reparseData: SymbolicLinkReparseData,
        fileAttributes: Set<FileAttributes>? = null
    ) {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val session = getSession(path.authority)
        val share = getDiskShare(session, sharePath.name)
        val diskEntry = try {
            share.open(
                sharePath.path, enumSetOf(
                    AccessMask.FILE_READ_ATTRIBUTES, AccessMask.FILE_WRITE_ATTRIBUTES,
                    AccessMask.FILE_READ_EA, AccessMask.FILE_WRITE_EA, AccessMask.DELETE,
                    AccessMask.SYNCHRONIZE
                ), enumSetOf<FileAttributes>().apply {
                    fileAttributes?.let { addAll(it) }
                    this -= FileAttributes.FILE_ATTRIBUTE_REPARSE_POINT
                    if (isEmpty()) {
                        this += FileAttributes.FILE_ATTRIBUTE_NORMAL
                    }
                }, null, SMB2CreateDisposition.FILE_CREATE, enumSetOf(
                    SMB2CreateOptions.FILE_NON_DIRECTORY_FILE,
                    SMB2CreateOptions.FILE_OPEN_REPARSE_POINT
                )
            )
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        try {
            diskEntry.use {
                var successful = false
                try {
                    it.setSymbolicLinkReparseData(reparseData)
                    successful = true
                } finally {
                    if (!successful) {
                        try {
                            it.deleteOnClose()
                        } catch (e: SMBRuntimeException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
    }

    // @see https://gitlab.com/samba-team/devel/samba/-/blob/master/source3/libsmb/clifile.c
    //      cli_smb2_hardlink_send
    @Throws(ClientException::class)
    fun createLink(path: Path, link: Path, openReparsePoint: Boolean) {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val linkSharePath = link.sharePath
            ?: throw ClientException("$link does not have a share path")
        if (link.authority != path.authority || linkSharePath.name != sharePath.name) {
            throw ClientException(
                SMBApiException(
                    NtStatus.STATUS_NOT_SAME_DEVICE.value, SMB2MessageCommandCode.SMB2_SET_INFO,
                    null
                )
            )
        }
        val session = getSession(path.authority)
        val share = getDiskShare(session, sharePath.name)
        val diskEntry = try {
            share.open(
                sharePath.path,
                enumSetOf(AccessMask.FILE_WRITE_ATTRIBUTES, AccessMask.FILE_WRITE_EA),
                null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN,
                // CreateHardLink doesn't work for directories.
                enumSetOf(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE).apply {
                    if (openReparsePoint) {
                        this += SMB2CreateOptions.FILE_OPEN_REPARSE_POINT
                    }
                }
            )
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        try {
            diskEntry.use { it.createHardlink(linkSharePath.path, false) }
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
    }

    @Throws(ClientException::class)
    fun delete(path: Path) {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val session = getSession(path.authority)
        val share = getDiskShare(session, sharePath.name)
        val diskEntry = try {
            share.open(
                sharePath.path, enumSetOf(AccessMask.DELETE), null, SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN, enumSetOf(
                    SMB2CreateOptions.FILE_DELETE_ON_CLOSE,
                    SMB2CreateOptions.FILE_OPEN_REPARSE_POINT
                )
            )
        } catch (e: SMBRuntimeException) {
            if (e is SMBApiException && e.status == NtStatus.STATUS_DELETE_PENDING) {
                return
            }
            throw ClientException(e)
        }
        try {
            diskEntry.close()
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        directoryFileInformationCache -= path
    }

    // @see https://gitlab.com/samba-team/devel/samba/-/blob/master/source3/libsmb/clisymlink.c
    //      cli_readlink_send
    @Throws(ClientException::class)
    fun readSymbolicLink(path: Path): SymbolicLinkReparseData {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val session = getSession(path.authority)
        val share = getDiskShare(session, sharePath.name)
        val diskEntry = try {
            share.open(
                sharePath.path,
                enumSetOf(AccessMask.FILE_READ_ATTRIBUTES, AccessMask.FILE_READ_EA), null,
                SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN,
                enumSetOf(SMB2CreateOptions.FILE_OPEN_REPARSE_POINT)
            )
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        return try {
            diskEntry.use { it.getSymbolicLinkReparseData() }
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
    }

    // @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-smb2/cd0162e4-7650-4293-8a2a-d696923203ef
    @Throws(ClientException::class)
    fun copyFile(
        source: Path,
        target: Path,
        copyAttributes: Boolean,
        openReparsePoint: Boolean,
        intervalMillis: Long,
        listener: ((Long) -> Unit)?
    ) {
        val sourceSharePath = source.sharePath
            ?: throw ClientException("$source does not have a share path")
        val targetSharePath = target.sharePath
            ?: throw ClientException("$target does not have a share path")
        val sourceSession = getSession(source.authority)
        val sourceShare = getDiskShare(sourceSession, sourceSharePath.name)
        val targetSession = getSession(target.authority)
        val targetShare = getDiskShare(targetSession, targetSharePath.name)
        val sourceFile = try {
            sourceShare.openFile(
                sourceSharePath.path, enumSetOf(
                    AccessMask.FILE_READ_DATA, AccessMask.FILE_READ_ATTRIBUTES,
                    AccessMask.FILE_READ_EA
                ), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN,
                if (openReparsePoint) {
                    enumSetOf(SMB2CreateOptions.FILE_OPEN_REPARSE_POINT)
                } else {
                    null
                }
            )
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        try {
            sourceFile.use {
                val attributesToCopy = if (copyAttributes) {
                    val sourceAttributes = try {
                        sourceFile.getFileInformation(FileBasicInformation::class.java)
                    } catch (e: SMBRuntimeException) {
                        throw ClientException(e)
                    }.fileAttributes
                    EnumWithValue.EnumUtils.toEnumSet(sourceAttributes, FileAttributes::class.java)
                } else {
                    enumSetOf(FileAttributes.FILE_ATTRIBUTE_NORMAL)
                }
                val targetFile = try {
                    targetShare.openFile(
                        targetSharePath.path, enumSetOf(
                        AccessMask.FILE_WRITE_DATA, AccessMask.FILE_WRITE_ATTRIBUTES,
                        AccessMask.FILE_WRITE_EA, AccessMask.DELETE
                    ), attributesToCopy, SMB2ShareAccess.ALL,
                        SMB2CreateDisposition.FILE_CREATE,
                        enumSetOf(SMB2CreateOptions.FILE_OPEN_REPARSE_POINT)
                    )
                } catch (e: SMBRuntimeException) {
                    throw ClientException(e)
                }
                targetFile.use {
                    var successful = false
                    try {
                        if (sourceSession == targetSession) {
                            val length = try {
                                sourceFile.getFileInformation(FileStandardInformation::class.java)
                            } catch (e: SMBRuntimeException) {
                                throw ClientException(e)
                            }.endOfFile
                            val progressListener = listener?.let {
                                var lastCopiedSize = 0L
                                ProgressListener { copiedSize, _ ->
                                    it(copiedSize - lastCopiedSize)
                                    lastCopiedSize = copiedSize
                                }
                            }
                            try {
                                sourceFile.serverCopy(0, targetFile, 0, length, progressListener)
                            } catch (e: SMBRuntimeException) {
                                throw ClientException(e)
                            }
                        } else {
                            val sourceInputStream = FileByteChannel(sourceFile, false)
                                .newInputStream()
                            val targetOutputStream = FileByteChannel(targetFile, false)
                                .newOutputStream()
                            sourceInputStream.copyTo(targetOutputStream, intervalMillis, listener)
                        }
                        successful = true
                    } finally {
                        if (!successful) {
                            try {
                                targetFile.deleteOnClose()
                            } catch (e: SMBRuntimeException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
    }

    // @see https://gitlab.com/samba-team/devel/samba/-/blob/master/source3/libsmb/cli_smb2_fnum.c
    //      cli_smb2_rename
    @Throws(ClientException::class)
    fun rename(path: Path, newPath: Path) {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val newSharePath = newPath.sharePath
            ?: throw ClientException("$newPath does not have a share path")
        if (newPath.authority != path.authority || newSharePath.name != sharePath.name) {
            throw ClientException(
                SMBApiException(
                    NtStatus.STATUS_NOT_SAME_DEVICE.value, SMB2MessageCommandCode.SMB2_SET_INFO,
                    null
                )
            )
        }
        val session = getSession(path.authority)
        val share = getDiskShare(session, sharePath.name)
        val diskEntry = try {
            share.open(
                sharePath.path, enumSetOf(AccessMask.DELETE), null, SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                enumSetOf(SMB2CreateOptions.FILE_OPEN_REPARSE_POINT)
            )
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        try {
            diskEntry.use { it.rename(newSharePath.path, true) }
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        directoryFileInformationCache -= path
        directoryFileInformationCache -= newPath
    }

    @Throws(ClientException::class)
    fun getPathInformation(path: Path, openReparsePoint: Boolean): PathInformation {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val session = getSession(path.authority)
        if (sharePath.path.isEmpty()) {
            return when (val share = getShare(session, sharePath.name)) {
                is DiskShare -> {
                    val shareInfo = try {
                        share.shareInformation
                    } catch (e: SMBRuntimeException) {
                        e.printStackTrace()
                        null
                    }
                    ShareInformation(ShareType.DISK, shareInfo)
                    // Don't close the disk share, because it might still be in use, or might become
                    // in use shortly. All shares are automatically closed when the session is
                    // closed anyway.
                }
                is PipeShare -> ShareInformation(ShareType.PIPE, null).also { share.closeSafe() }
                is PrinterShare -> ShareInformation(ShareType.PRINTER, null)
                    .also { share.closeSafe() }
                else -> throw AssertionError(share)
            }
        } else {
            synchronized(directoryFileInformationCache) {
                directoryFileInformationCache[path]?.let {
                    if (openReparsePoint || !it.fileAttributes.hasBits(
                            FileAttributes.FILE_ATTRIBUTE_REPARSE_POINT.value
                        )) {
                        return it.also { directoryFileInformationCache -= path }
                    }
                }
            }
            val share = getDiskShare(session, sharePath.name)
            val diskEntry = try {
                share.open(
                    sharePath.path,
                    enumSetOf(AccessMask.FILE_READ_ATTRIBUTES, AccessMask.FILE_READ_EA),
                    null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN,
                    if (openReparsePoint) {
                        enumSetOf(SMB2CreateOptions.FILE_OPEN_REPARSE_POINT)
                    } else {
                        null
                    }
                )
            } catch (e: SMBRuntimeException) {
                throw ClientException(e)
            }
            val fileAllInformation = try {
                diskEntry.use { it.fileInformation }
            } catch (e: SMBRuntimeException) {
                throw ClientException(e)
            }
            return fileAllInformation.toFileInformation()
        }
    }

    @Throws(ClientException::class)
    fun setFileInformation(
        path: Path,
        openReparsePoint: Boolean,
        fileInformation: FileSettableInformation
    ) {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val session = getSession(path.authority)
        val share = getDiskShare(session, sharePath.name)
        val diskEntry = try {
            share.open(
                sharePath.path,
                enumSetOf(AccessMask.FILE_WRITE_ATTRIBUTES, AccessMask.FILE_WRITE_EA),
                null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN,
                if (openReparsePoint) {
                    enumSetOf(SMB2CreateOptions.FILE_OPEN_REPARSE_POINT)
                } else {
                    null
                }
            )
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        try {
            diskEntry.use { it.setFileInformation(fileInformation) }
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        directoryFileInformationCache -= path
    }

    @Throws(ClientException::class)
    fun checkAccess(path: Path, desiredAccess: Set<AccessMask>, openReparsePoint: Boolean) {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val session = getSession(path.authority)
        val share = getDiskShare(session, sharePath.name)
        val diskEntry = try {
            share.open(
                sharePath.path, desiredAccess, null, SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN, if (openReparsePoint) {
                    enumSetOf(SMB2CreateOptions.FILE_OPEN_REPARSE_POINT)
                } else {
                    null
                }
            )
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
        try {
            diskEntry.close()
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
    }

    // @see https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-smb2/05869c32-39f0-4726-afc9-671b76ae5ca7
    @Throws(ClientException::class)
    fun openDirectoryForChangeNotification(path: Path): Directory {
        val sharePath = path.sharePath ?: throw ClientException("$path does not have a share path")
        val session = getSession(path.authority)
        val share = getDiskShare(session, sharePath.name)
        return try {
            share.openDirectory(
                sharePath.path, enumSetOf(AccessMask.FILE_LIST_DIRECTORY), null,
                SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null
            )
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
    }

    @Throws(ClientException::class)
    fun requestDirectoryChangeNotification(
        directory: Directory,
        completionFilter: Set<SMB2CompletionFilter>
    ): Future<SMB2ChangeNotifyResponse> {
        return try {
            directory.watchAsync(completionFilter, false)
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
    }

    @Throws(ClientException::class)
    private fun getSession(authority: Authority): Session {
        synchronized(sessions) {
            var session = sessions[authority]
            if (session != null) {
                val connection = session.connection
                if (connection.isConnected) {
                    return session
                } else {
                    session.closeSafe()
                    connection.closeSafe()
                    sessions -= authority
                }
            }
            val password = authenticator.getPassword(authority)
                ?: throw ClientException("No password found for $authority")
            val hostAddress = resolveHostName(authority.host)
            val connection = try {
                client.connect(hostAddress, authority.port)
            } catch (e: IOException) {
                throw ClientException(e)
            }
            val authenticationContext =
                AuthenticationContext(authority.username, password.toCharArray(), authority.domain)
            session = try {
                connection.authenticate(authenticationContext)
            } catch (e: SMBRuntimeException) {
                // We need to close the connection here, otherwise future authentications reusing it
                // will receive an exception about no available credits.
                connection.closeSafe()
                throw ClientException(e)
            // TODO: kotlinc: Type mismatch: inferred type is Session? but TypeVariable(V) was
            //  expected
            //}
            }!!
            sessions[authority] = session
            return session
        }
    }

    @Throws(ClientException::class)
    private fun resolveHostName(hostName: String): String {
        val nameServiceClient = SingletonContext.getInstance().nameServiceClient
        val addresses = try {
            nameServiceClient.getAllByName(hostName, false).mapNotNull { it.toInetAddress() }
        } catch (e: UnknownHostException) {
            throw ClientException(e)
        }
        val address = addresses.firstOrNull { it is Inet4Address } ?: addresses.first()
        return address.hostAddress!!
    }

    @Throws(ClientException::class)
    private fun getShare(session: Session, shareName: String): Share {
        return try {
            session.connectShare(shareName)
        } catch (e: SMBRuntimeException) {
            throw ClientException(e)
        }
    }

    @Throws(ClientException::class)
    private fun getDiskShare(session: Session, shareName: String): DiskShare =
        getShare(session, shareName) as? DiskShare
            ?: throw ClientException("$shareName is not a DiskShare")

    interface Path {
        val authority: Authority
        val sharePath: SharePath?
        fun resolve(other: String): Path

        data class SharePath(
            val name: String,
            val path: String
        )
    }
}
