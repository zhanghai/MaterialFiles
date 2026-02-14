package me.zhanghai.android.files.recent

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import me.zhanghai.android.files.databinding.FileItemListBinding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.filelist.FileListAdapter
import me.zhanghai.android.files.util.layoutInflater

import me.zhanghai.android.files.file.iconRes

class RecentFilesAdapter(
    private val listener: Listener
) : ListAdapter<FileItem, FileListAdapter.ViewHolder>(CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileListAdapter.ViewHolder {
        val inflater = parent.context.layoutInflater
        val binding = FileItemListBinding.inflate(inflater, parent, false)
        return FileListAdapter.ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileListAdapter.ViewHolder, position: Int) {
        val file = getItem(position)
        
        holder.nameText.text = file.name
        holder.iconImage.setImageResource(file.mimeType.iconRes)
        
        holder.itemLayout.setOnClickListener {
            listener.openFile(file)
        }
    }

    companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<FileItem>() {
            override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean =
                oldItem.path == newItem.path

            override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean =
                oldItem == newItem
        }
    }

    interface Listener {
        fun openFile(file: FileItem)
    }
}
