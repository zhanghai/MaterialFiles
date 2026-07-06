/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import java8.nio.file.Path

object PreviewableFileDetector {
    private val markdownExtensions = setOf(
        "md", "markdown", "mdown", "mkd"
    )

    private val codeExtensions = setOf(
        "kt", "ktm", "kts",
        "java", "jav", "jsh",
        "xml", "xsd", "xslt", "xsl", "svg", "plist",
        "json", "jsonc", "json5",
        "gradle", "gradle.kts",
        "js", "jsx", "mjs", "cjs",
        "ts", "tsx",
        "py", "py3", "pyx", "py3x", "wsgi",
        "sh", "bash", "zsh", "ksh", "fish",
        "html", "htm", "xhtml",
        "css", "scss", "sass", "less", "styl",
        "yaml", "yml",
        "toml",
        "ini", "cfg", "conf",
        "php", "rb", "go", "rs", "swift", "c", "cpp", "cxx", "h", "hpp",
        "sql", "r", "m", "mm", "pl", "pm", "lua", "dart", "groovy",
        "bat", "cmd", "ps1", "psm1",
        "dockerfile", "makefile", "cmake",
        "diff", "patch",
        "proto", "graphql", "gql",
        "env", "editorconfig", "gitignore", "gitattributes",
        "properties", "prop"
    )

    private val textExtensions = setOf(
        "txt", "log", "text", "asc",
        "nfo", "readme", "changes", "changelog",
        "license", "copying", "authors",
        "todo"
    )

    fun isPreviewable(path: Path): Boolean {
        val extension = path.extension?.lowercase()
        if (extension != null) {
            if (extension in markdownExtensions) return true
            if (extension in codeExtensions) return true
            if (extension in textExtensions) return true
        }
        val fileName = path.fileName.toString().lowercase()
        if (fileName in codeExtensions) return true
        return false
    }

    fun getPreviewType(path: Path): PreviewType {
        val extension = path.extension?.lowercase()
        if (extension in markdownExtensions) return PreviewType.MARKDOWN
        if (extension in codeExtensions) return PreviewType.CODE
        val fileName = path.fileName.toString().lowercase()
        if (fileName in codeExtensions) return PreviewType.CODE
        return PreviewType.TEXT
    }

    fun getHighlightLanguage(path: Path): String {
        val extension = path.extension?.lowercase() ?: return ""
        val fileName = path.fileName.toString().lowercase()
        return when (extension) {
            "kt", "ktm", "kts" -> "kotlin"
            "java", "jav" -> "java"
            "xml", "xsd", "xslt", "xsl", "plist" -> "xml"
            "svg" -> "xml"
            "json", "jsonc", "json5" -> "json"
            "gradle", "gradle.kts" -> "gradle"
            "js", "jsx", "mjs", "cjs" -> "javascript"
            "ts", "tsx" -> "typescript"
            "py", "py3", "pyx", "py3x", "wsgi" -> "python"
            "sh", "bash", "zsh", "ksh", "fish" -> "bash"
            "html", "htm", "xhtml" -> "html"
            "css" -> "css"
            "scss", "sass" -> "scss"
            "less" -> "less"
            "styl" -> "stylus"
            "yaml", "yml" -> "yaml"
            "toml" -> "toml"
            "ini", "cfg", "conf" -> "ini"
            "php" -> "php"
            "rb" -> "ruby"
            "go" -> "go"
            "rs" -> "rust"
            "swift" -> "swift"
            "c", "h" -> "c"
            "cpp", "cxx", "hpp" -> "cpp"
            "sql" -> "sql"
            "r" -> "r"
            "m" -> "objectivec"
            "mm" -> "objectivec"
            "pl", "pm" -> "perl"
            "lua" -> "lua"
            "dart" -> "dart"
            "groovy" -> "groovy"
            "bat", "cmd" -> "dos"
            "ps1", "psm1" -> "powershell"
            "diff", "patch" -> "diff"
            "proto" -> "protobuf"
            "graphql", "gql" -> "graphql"
            "dockerfile" -> "dockerfile"
            "makefile" -> "makefile"
            "cmake" -> "cmake"
            "env" -> "ini"
            "properties", "prop" -> "properties"
            "editorconfig" -> "editorconfig"
            "gitignore", "gitattributes" -> "git"
            else -> if (fileName == "dockerfile") "dockerfile"
            else if (fileName == "makefile") "makefile"
            else if (fileName == "cmakelists.txt") "cmake"
            else ""
        }
    }
}
