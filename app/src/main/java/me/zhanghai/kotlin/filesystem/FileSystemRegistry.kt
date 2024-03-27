package me.zhanghai.kotlin.filesystem

// TODO: Make thread safe
public object FileSystemRegistry {
    private val providers: MutableMap<String, FileSystemProvider> = mutableMapOf()

    private val fileSystems: MutableMap<Uri, FileSystem> = mutableMapOf()

    public fun getProviders(): Map<String, FileSystemProvider> = providers.toMap()

    public fun getProvider(scheme: String): FileSystemProvider? = providers[scheme]

    public fun removeProvider(scheme: String): FileSystemProvider? = providers.remove(scheme)

    public fun getFileSystems(): Map<Uri, FileSystem> = fileSystems.toMap()

    public fun getFileSystem(rootUri: Uri): FileSystem? = fileSystems[rootUri]

    public fun getOrCreateFileSystem(rootUri: Uri): FileSystem {
        fileSystems[rootUri]?.let {
            return it
        }
        val provider = providers[rootUri.scheme]
        requireNotNull(provider) { "No file system provider for scheme \"${rootUri.scheme}\"" }
        return provider.createFileSystem(rootUri)
    }

    public fun removeFileSystem(rootUri: Uri): FileSystem? = fileSystems.remove(rootUri)
}

public expect val FileSystemRegistry.platformFileSystem: PlatformFileSystem
