package me.zhanghai.android.files.recent

import android.content.Context
import java8.nio.file.Path
import java8.nio.file.Paths
import org.json.JSONArray

object RecentFilesRepository {
    private const val PREFS_NAME = "recent_files"
    private const val KEY_RECENT_PATHS = "recent_paths"
    private const val MAX_ITEMS = 20

    fun add(context: Context, path: Path) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val list = getList(context).toMutableList()
        val pathString = path.toString()
        
        // Remove if existing to move to top
        list.removeIf { it.toString() == pathString }
        list.add(0, path)
        
        if (list.size > MAX_ITEMS) {
            list.removeAt(list.lastIndex)
        }
        
        saveList(context, list)
    }

    fun getList(context: Context): List<Path> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_RECENT_PATHS, "[]")
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<Path>()
        for (i in 0 until jsonArray.length()) {
            try {
                list.add(Paths.get(jsonArray.getString(i)))
            } catch (e: Exception) {
                // Ignore invalid paths
            }
        }
        return list
    }

    private fun saveList(context: Context, list: List<Path>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (path in list) {
            jsonArray.put(path.toString())
        }
        prefs.edit().putString(KEY_RECENT_PATHS, jsonArray.toString()).apply()
    }
    
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_RECENT_PATHS).apply()
    }
}
