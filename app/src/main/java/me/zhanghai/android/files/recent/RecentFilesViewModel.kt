package me.zhanghai.android.files.recent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.loadFileItem
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import java.io.IOException

class RecentFilesViewModel(application: Application) : AndroidViewModel(application) {
    private val _files = MutableLiveData<Stateful<List<FileItem>>>()
    val files: LiveData<Stateful<List<FileItem>>> = _files

    init {
        loadFiles()
    }

    private fun loadFiles() {
        _files.value = Loading(null)
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val context = getApplication<Application>()
                val paths = RecentFilesRepository.getList(context)
                val items = paths.mapNotNull { path ->
                    try {
                        path.loadFileItem()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                withContext(Dispatchers.Main) {
                    _files.value = Success(items)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _files.value = Failure(null, e)
                }
            }
        }
    }
}
