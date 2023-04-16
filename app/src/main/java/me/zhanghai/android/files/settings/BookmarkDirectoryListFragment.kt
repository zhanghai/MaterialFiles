/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import java8.nio.file.Path
import me.zhanghai.android.files.databinding.BookmarkDirectoryListFragmentBinding
import me.zhanghai.android.files.filelist.FileListActivity
import me.zhanghai.android.files.navigation.BookmarkDirectories
import me.zhanghai.android.files.navigation.BookmarkDirectory
import me.zhanghai.android.files.navigation.EditBookmarkDirectoryDialogActivity
import me.zhanghai.android.files.navigation.EditBookmarkDirectoryDialogFragment
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.getDrawable
import me.zhanghai.android.files.util.launchSafe
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.startActivitySafe

class BookmarkDirectoryListFragment : Fragment(), BookmarkDirectoryListAdapter.Listener {
    private val pickPathLauncher = registerForActivityResult(
        FileListActivity.PickDirectoryContract(), this::onPickPathResult
    )

    private lateinit var binding: BookmarkDirectoryListFragmentBinding

    private lateinit var adapter: BookmarkDirectoryListAdapter
    private lateinit var dragDropManager: RecyclerViewDragDropManager
    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        BookmarkDirectoryListFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            activity, RecyclerView.VERTICAL, false
        )
        adapter = BookmarkDirectoryListAdapter(this)
        dragDropManager = RecyclerViewDragDropManager().apply {
            setDraggingItemShadowDrawable(
                getDrawable(
                    com.h6ah4i.android.materialshadowninepatch.R.drawable.ms9_composite_shadow_z2
                ) as NinePatchDrawable
            )
        }
        wrappedAdapter = dragDropManager.createWrappedAdapter(adapter)
        binding.recyclerView.adapter = wrappedAdapter
        binding.recyclerView.itemAnimator = DraggableItemAnimator()
        dragDropManager.attachRecyclerView(binding.recyclerView)
        binding.fab.setOnClickListener { onAddBookmarkDirectory() }

        Settings.BOOKMARK_DIRECTORIES.observe(viewLifecycleOwner) {
            onBookmarkDirectoryListChanged(it)
        }
    }

    override fun onPause() {
        super.onPause()

        dragDropManager.cancelDrag()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        dragDropManager.release()
        WrapperAdapterUtils.releaseAll(wrappedAdapter)
    }

    private fun onBookmarkDirectoryListChanged(bookmarkDirectories: List<BookmarkDirectory>) {
        binding.emptyView.fadeToVisibilityUnsafe(bookmarkDirectories.isEmpty())
        adapter.replace(bookmarkDirectories)
    }

    private fun onAddBookmarkDirectory() {
        pickPathLauncher.launchSafe(null, this)
    }

    private fun onPickPathResult(result: Path?) {
        result ?: return
        BookmarkDirectories.add(BookmarkDirectory(null, result))
    }

    override fun editBookmarkDirectory(bookmarkDirectory: BookmarkDirectory) {
        startActivitySafe(
            EditBookmarkDirectoryDialogActivity::class.createIntent()
                .putArgs(EditBookmarkDirectoryDialogFragment.Args(bookmarkDirectory))
        )
    }

    override fun moveBookmarkDirectory(fromPosition: Int, toPosition: Int) {
        BookmarkDirectories.move(fromPosition, toPosition)
    }
}
