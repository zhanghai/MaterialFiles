# Plan: Device-wide media category entries (Images/Videos/Audio/Documents/APKs)

## Context

MaterialFiles' navigation drawer currently exposes path-bound "Standard Directories" — Pictures, Movies, Music, Documents — that only show files inside those specific folders on primary external storage. Users want categories that aggregate files of a given type (images, videos, audio, documents, APKs) **across the entire device**, including subfolders, secondary storage, and any folder regardless of name. Users should be able to tap an entry and get a single flat list of every matching file.

This change adds five new drawer entries that populate from `MediaStore.Files` and surface results as ordinary `LinuxPath`-backed `FileItem`s, so all existing per-file actions (open, share, delete, properties, copy/move, viewer) continue to work without modification. The app already holds `MANAGE_EXTERNAL_STORAGE`, so listing arbitrary on-disk paths from `MediaStore.MediaColumns.DATA` is permitted.

## Approach

A new minimal `FileSystemProvider` (scheme `mediastore`) exposes five virtual roots (`mediastore:/images`, `/videos`, `/audio`, `/documents`, `/apks`). Its only meaningful operation is `newDirectoryStream`, which queries `MediaStore.Files` with a per-category selection and yields `LinuxPath`s for each result. All other provider operations throw `UnsupportedOperationException` (consumer never calls them on the synthetic root; mutations always target the linux-backed children). This isolates the synthetic mode from path-keyed subsystems (sort, view-type, breadcrumbs, bookmarks) — each category gets its own stable URI key.

Search inside a category is gated off in v1 (the existing `WalkFileTreeSearchable` cannot walk a synthetic root cheaply).

## Files to create

| Path | Purpose |
|------|---------|
| `app/src/main/java/me/zhanghai/android/files/provider/mediastore/MediaStoreCategory.kt` | Enum `IMAGES, VIDEOS, AUDIO, DOCUMENTS, APKS` with `segment: String`, `selection: String`, `selectionArgs`, `iconRes`, `titleRes`. Companion `fromSegment(String)`. |
| `app/src/main/java/me/zhanghai/android/files/provider/mediastore/MediaStoreFileSystem.kt` | Singleton `FileSystem`. Mirrors `ContentFileSystem` shape; provides `getPath(first, vararg)` returning `MediaStorePath`. |
| `app/src/main/java/me/zhanghai/android/files/provider/mediastore/MediaStorePath.kt` | `ByteStringListPath<MediaStorePath>` subclass, `Parcelable`, `uriScheme = "mediastore"`. Five well-known absolute single-segment paths. `toFile`, `toRealPath`, `register` throw `UnsupportedOperationException`. |
| `app/src/main/java/me/zhanghai/android/files/provider/mediastore/MediaStoreFileSystemProvider.kt` | `object : FileSystemProvider()`. Modeled on `provider/content/ContentFileSystemProvider.kt`. Implements `getScheme="mediastore"`, `getPath(URI)`, `newFileSystem`/`getFileSystem`, `newDirectoryStream`, `readAttributes` (returns `MediaStoreRootAttributes` with `isDirectory=true`), `checkAccess` (no-op), `isHidden=false`, `isSameFile==`. All mutating overrides throw `UnsupportedOperationException` after the standard `ProviderMismatchException` guard. |
| `app/src/main/java/me/zhanghai/android/files/provider/mediastore/MediaStoreLister.kt` | Helper: `query(category): List<Path>`. Calls `appContext.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(MediaColumns.DATA), category.selection, category.selectionArgs, null)`. Wraps cursor in `use`, filters `null/blank` and non-existent files (`File(path).isFile`), maps each to `Paths.get(file)` (linux path). |
| `app/src/main/java/me/zhanghai/android/files/provider/mediastore/MediaStoreRootAttributes.kt` | Tiny `BasicFileAttributes` with `isDirectory=true`, others zero/`FileTime.fromMillis(0)`. |
| `app/src/main/res/drawable/apk_icon_white_24dp.xml` | Drawer-style 24dp white-tinted vector for APKs (parallel to `image_icon_white_24dp.xml`). Reuse path data from `file_apk_icon.xml` but tint white at 24dp. |

## Files to modify

### `app/src/main/java/me/zhanghai/android/files/provider/FileSystemProviders.kt`
Within the `if (!isRunningAsRoot)` block (~line 36-51), add:
```kotlin
FileSystemProvider.installProvider(MediaStoreFileSystemProvider)
```
parallel to the existing `ContentFileSystemProvider` registration.

### `app/src/main/java/me/zhanghai/android/files/navigation/NavigationItems.kt`
1. Add private getter `mediaCategoryItems: List<NavigationItem>` returning `MediaStoreCategory.values().map { MediaCategoryItem(it) }`.
2. Add private class `MediaCategoryItem(category)` extending `PathItem(MediaStoreFileSystemProvider.getCategoryPath(category))` and implementing `NavigationRoot` so breadcrumbs show the category title rather than `/images`. Override `id`, `iconRes`, `getTitle`, `getName`.
3. In `navigationItems` getter (line 39-63), insert a new section **after `AddStorageItem`** and **before `standardDirectoryItems`**:
   ```kotlin
   add(null)
   addAll(mediaCategoryItems)
   ```

### `app/src/main/res/values/strings.xml` (around line 688)
Add:
```xml
<string name="navigation_media_category_images">Images</string>
<string name="navigation_media_category_videos">Videos</string>
<string name="navigation_media_category_audio">Audio</string>
<string name="navigation_media_category_documents">Documents</string>
<string name="navigation_media_category_apks">APKs</string>
```
(Localized translations can follow in later commits — defaults fall back fine.)

### `app/src/main/AndroidManifest.xml` (around line 24)
Add (alongside existing storage perms):
```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
```
No new runtime permission flow — `MANAGE_EXTERNAL_STORAGE` (already granted via existing flow) covers MediaStore reads. These declarations future-proof if the user revokes All Files Access.

### `app/src/main/java/me/zhanghai/android/files/filelist/FileListFragment.kt`
Locate the search menu-item visibility logic (search for `R.id.action_search` in `onPrepareOptionsMenu` / menu inflation). Hide the search action when `viewModel.currentPath is MediaStorePath`. Same for "create file/folder", "paste", "select all" if those would behave incorrectly on the synthetic root — verify by inspection during implementation; default to hiding any item whose enable-state is path-bound.

## MediaStore selection per category

| Category | Selection | API notes |
|---------|-----------|-----------|
| IMAGES | `MEDIA_TYPE = ?` arg = `MEDIA_TYPE_IMAGE` | All API levels |
| VIDEOS | `MEDIA_TYPE = ?` arg = `MEDIA_TYPE_VIDEO` | All API levels |
| AUDIO | `MEDIA_TYPE = ?` arg = `MEDIA_TYPE_AUDIO` | All API levels |
| DOCUMENTS | API 30+: `MEDIA_TYPE = ?` arg = `MEDIA_TYPE_DOCUMENT` (`6`); API 23-29: `MIME_TYPE IN (...)` allowlist (pdf, msword, vnd.openxmlformats-officedocument.*, ms-excel, ms-powerpoint, vnd.oasis.opendocument.*, rtf, plain, csv, epub, application/json, application/xml) | Branch on `Build.VERSION.SDK_INT` |
| APKS | `MIME_TYPE = ?` arg = `application/vnd.android.package-archive` | All API levels |

All queries also AND `DATA IS NOT NULL`. Cursor read is wrapped in `use {}`.

## Reused existing code

- `provider/content/ContentFileSystemProvider.kt` — template (read-only provider with mostly throwing overrides).
- `provider/common/ByteStringListPath.kt` — base class for `MediaStorePath`.
- `file/FileItem.kt:46-70` `loadFileItem(path)` — unchanged; consumes `LinuxPath`s from the listing.
- `filelist/FileListLiveData.kt:42` — unchanged; calls `path.newDirectoryStream()` via the new provider.
- `app/SystemServices.kt:9` `appContext.contentResolver` — used by `MediaStoreLister`.
- `compat/MimeTypeMapCompat.kt`, `file/MimeType.kt` — for selection-clause MIME constants.
- Existing drawer drawables `image_icon_white_24dp.xml`, `video_icon_white_24dp.xml`, `audio_icon_white_24dp.xml`, `document_icon_white_24dp.xml` — reused for category items.

## Verification

1. `./gradlew :app:assembleDebug` builds clean.
2. `adb install -r app/build/outputs/apk/foss/debug/app-foss-debug.apk` (or whichever variant is active).
3. Launch app → grant All Files Access if prompted (existing flow).
4. Open navigation drawer → confirm 5 new entries below "Add storage" with proper icons + labels.
5. Tap **Images** → file list populates with images from `/sdcard/DCIM`, `/sdcard/Pictures`, `/sdcard/Download`, app-private screenshots, etc. Verify count plausibly matches a manual `find /sdcard -iname '*.jpg' -o -iname '*.png' | wc -l`.
6. Tap a listed image → opens in `ImageViewerActivity`; swipe shows neighbors from the same listing.
7. Long-press an item → confirm Cut/Copy/Delete/Properties/Share/Open With render and execute against the real linux path.
8. Tap **Videos**, **Audio**, **Documents**, **APKs** in turn — each populates appropriately. APKs entry should include downloaded `.apk` files. Documents entry should include PDFs and Office docs.
9. Rotate device while in a category → list survives (parcel round-trip on `MediaStorePath`).
10. Sort options changed inside Images do not bleed into Videos or into a real directory listing.
11. Search action either hidden or disabled inside a category; back button exits to last real screen / drawer.
12. Repeat smoke test on an API 23 emulator to confirm the API-29 documents fallback selection works.
13. Repeat smoke test on an API 33+ device with All Files Access revoked → MediaStore queries should still succeed via `READ_MEDIA_*`.

## Out of scope (future work)

- Per-category enable/disable in `Settings → Standard Directories` UI.
- Searching within a category (would require flat in-memory filter).
- Grouping by date / album / artist for media categories.
- Custom category sort options persisted separately from path-bound sorts.
