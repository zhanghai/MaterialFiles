/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Paths

class PreviewableFileDetectorTest {
    @Test
    fun isPreviewable_markdown() {
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("readme.md")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("CHANGELOG.markdown")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("intro.mdown")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("doc.mkd")))
    }

    @Test
    fun isPreviewable_code() {
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("Main.kt")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("App.java")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("index.html")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("style.css")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("app.js")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("main.ts")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("build.gradle")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("settings.gradle.kts")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("config.json")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("config.yaml")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("config.yml")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("pyproject.toml")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("Dockerfile")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("Makefile")))
    }

    @Test
    fun isPreviewable_text() {
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("readme.txt")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("error.log")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get(".gitignore")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get(".gitattributes")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get(".env")))
    }

    @Test
    fun isPreviewable_notPreviewable() {
        assertFalse(PreviewableFileDetector.isPreviewable(Paths.get("image.png")))
        assertFalse(PreviewableFileDetector.isPreviewable(Paths.get("video.mp4")))
        assertFalse(PreviewableFileDetector.isPreviewable(Paths.get("archive.zip")))
        assertFalse(PreviewableFileDetector.isPreviewable(Paths.get("document.pdf")))
        assertFalse(PreviewableFileDetector.isPreviewable(Paths.get("binary.bin")))
        assertFalse(PreviewableFileDetector.isPreviewable(Paths.get("noextension")))
    }

    @Test
    fun getPreviewType_markdown() {
        assertEquals(
            PreviewType.MARKDOWN,
            PreviewableFileDetector.getPreviewType(Paths.get("readme.md"))
        )
        assertEquals(
            PreviewType.MARKDOWN,
            PreviewableFileDetector.getPreviewType(Paths.get("doc.markdown"))
        )
    }

    @Test
    fun getPreviewType_code() {
        assertEquals(
            PreviewType.CODE,
            PreviewableFileDetector.getPreviewType(Paths.get("Main.kt"))
        )
        assertEquals(
            PreviewType.CODE,
            PreviewableFileDetector.getPreviewType(Paths.get("app.js"))
        )
        assertEquals(
            PreviewType.CODE,
            PreviewableFileDetector.getPreviewType(Paths.get("Dockerfile"))
        )
    }

    @Test
    fun getPreviewType_text() {
        assertEquals(
            PreviewType.TEXT,
            PreviewableFileDetector.getPreviewType(Paths.get("readme.txt"))
        )
        assertEquals(
            PreviewType.TEXT,
            PreviewableFileDetector.getPreviewType(Paths.get("error.log"))
        )
    }

    @Test
    fun getHighlightLanguage() {
        assertEquals("kotlin", PreviewableFileDetector.getHighlightLanguage(Paths.get("Main.kt")))
        assertEquals("java", PreviewableFileDetector.getHighlightLanguage(Paths.get("App.java")))
        assertEquals("python", PreviewableFileDetector.getHighlightLanguage(Paths.get("script.py")))
        assertEquals("javascript", PreviewableFileDetector.getHighlightLanguage(Paths.get("app.js")))
        assertEquals("typescript", PreviewableFileDetector.getHighlightLanguage(Paths.get("main.ts")))
        assertEquals("json", PreviewableFileDetector.getHighlightLanguage(Paths.get("data.json")))
        assertEquals("yaml", PreviewableFileDetector.getHighlightLanguage(Paths.get("config.yaml")))
        assertEquals("xml", PreviewableFileDetector.getHighlightLanguage(Paths.get("layout.xml")))
    }

    @Test
    fun getHighlightLanguage_unknown() {
        assertEquals("", PreviewableFileDetector.getHighlightLanguage(Paths.get("readme.txt")))
        assertEquals("", PreviewableFileDetector.getHighlightLanguage(Paths.get("error.log")))
    }

    @Test
    fun caseInsensitive() {
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("README.MD")))
        assertTrue(PreviewableFileDetector.isPreviewable(Paths.get("MAIN.KT")))
        assertEquals(
            PreviewType.MARKDOWN,
            PreviewableFileDetector.getPreviewType(Paths.get("README.MD"))
        )
        assertEquals("kotlin", PreviewableFileDetector.getHighlightLanguage(Paths.get("MAIN.KT")))
    }
}
