# Changelog

## Unreleased

### Added

- **Built-in Preview & Edit for Markdown, Code & Text files**
  - Long-press one or more files → "Preview" in context menu → opens rendered view
  - Supports 40+ file types: Markdown (`.md`, `.markdown`, `.mdown`, `.mkd`), code (`.kt`, `.py`, `.js`, `.ts`, `.go`, `.rs`, `.java`, `.c`, `.cpp`, `.swift`, `.rb`, `.php`, `.html`, `.css`, `.scss`, `.json`, `.xml`, `.yaml`, `.toml`, `.gradle`, `.sh`, `.sql`, `.dart`, and more), and text files (`.txt`, `.log`, `.gitignore`, `.env`, etc.)
  - `PreviewableFileDetector`: Extension-based classification with case-insensitive matching
  - `MimeTypeConversionExtensions`: Extended with 40+ missing MIME type overrides + preview property helpers
  - Non-previewable files don't show the Preview option in the context menu

- **Preview/Edit dual mode**
  - Default mode: Preview (read-only, rendered via WebView)
  - Toolbar toggle button (eye icon) switches between Preview and Edit modes
  - Save button appears when edits are made in either mode
  - Edit mode features full editor with save, reload, encoding selection, and line numbers

- **Preview engine** (WebView-based)
  - Markdown rendered via Marked.js v4 (GFM, tables, task lists, code blocks)
  - Syntax highlighting via highlight.js v11.9.0 for all supported languages
  - Theme-aware rendering with CSS variables and `@media (prefers-color-scheme)` fallback
  - Theme switching updates CSS variables instantly via JS bridge
  - `PreviewRenderer`: Text/Code/Markdown content injection into `preview.html` template
  - `PreviewWebViewClient`: WebView lifecycle callbacks and error handling

- **ViewPager2 swipe navigation**
  - `FileViewerEditorActivity` with `FragmentStateAdapter` for lazy-loaded pages
  - Each page is a `TextEditorFragment` instance with its own toolbar and state
  - Offscreen page limit of 1 for memory efficiency
  - Supports opening multiple selected files from the file browser

- **Large file handling**
  - Warning dialog for files >1MB showing file size
  - "Load anyway" option loads files up to 5MB
  - Previous behavior: hard error at 1MB limit

- **Line numbers in edit mode**
  - `LineNumberView`: Custom view drawing right-aligned numbers with separator
  - Scroll-synced with the editor content via `NestedScrollView.OnScrollChangeListener`
  - Hidden in Preview mode, shown in Edit mode
  - Theme-aware colors using `textColorTertiary` attribute

### Changed

- `TextEditorFragment` extended with dual Preview/Edit mode, WebView initialization, theme detection, line number integration, and large file dialog
- `TextEditorViewModel` increased `MAX_FILE_SIZE` from 1MB to 5MB, added `largeFileSize` StateFlow and `loadLargeFile()` method
- `text_editor_fragment.xml` layout: added `WebView` container, `LineNumberView` alongside `ScrollingChildEditText`
- `text_editor.xml` menu: added `action_preview_edit_toggle` with eye icon
- `file_list_select.xml` menu: added `action_preview` item for multi-select
- `FileListFragment`: wired Preview into overlay action mode with visibility logic
- `preview.html`: migrated from CSS class-based theming to CSS custom properties with `prefers-color-scheme` media query fallback

### New files

- `app/src/main/java/me/zhanghai/android/files/file/PreviewType.kt`
- `app/src/main/java/me/zhanghai/android/files/file/PreviewableFileDetector.kt`
- `app/src/main/java/me/zhanghai/android/files/viewer/text/PreviewRenderer.kt`
- `app/src/main/java/me/zhanghai/android/files/viewer/text/PreviewWebViewClient.kt`
- `app/src/main/java/me/zhanghai/android/files/viewer/text/FileViewerEditorActivity.kt`
- `app/src/main/java/me/zhanghai/android/files/viewer/text/FileViewerEditorAdapter.kt`
- `app/src/main/java/me/zhanghai/android/files/viewer/text/ConfirmLargeFileDialogFragment.kt`
- `app/src/main/java/me/zhanghai/android/files/ui/LineNumberView.kt`
- `app/src/main/assets/preview/preview.html`
- `app/src/main/assets/preview/marked.min.js`
- `app/src/main/assets/preview/highlight.min.js`
- `app/src/main/assets/preview/github-light.css`
- `app/src/main/assets/preview/github-dark.css`
- `app/src/main/res/drawable/eye_icon.xml`
- `app/src/test/java/me/zhanghai/android/files/file/PreviewableFileDetectorTest.kt`
