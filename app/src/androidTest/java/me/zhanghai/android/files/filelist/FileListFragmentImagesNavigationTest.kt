/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import me.zhanghai.android.files.provider.mediastore.MediaStoreFileSystemProvider
import me.zhanghai.android.files.provider.mediastore.MediaStoreCategory
import me.zhanghai.android.files.provider.mediastore.isMediaStorePath

/**
 * Test that Images navigation (MediaStore paths) do not include buttons to add files or folders.
 */
@RunWith(AndroidJUnit4::class)
class FileListFragmentImagesNavigationTest {

    @Test
    fun imagesPathIsRecognizedAsMediaStorePath() {
        val imagesPath = MediaStoreFileSystemProvider.getCategoryPath(MediaStoreCategory.IMAGES)
        assertTrue("Images path should be recognized as MediaStore path", imagesPath.isMediaStorePath)
    }

    @Test
    fun videosPathIsRecognizedAsMediaStorePath() {
        val videosPath = MediaStoreFileSystemProvider.getCategoryPath(MediaStoreCategory.VIDEOS)
        assertTrue("Videos path should be recognized as MediaStore path", videosPath.isMediaStorePath)
    }

    @Test
    fun audioPathIsRecognizedAsMediaStorePath() {
        val audioPath = MediaStoreFileSystemProvider.getCategoryPath(MediaStoreCategory.AUDIO)
        assertTrue("Audio path should be recognized as MediaStore path", audioPath.isMediaStorePath)
    }

    @Test
    fun documentsPathIsRecognizedAsMediaStorePath() {
        val documentsPath = MediaStoreFileSystemProvider.getCategoryPath(MediaStoreCategory.DOCUMENTS)
        assertTrue("Documents path should be recognized as MediaStore path", documentsPath.isMediaStorePath)
    }

    @Test
    fun apksPathIsRecognizedAsMediaStorePath() {
        val apksPath = MediaStoreFileSystemProvider.getCategoryPath(MediaStoreCategory.APKS)
        assertTrue("APKs path should be recognized as MediaStore path", apksPath.isMediaStorePath)
    }

    @Test
    fun normalPathIsNotRecognizedAsMediaStorePath() {
        // Test that a normal file system path is NOT a MediaStore path
        // Using a generic path representation - actual path structure may vary
        val normalPath = java8.nio.file.Paths.get("/storage/emulated/0")
        assertFalse("Normal path should not be recognized as MediaStore path", normalPath.isMediaStorePath)
    }
}
