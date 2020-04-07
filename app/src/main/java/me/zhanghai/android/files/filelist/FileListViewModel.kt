/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import java8.nio.file.Path
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.filelist.FileSortOptions.By
import me.zhanghai.android.files.filelist.FileSortOptions.Order
import me.zhanghai.android.files.provider.archive.archiveRefresh
import me.zhanghai.android.files.provider.archive.isArchivePath
import me.zhanghai.android.files.util.CloseableLiveData
import me.zhanghai.android.files.util.valueCompat
import java.io.Closeable
import java.util.LinkedHashSet

// TODO: Use SavedStateHandle to save state.
class FileListViewModel : ViewModel() {
    private val trailLiveData = TrailLiveData()
    val hasTrail: Boolean
        get() = trailLiveData.value != null
    val pendingState: Parcelable?
        get() = trailLiveData.valueCompat.pendingState

    fun navigateTo(lastState: Parcelable, path: Path) = trailLiveData.navigateTo(lastState, path)

    fun resetTo(path: Path) = trailLiveData.resetTo(path)

    fun navigateUp(overrideBreadcrumb: Boolean): Boolean =
        if (!overrideBreadcrumb && breadcrumbLiveData.valueCompat.selectedIndex == 0) {
            false
        } else {
            trailLiveData.navigateUp()
        }

    fun reload() {
        val path = trailLiveData.valueCompat.currentPath
        if (path.isArchivePath) {
            path.archiveRefresh()
        }
        trailLiveData.reload()
    }

    val currentPathLiveData = trailLiveData.map { it.currentPath }
    val currentPath: Path
        get() = currentPathLiveData.valueCompat

    private val _searchStateLiveData = MutableLiveData(SearchState(false, ""))
    val searchStateLiveData: LiveData<SearchState> = _searchStateLiveData
    val searchState: SearchState
        get() = _searchStateLiveData.valueCompat

    fun search(query: String) {
        val searchState = _searchStateLiveData.valueCompat
        if (searchState.isSearching && searchState.query == query) {
            return
        }
        _searchStateLiveData.value = SearchState(true, query)
    }

    fun stopSearching() {
        val searchState = _searchStateLiveData.valueCompat
        if (!searchState.isSearching) {
            return
        }
        _searchStateLiveData.value = SearchState(false, "")
    }

    private val _fileListLiveData =
        FileListSwitchMapLiveData(currentPathLiveData, _searchStateLiveData)
    val fileListLiveData: LiveData<FileListData>
        get() = _fileListLiveData
    val fileListData: FileListData
        get() = _fileListLiveData.valueCompat

    val searchViewExpandedLiveData = MutableLiveData(false)
    var isSearchViewExpanded: Boolean
        get() = searchViewExpandedLiveData.valueCompat
        set(value) {
            if (searchViewExpandedLiveData.valueCompat == value) {
                return
            }
            searchViewExpandedLiveData.value = value
        }

    private val _searchViewQueryLiveData = MutableLiveData("")
    var searchViewQuery: String
        get() = _searchViewQueryLiveData.valueCompat
        set(value) {
            if (_searchViewQueryLiveData.valueCompat == value) {
                return
            }
            _searchViewQueryLiveData.value = value
        }

    val breadcrumbLiveData: LiveData<BreadcrumbData> = BreadcrumbLiveData(trailLiveData)

    private val _sortOptionsLiveData = FileSortOptionsLiveData(currentPathLiveData)
    val sortOptionsLiveData: LiveData<FileSortOptions> = _sortOptionsLiveData
    val sortOptions: FileSortOptions
        get() = _sortOptionsLiveData.valueCompat

    fun setSortBy(by: By) = _sortOptionsLiveData.putBy(by)

    fun setSortOrder(order: Order) = _sortOptionsLiveData.putOrder(order)

    fun setSortDirectoriesFirst(isDirectoriesFirst: Boolean) =
        _sortOptionsLiveData.putIsDirectoriesFirst(isDirectoriesFirst)

    private val _sortPathSpecificLiveData = FileSortPathSpecificLiveData(currentPathLiveData)
    val sortPathSpecificLiveData: LiveData<Boolean>
        get() = _sortPathSpecificLiveData
    var isSortPathSpecific: Boolean
        get() = _sortPathSpecificLiveData.valueCompat
        set(value) {
            _sortPathSpecificLiveData.putValue(value)
        }

    private val _pickOptionsLiveData = MutableLiveData<PickOptions?>()
    val pickOptionsLiveData: LiveData<PickOptions?>
        get() = _pickOptionsLiveData
    var pickOptions: PickOptions?
        get() = _pickOptionsLiveData.value
        set(value) {
            _pickOptionsLiveData.value = value
        }

    private val _selectedFilesLiveData = MutableLiveData(LinkedHashSet<FileItem>())
    val selectedFilesLiveData: LiveData<LinkedHashSet<FileItem>>
        get() = _selectedFilesLiveData
    val selectedFiles: LinkedHashSet<FileItem>
        get() = _selectedFilesLiveData.valueCompat

    fun selectFile(file: FileItem, selected: Boolean) {
        selectFiles(linkedSetOf(file), selected)
    }

    fun selectFiles(files: LinkedHashSet<FileItem>, selected: Boolean) {
        val selectedFiles = _selectedFilesLiveData.valueCompat
        if (selectedFiles === files) {
            if (!selected && selectedFiles.isNotEmpty()) {
                selectedFiles.clear()
                _selectedFilesLiveData.value = selectedFiles
            }
            return
        }
        var changed = false
        for (file in files) {
            changed = changed or if (selected) {
                selectedFiles.add(file)
            } else {
                selectedFiles.remove(file)
            }
        }
        if (changed) {
            _selectedFilesLiveData.value = selectedFiles
        }
    }

    fun replaceSelectedFiles(files: LinkedHashSet<FileItem>) {
        val selectedFiles = _selectedFilesLiveData.valueCompat
        if (selectedFiles == files) {
            return
        }
        selectedFiles.clear()
        selectedFiles.addAll(files)
        _selectedFilesLiveData.value = selectedFiles
    }

    fun clearSelectedFiles() {
        val selectedFiles = _selectedFilesLiveData.valueCompat
        if (selectedFiles.isEmpty()) {
            return
        }
        selectedFiles.clear()
        _selectedFilesLiveData.value = selectedFiles
    }

    val pasteStateLiveData: LiveData<PasteState> = _pasteStateLiveData
    val pasteState: PasteState
        get() = _pasteStateLiveData.valueCompat

    fun addToPasteState(copy: Boolean, files: LinkedHashSet<FileItem>) {
        val pasteState = _pasteStateLiveData.valueCompat
        var changed = false
        if (pasteState.copy != copy) {
            changed = pasteState.files.isNotEmpty()
            pasteState.files.clear()
            pasteState.copy = copy
        }
        changed = changed or pasteState.files.addAll(files)
        if (changed) {
            _pasteStateLiveData.value = pasteState
        }
    }

    fun clearPasteState() {
        val pasteState = _pasteStateLiveData.valueCompat
        if (pasteState.files.isEmpty()) {
            return
        }
        pasteState.files.clear()
        _pasteStateLiveData.value = pasteState
    }

    override fun onCleared() {
        _fileListLiveData.close()
    }

    companion object {
        private val _pasteStateLiveData = MutableLiveData(PasteState())
    }

    private class FileListSwitchMapLiveData(
        private val pathLiveData: LiveData<Path>,
        private val searchStateLiveData: LiveData<SearchState>
    ) : MediatorLiveData<FileListData>(), Closeable {
        private var liveData: CloseableLiveData<out FileListData>? = null

        init {
            addSource(pathLiveData) { updateSource() }
            addSource(searchStateLiveData) { updateSource() }
        }

        private fun updateSource() {
            liveData?.let {
                removeSource(it)
                it.close()
            }
            val path = pathLiveData.valueCompat
            val searchState = searchStateLiveData.valueCompat
            val liveData = if (searchState.isSearching) {
                SearchFileListLiveData(path, searchState.query)
            } else {
                FileListLiveData(path)
            }
            this.liveData = liveData
            addSource(liveData) { value = it }
        }

        override fun close() {
            liveData?.let {
                removeSource(it)
                it.close()
                this.liveData = null
            }
        }
    }
}
