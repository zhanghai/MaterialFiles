package me.zhanghai.kotlin.filesystem

import kotlinx.io.bytestring.ByteString

public interface PlatformFileSystem : FileSystem {
    public fun getPath(platformPath: ByteString): Path

    public fun toPlatformPath(path: Path): ByteString

    public companion object {
        public const val SCHEME: String = "file"
    }
}
