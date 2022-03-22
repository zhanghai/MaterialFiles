package me.zhanghai.android.files.provider.ftp.client

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPCmd
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.io.IOException
import java.util.Calendar

private val DUMMY_ROOT_FTP_FILE = FTPFile().apply {
    rawListing = "Type=dir;Size=4096;Modify=19700101000000;Perm=cdeflmp; /"
    type = FTPFile.DIRECTORY_TYPE
    size = 4096
    timestamp = Calendar.getInstance().apply { timeInMillis = 0 }
    setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, true)
    setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true)
    setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, true)
    name = "/"
}

@Throws(IOException::class)
fun FTPClient.mlistFileCompat(pathname: String): FTPFile? {
    if (hasFeature(FTPCmd.MLST)) {
        return mlistFile(pathname)
    } else {
        val path = File(pathname)
        val parent = path.parent ?: return DUMMY_ROOT_FTP_FILE
        return listFiles(parent)?.firstOrNull { it != null && it.name == path.name }
    }
}

@Throws(IOException::class)
fun FTPClient.mlistDirCompat(pathname: String): Array<FTPFile>? =
    // Note that there is no distinct FEAT output for MLSD. The presence of the MLST feature
    // indicates that both MLST and MLSD are supported.
    // @see https://datatracker.ietf.org/doc/html/rfc3659#section-7.8
    // FTPClient silently returns an empty array even when server returns an error for unknown
    // command, so we have to rely on checking the feature.
    if (hasFeature(FTPCmd.MLST)) mlistDir(pathname) else listFiles(pathname)

@Throws(IOException::class)
fun FTPClient.setModificationTimeCompat(pathname: String, timeval: String): Boolean =
    // @see https://www.ietf.org/archive/id/draft-somers-ftp-mfxx-04.txt
    // This is frequently called during file operations, so in order to avoid wasting network
    // requests, we check the feature first which is cached locally.
    if (hasFeature(FTPCmd.MFMT)) {
        setModificationTime(pathname, timeval)
    } else {
        throw IOException("Missing feature ${FTPCmd.MFMT.command}")
    }
