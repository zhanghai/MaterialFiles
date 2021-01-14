/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
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
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.BookmarkDirectoryListFragmentBinding
import me.zhanghai.android.files.filelist.FileListActivity
import me.zhanghai.android.files.navigation.BookmarkDirectories
import me.zhanghai.android.files.navigation.BookmarkDirectory
import me.zhanghai.android.files.navigation.EditBookmarkDirectoryDialogFragment
import me.zhanghai.android.files.util.extraPath
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.getDrawable
import me.zhanghai.android.files.util.startActivityForResultSafe

class BookmarkDirectoryListFragment : Fragment(), BookmarkDirectoryAdapter.Listener,
    EditBookmarkDirectoryDialogFragment.Listener {
    private lateinit var binding: BookmarkDirectoryListFragmentBinding

    private lateinit var adapter: BookmarkDirectoryAdapter
    private lateinit var dragDropManager: RecyclerViewDragDropManager
    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

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
        binding.recyclerView.layoutManager = LinearLayoutManager(
            activity, RecyclerView.VERTICAL, false
        )
        adapter = BookmarkDirectoryAdapter(this)
        dragDropManager = RecyclerViewDragDropManager().apply {
            setDraggingItemShadowDrawable(
                getDrawable(R.drawable.ms9_composite_shadow_z2) as NinePatchDrawable
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                // This recreates MainActivity but we cannot have singleTop as launch mode along
                // with document launch mode.
                //AppCompatActivity activity = (AppCompatActivity) requireActivity();
                //activity.onSupportNavigateUp();
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_PICK_DIRECTORY ->
                if (resultCode == Activity.RESULT_OK) {
                    data?.extraPath?.let { addBookmarkDirectory(it) }
                }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onBookmarkDirectoryListChanged(bookmarkDirectories: List<BookmarkDirectory>) {
        binding.emptyView.fadeToVisibilityUnsafe(bookmarkDirectories.isEmpty())
        adapter.replace(bookmarkDirectories)
    }

    private fun onAddBookmarkDirectory() {
        val intent = FileListActivity.createPickDirectoryIntent(null)
        startActivityForResultSafe(intent, REQUEST_CODE_PICK_DIRECTORY)
    }

    private fun addBookmarkDirectory(path: Path) {
        BookmarkDirectories.add(BookmarkDirectory(null, path))
    }

    override fun editBookmarkDirectory(bookmarkDirectory: BookmarkDirectory) {
        EditBookmarkDirectoryDialogFragment.show(bookmarkDirectory, this)
    }

    override fun moveBookmarkDirectory(fromPosition: Int, toPosition: Int) {
        BookmarkDirectories.move(fromPosition, toPosition)
    }

    override fun replaceBookmarkDirectory(bookmarkDirectory: BookmarkDirectory) {
        BookmarkDirectories.replace(bookmarkDirectory)
    }

    override fun removeBookmarkDirectory(bookmarkDirectory: BookmarkDirectory) {
        BookmarkDirectories.remove(bookmarkDirectory)
    }

    companion object {
        private const val REQUEST_CODE_PICK_DIRECTORY = 1
    }
}
