/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone.client

import me.zhanghai.android.files.provider.rclone.daemon.RcloneDaemon
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object RcloneClient {
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private val OCTET_STREAM_MEDIA_TYPE = "application/octet-stream".toMediaType()

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private data class CachedItem(
        val item: RcloneItem,
        val timestamp: Long
    )

    private val itemCache = ConcurrentHashMap<String, CachedItem>()
    private const val CACHE_TTL_MILLIS = 5 * 60 * 1000L // 5 minutes

    private fun cacheKey(remoteName: String, relativePath: String): String =
        "$remoteName:${relativePath.trimStart('/')}"

    fun cacheItem(remoteName: String, relativePath: String, item: RcloneItem) {
        val key = cacheKey(remoteName, relativePath)
        itemCache[key] = CachedItem(item, System.currentTimeMillis())
    }

    fun getCachedItem(remoteName: String, relativePath: String): RcloneItem? {
        val key = cacheKey(remoteName, relativePath)
        val cached = itemCache[key] ?: return null
        if (System.currentTimeMillis() - cached.timestamp > CACHE_TTL_MILLIS) {
            itemCache.remove(key)
            return null
        }
        return cached.item
    }

    fun invalidateCache(remoteName: String? = null, relativePath: String? = null) {
        if (remoteName == null) {
            itemCache.clear()
            return
        }
        if (relativePath == null) {
            val prefix = "$remoteName:"
            itemCache.keys.removeIf { it.startsWith(prefix) }
            return
        }
        val exactKey = cacheKey(remoteName, relativePath)
        itemCache.remove(exactKey)
        val prefix = "$exactKey/"
        itemCache.keys.removeIf { it.startsWith(prefix) }
    }

    private fun getBaseUrl(): String {
        RcloneDaemon.ensureRunning()
        return RcloneDaemon.baseUrl
            ?: throw IOException("Rclone daemon baseUrl is not available")
    }

    private fun callRpc(method: String, json: JSONObject): JSONObject {
        val baseUrl = getBaseUrl()
        val requestBody = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$baseUrl$method")
            .post(requestBody)
            .build()
        val response = httpClient.newCall(request).execute()
        return response.use { resp ->
            val responseBody = resp.body.string()
            if (!resp.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optString("error", resp.message)
                } catch (e: Exception) {
                    resp.message
                }
                throw IOException("Rclone RPC error ($method, status ${resp.code}): $errorMsg")
            }
            if (responseBody.isEmpty()) JSONObject() else JSONObject(responseBody)
        }
    }

    @Throws(IOException::class)
    fun list(remoteName: String, relativePath: String): List<RcloneItem> {
        val cleanPath = relativePath.trimStart('/')
        val json = JSONObject().apply {
            put("fs", "$remoteName:")
            put("remote", cleanPath)
        }
        val result = callRpc("operations/list", json)
        val listArray = result.optJSONArray("list") ?: return emptyList()
        val items = mutableListOf<RcloneItem>()
        for (i in 0 until listArray.length()) {
            val itemObj = listArray.optJSONObject(i) ?: continue
            val item = RcloneItem.fromJson(itemObj)
            items += item
            val itemRelativePath = if (cleanPath.isEmpty()) item.name else "$cleanPath/${item.name}"
            cacheItem(remoteName, itemRelativePath, item)
        }
        return items
    }

    @Throws(IOException::class)
    fun stat(remoteName: String, relativePath: String): RcloneItem? {
        val cleanPath = relativePath.trimStart('/')
        if (cleanPath.isEmpty()) {
            return RcloneItem(
                path = "",
                name = remoteName,
                size = 0L,
                mimeType = "inode/directory",
                modTime = null,
                isDir = true
            )
        }

        getCachedItem(remoteName, cleanPath)?.let {
            return it
        }

        val json = JSONObject().apply {
            put("fs", "$remoteName:")
            put("remote", cleanPath)
        }
        val result = try {
            callRpc("operations/stat", json)
        } catch (e: IOException) {
            if (e.message?.contains("404") == true) {
                return null
            }
            throw e
        }
        if (result.isNull("item")) {
            return null
        }
        val itemObj = result.optJSONObject("item") ?: return null
        val item = RcloneItem.fromJson(itemObj)
        cacheItem(remoteName, cleanPath, item)
        return item
    }

    @Throws(IOException::class)
    fun copyFile(srcRemote: String, srcPath: String, dstRemote: String, dstPath: String) {
        val json = JSONObject().apply {
            put("srcFs", "$srcRemote:")
            put("srcRemote", srcPath.trimStart('/'))
            put("dstFs", "$dstRemote:")
            put("dstRemote", dstPath.trimStart('/'))
        }
        callRpc("operations/copyfile", json)
        invalidateCache(dstRemote, dstPath)
    }

    @Throws(IOException::class)
    fun moveFile(srcRemote: String, srcPath: String, dstRemote: String, dstPath: String) {
        val json = JSONObject().apply {
            put("srcFs", "$srcRemote:")
            put("srcRemote", srcPath.trimStart('/'))
            put("dstFs", "$dstRemote:")
            put("dstRemote", dstPath.trimStart('/'))
        }
        callRpc("operations/movefile", json)
        invalidateCache(srcRemote, srcPath)
        invalidateCache(dstRemote, dstPath)
    }

    @Throws(IOException::class)
    fun deleteFile(remoteName: String, relativePath: String) {
        val cleanPath = relativePath.trimStart('/')
        val json = JSONObject().apply {
            put("fs", "$remoteName:")
            put("remote", cleanPath)
        }
        callRpc("operations/deletefile", json)
        invalidateCache(remoteName, cleanPath)
    }

    @Throws(IOException::class)
    fun mkdir(remoteName: String, relativePath: String) {
        val cleanPath = relativePath.trimStart('/')
        val json = JSONObject().apply {
            put("fs", "$remoteName:")
            put("remote", cleanPath)
        }
        callRpc("operations/mkdir", json)
        invalidateCache(remoteName, cleanPath)
    }

    @Throws(IOException::class)
    fun purge(remoteName: String, relativePath: String) {
        val cleanPath = relativePath.trimStart('/')
        val json = JSONObject().apply {
            put("fs", "$remoteName:")
            put("remote", cleanPath)
        }
        callRpc("operations/purge", json)
        invalidateCache(remoteName, cleanPath)
    }

    @Throws(IOException::class)
    fun uploadFile(remoteName: String, relativePath: String, file: File) {
        val cleanPath = relativePath.trimStart('/')
        val lastSlash = cleanPath.lastIndexOf('/')
        val parentDir = if (lastSlash != -1) cleanPath.substring(0, lastSlash) else ""
        val fileName = if (lastSlash != -1) cleanPath.substring(lastSlash + 1) else cleanPath

        val baseUrl = getBaseUrl()
        val url = baseUrl.toHttpUrl().newBuilder()
            .addPathSegments("operations/uploadfile")
            .addQueryParameter("fs", "$remoteName:")
            .addQueryParameter("remote", parentDir)
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                fileName,
                file.asRequestBody(OCTET_STREAM_MEDIA_TYPE)
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        response.use { resp ->
            if (!resp.isSuccessful) {
                val bodyStr = resp.body.string()
                throw IOException("Failed to upload file ($cleanPath, status ${resp.code}): $bodyStr")
            }
        }
        invalidateCache(remoteName, cleanPath)
        invalidateCache(remoteName, parentDir)
    }

    internal fun buildDownloadUrl(baseUrl: String, remoteName: String, relativePath: String): String {
        val cleanPath = relativePath.trimStart('/')
        val encodedRemote = "%5B" + URLEncoder.encode(remoteName, "UTF-8").replace("+", "%20") + "%3A%5D"
        val encodedPath = if (cleanPath.isEmpty()) "" else {
            cleanPath.split('/').joinToString("/") { segment ->
                URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
            }
        }
        val prefix = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
        return if (encodedPath.isEmpty()) {
            "$prefix$encodedRemote"
        } else {
            "$prefix$encodedRemote/$encodedPath"
        }
    }

    @Throws(IOException::class)
    fun download(remoteName: String, relativePath: String): InputStream {
        val cleanPath = relativePath.trimStart('/')
        val baseUrl = getBaseUrl()
        val url = buildDownloadUrl(baseUrl, remoteName, cleanPath)

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val code = response.code
            response.close()
            throw IOException("Failed to download file ($cleanPath, HTTP status $code)")
        }
        return ResponseBodyInputStream(response)
    }

    @Throws(IOException::class)
    fun downloadRange(remoteName: String, relativePath: String, position: Long, size: Int): InputStream {
        val cleanPath = relativePath.trimStart('/')
        val baseUrl = getBaseUrl()
        val url = buildDownloadUrl(baseUrl, remoteName, cleanPath)

        val endPosition = position + size - 1
        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=$position-$endPosition")
            .get()
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful && response.code != 206) {
            val code = response.code
            response.close()
            throw IOException("Failed to download range ($cleanPath, HTTP status $code)")
        }
        return ResponseBodyInputStream(response)
    }

    @Throws(IOException::class)
    fun listRemotes(): List<String> {
        val result = callRpc("config/listremotes", JSONObject())
        val remotesArray = result.optJSONArray("remotes") ?: return emptyList()
        val remotes = mutableListOf<String>()
        for (i in 0 until remotesArray.length()) {
            val remote = remotesArray.optString(i)
            if (remote.isNotEmpty()) {
                remotes += remote
            }
        }
        return remotes
    }

    private class ResponseBodyInputStream(
        private val response: Response
    ) : InputStream() {
        private val delegate: InputStream = response.body.byteStream()

        override fun read(): Int = delegate.read()

        override fun read(b: ByteArray): Int = delegate.read(b)

        override fun read(b: ByteArray, off: Int, len: Int): Int = delegate.read(b, off, len)

        override fun available(): Int = delegate.available()

        override fun close() {
            try {
                delegate.close()
            } finally {
                response.close()
            }
        }
    }
}
