/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.webdav.client

import at.bitfire.dav4jvm.DavCollection
import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.HttpUtils
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.Response
import at.bitfire.dav4jvm.exception.ConflictException
import at.bitfire.dav4jvm.exception.DavException
import at.bitfire.dav4jvm.exception.ForbiddenException
import at.bitfire.dav4jvm.exception.HttpException
import at.bitfire.dav4jvm.exception.NotFoundException
import at.bitfire.dav4jvm.exception.PreconditionFailedException
import at.bitfire.dav4jvm.exception.ServiceUnavailableException
import at.bitfire.dav4jvm.exception.UnauthorizedException
import at.bitfire.dav4jvm.property.webdav.CreationDate
import at.bitfire.dav4jvm.property.webdav.GetContentLength
import at.bitfire.dav4jvm.property.webdav.GetLastModified
import at.bitfire.dav4jvm.property.webdav.ResourceType
import java8.nio.channels.SeekableByteChannel
import me.zhanghai.android.files.app.okHttpClient
import me.zhanghai.android.files.provider.common.LocalWatchService
import me.zhanghai.android.files.provider.common.NotifyEntryModifiedOutputStream
import me.zhanghai.android.files.provider.common.NotifyEntryModifiedSeekableByteChannel
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Route
import org.threeten.bp.Instant
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.util.Collections
import java.util.WeakHashMap
import java8.nio.file.Path as Java8Path
import okhttp3.Response as OkHttpResponse

// See also https://github.com/miquels/webdavfs/blob/master/fuse.go
object Client {
    private val FILE_PROPERTIES = arrayOf(
        ResourceType.NAME,
        CreationDate.NAME,
        GetContentLength.NAME,
        GetLastModified.NAME
    )

    @Volatile
    lateinit var authenticator: Authenticator

    private val clients = mutableMapOf<Authority, OkHttpClient>()

    private val collectionMemberCache = Collections.synchronizedMap(WeakHashMap<Path, Response>())

    @Throws(IOException::class)
    private fun getClient(authority: Authority): OkHttpClient {
        synchronized(clients) {
            var client = clients[authority]
            if (client == null) {
                val authenticatorInterceptor =
                    OkHttpAuthenticatorInterceptor(authenticator, authority)
                client = okHttpClient.newBuilder()
                    // Turn off follow redirects for PROPFIND.
                    .followRedirects(false)
                    .cookieJar(MemoryCookieJar())
                    .addNetworkInterceptor(authenticatorInterceptor)
                    .authenticator(authenticatorInterceptor)
                    .build()
                clients[authority] = client
            }
            return client
        }
    }

    @Throws(DavException::class)
    fun makeCollection(path: Path) {
        try {
            DavResource(getClient(path.authority), path.url).mkCol(null) {}
        } catch (e: IOException) {
            throw e.toDavException()
        }
        LocalWatchService.onEntryCreated(path as Java8Path)
    }

    @Throws(DavException::class)
    fun makeFile(path: Path) {
        try {
            put(path).close()
        } catch (e: IOException) {
            throw e.toDavException()
        }
        LocalWatchService.onEntryCreated(path as Java8Path)
    }

    @Throws(DavException::class)
    fun delete(path: Path) {
        try {
            DavResource(getClient(path.authority), path.url).delete {}
        } catch (e: IOException) {
            throw e.toDavException()
        }
        collectionMemberCache -= path
        LocalWatchService.onEntryDeleted(path as Java8Path)
    }

    @Throws(DavException::class)
    fun move(source: Path, target: Path) {
        if (source.authority != target.authority) {
            throw IOException("Paths aren't on the same authority")
        }
        try {
            DavResource(getClient(source.authority), source.url).move(target.url, false) {}
        } catch (e: IOException) {
            throw e.toDavException()
        }
        collectionMemberCache -= source
        collectionMemberCache -= target
        LocalWatchService.onEntryDeleted(source as Java8Path)
        LocalWatchService.onEntryCreated(target as Java8Path)
    }

    @Throws(DavException::class)
    fun get(path: Path): InputStream =
        try {
            DavResource(getClient(path.authority), path.url).getCompat("*/*", null)
        } catch (e: IOException) {
            throw e.toDavException()
        }

    @Throws(DavException::class)
    fun findCollectionMembers(path: Path): List<Path> =
        buildList {
            try {
                DavCollection(getClient(path.authority), path.url)
                    .propfind(1, *FILE_PROPERTIES) { response, relation ->
                        if (relation != Response.HrefRelation.MEMBER) {
                            return@propfind
                        }
                        this += path.resolve(response.hrefName())
                            .also {
                                if (response.isSuccess()) {
                                    collectionMemberCache[it] = response
                                }
                            }
                    }
            } catch (e: IOException) {
                throw e.toDavException()
            }
        }

    @Throws(DavException::class)
    fun findPropertiesOrNull(path: Path, noFollowLinks: Boolean): Response? =
        try {
            findProperties(path, noFollowLinks)
        } catch (e: NotFoundException) {
            null
        } catch (e: IOException) {
            throw e.toDavException()
        }

    // TODO: Support noFollowLinks.
    @Throws(DavException::class)
    fun findProperties(path: Path, noFollowLinks: Boolean): Response {
        synchronized(collectionMemberCache) {
            collectionMemberCache.remove(path)?.let { return it }
        }
        try {
            return findProperties(
                DavResource(getClient(path.authority), path.url), *FILE_PROPERTIES
            )
        } catch (e: IOException) {
            throw e.toDavException()
        }
    }

    @Throws(DavException::class, IOException::class)
    internal fun findProperties(resource: DavResource, vararg properties: Property.Name): Response {
        var responseRef: Response? = null
        resource.propfind(0, *properties) { response, relation ->
            if (relation != Response.HrefRelation.SELF) {
                return@propfind
            }
            if (responseRef != null) {
                throw DavException("Duplicate response for self")
            }
            responseRef = response
        }
        val response = responseRef ?: throw DavException("Couldn't find a response for self")
        response.checkSuccess()
        return response
    }

    @Throws(DavException::class)
    fun openByteChannel(path: Path, isAppend: Boolean): SeekableByteChannel {
        try {
            val client = getClient(path.authority)
            val resource = DavResource(client, path.url)
            val patchSupport = resource.getPatchSupport()
            return NotifyEntryModifiedSeekableByteChannel(
                FileByteChannel(resource, patchSupport, isAppend), path as Java8Path
            )
        } catch (e: IOException) {
            throw e.toDavException()
        }
    }

    @Throws(DavException::class)
    fun setLastModifiedTime(path: Path, lastModifiedTime: Instant) {
        if (true) {
            return
        }
        // The following doesn't work on most servers. See also
        // https://github.com/sabre-io/dav/issues/1277
        try {
            DavResource(getClient(path.authority), path.url).proppatch(
                mapOf(GetLastModified.NAME to HttpUtils.formatDate(lastModifiedTime)), emptyList()
            ) { response, _ -> response.checkSuccess() }
        } catch (e: IOException) {
            throw e.toDavException()
        }
        LocalWatchService.onEntryModified(path as Java8Path)
    }

    @Throws(DavException::class)
    fun put(path: Path): OutputStream =
        try {
            NotifyEntryModifiedOutputStream(
                DavResource(getClient(path.authority), path.url).putCompat(), path as Java8Path
            )
        } catch (e: IOException) {
            throw e.toDavException()
        }

    // @see DavResource.checkStatus
    private fun Response.checkSuccess() {
        if (isSuccess()) {
            return
        }
        val status = status!!
        throw when (status.code) {
            HttpURLConnection.HTTP_UNAUTHORIZED -> UnauthorizedException(status.message)
            HttpURLConnection.HTTP_FORBIDDEN -> ForbiddenException(status.message)
            HttpURLConnection.HTTP_NOT_FOUND -> NotFoundException(status.message)
            HttpURLConnection.HTTP_CONFLICT -> ConflictException(status.message)
            HttpURLConnection.HTTP_PRECON_FAILED -> PreconditionFailedException(status.message)
            HttpURLConnection.HTTP_UNAVAILABLE -> ServiceUnavailableException(status.message)
            else -> HttpException(status.code, status.message)
        }
    }

    interface Path {
        val authority: Authority
        val url: HttpUrl
        fun resolve(other: String): Path
    }

    private class OkHttpAuthenticatorInterceptor(
        private val authenticator: Authenticator,
        private val authority: Authority
    ) : AuthenticatorInterceptor {
        private var authenticatorInterceptorCache: Pair<Authentication, AuthenticatorInterceptor>? =
            null

        private fun getAuthenticatorInterceptor(): AuthenticatorInterceptor {
            val authentication = authenticator.getAuthentication(authority)
                ?: throw IOException("No authentication found for $authority")
            authenticatorInterceptorCache?.let {
                (cachedAuthentication, cachedAuthenticatorInterceptor) ->
                if (cachedAuthentication == authentication) {
                    return cachedAuthenticatorInterceptor
                }
            }
            return authentication.createAuthenticatorInterceptor(authority).also {
                authenticatorInterceptorCache = authentication to it
            }
        }

        override fun authenticate(route: Route?, response: OkHttpResponse): Request? =
            getAuthenticatorInterceptor().authenticate(route, response)

        override fun intercept(chain: Interceptor.Chain): OkHttpResponse =
            getAuthenticatorInterceptor().intercept(chain)
    }
}
