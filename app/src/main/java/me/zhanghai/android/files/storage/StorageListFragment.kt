/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

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
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.StorageListFragmentBinding
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.finish
import me.zhanghai.android.files.util.getDrawable
import me.zhanghai.android.files.util.startActivitySafe

class StorageListFragment : Fragment(), StorageAdapter.Listener {
    private lateinit var binding: StorageListFragmentBinding

    private lateinit var adapter: StorageAdapter
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
        StorageListFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            activity, RecyclerView.VERTICAL, false
        )
        adapter = StorageAdapter(this)
        dragDropManager = RecyclerViewDragDropManager().apply {
            setDraggingItemShadowDrawable(
                getDrawable(R.drawable.ms9_composite_shadow_z2) as NinePatchDrawable
            )
        }
        wrappedAdapter = dragDropManager.createWrappedAdapter(adapter)
        binding.recyclerView.adapter = wrappedAdapter
        binding.recyclerView.itemAnimator = DraggableItemAnimator()
        dragDropManager.attachRecyclerView(binding.recyclerView)
        binding.fab.setOnClickListener { onAddStorage() }

        Settings.STORAGES.observe(viewLifecycleOwner) { onStorageListChanged(it) }
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

    private fun onStorageListChanged(storages: List<Storage>) {
        binding.emptyView.fadeToVisibilityUnsafe(storages.isEmpty())
        adapter.replace(storages)
    }

    private fun onAddStorage() {
        startActivitySafe(AddStorageDialogActivity::class.createIntent())
    }

    override fun editStorage(storage: Storage) {
        // TODO
        //EditStorageDialogFragment.show(storage, this)
    }

    override fun moveStorage(fromPosition: Int, toPosition: Int) {
        Storages.move(fromPosition, toPosition)
    }
}
