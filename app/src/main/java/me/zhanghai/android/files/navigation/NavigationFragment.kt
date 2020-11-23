/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.storage.StorageVolume
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import java8.nio.file.Path
import me.zhanghai.android.files.databinding.NavigationFragmentBinding
import me.zhanghai.android.files.file.DocumentTreeUri
import me.zhanghai.android.files.file.asDocumentTreeUri
import me.zhanghai.android.files.file.asDocumentTreeUriOrNull
import me.zhanghai.android.files.file.releasePersistablePermission
import me.zhanghai.android.files.file.takePersistablePermission
import me.zhanghai.android.files.provider.document.documentTreeUri
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.util.startActivityForResultSafe

class NavigationFragment : Fragment(), NavigationItem.Listener,
    ConfirmRemoveDocumentTreeDialogFragment.Listener, EditBookmarkDirectoryDialogFragment.Listener {
    private lateinit var binding: NavigationFragmentBinding

    private lateinit var adapter: NavigationListAdapter

    lateinit var listener: Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        NavigationFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.recyclerView.setHasFixedSize(true)
        // TODO: Needed?
        //binding.recyclerView.setItemAnimator(new NoChangeAnimationItemAnimator());
        val context = requireContext()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = NavigationListAdapter(this)
        binding.recyclerView.adapter = adapter

        val viewLifecycleOwner = viewLifecycleOwner
        NavigationItemListLiveData.observe(viewLifecycleOwner) { onNavigationItemsChanged(it) }
        listener.observeCurrentPath(viewLifecycleOwner) { onCurrentPathChanged(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_OPEN_DOCUMENT_TREE ->
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.asDocumentTreeUriOrNull()?.let { addDocumentTree(it) }
                }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onNavigationItemsChanged(navigationItems: List<NavigationItem?>) {
        adapter.replace(navigationItems)
    }

    private fun onCurrentPathChanged(path: Path) {
        adapter.notifyCheckedChanged()
    }

    override val currentPath: Path
        get() = listener.currentPath

    override fun navigateTo(path: Path) {
        listener.navigateTo(path)
    }

    override fun navigateToRoot(path: Path) {
        listener.navigateToRoot(path)
    }

    override fun onAddDocumentTree() {
        startActivityForResultSafe(
            DocumentTreeUri.createOpenIntent(), REQUEST_CODE_OPEN_DOCUMENT_TREE
        )
    }

    private fun addDocumentTree(treeUri: DocumentTreeUri) {
        treeUri.takePersistablePermission()
    }

    override fun onRemoveDocumentTree(treeUri: DocumentTreeUri, storageVolume: StorageVolume?) {
        ConfirmRemoveDocumentTreeDialogFragment.show(treeUri, storageVolume, this)
    }

    override fun removeDocumentTree(treeUri: DocumentTreeUri) {
        treeUri.releasePersistablePermission()
        val currentPath = listener.currentPath
        if (currentPath.isDocumentPath
            && currentPath.documentTreeUri.asDocumentTreeUri() == treeUri) {
            listener.navigateToDefaultRoot()
        }
    }

    override fun onEditBookmarkDirectory(bookmarkDirectory: BookmarkDirectory) {
        EditBookmarkDirectoryDialogFragment.show(bookmarkDirectory, this)
    }

    override fun replaceBookmarkDirectory(bookmarkDirectory: BookmarkDirectory) {
        BookmarkDirectories.replace(bookmarkDirectory)
    }

    override fun removeBookmarkDirectory(bookmarkDirectory: BookmarkDirectory) {
        BookmarkDirectories.remove(bookmarkDirectory)
    }

    override fun closeNavigationDrawer() {
        listener.closeNavigationDrawer()
    }

    interface Listener {
        val currentPath: Path
        fun navigateTo(path: Path)
        fun navigateToRoot(path: Path)
        fun navigateToDefaultRoot()
        fun observeCurrentPath(owner: LifecycleOwner, observer: (Path) -> Unit)
        fun closeNavigationDrawer()
    }

    companion object {
        private const val REQUEST_CODE_OPEN_DOCUMENT_TREE = 1
    }
}
