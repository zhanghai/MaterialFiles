package me.zhanghai.android.files.recent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.RecentFilesFragmentBinding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.isApk
import me.zhanghai.android.files.ui.OverlayToolbarActionMode
import me.zhanghai.android.files.ui.ToolbarActionMode
import me.zhanghai.android.files.util.createViewIntent
import me.zhanghai.android.files.util.createInstallPackageIntent
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.viewModels
import androidx.core.view.isVisible
import android.content.Intent

class RecentFilesFragment : Fragment(), RecentFilesAdapter.Listener {
    private lateinit var binding: RecentFilesFragmentBinding
    private lateinit var overlayActionMode: ToolbarActionMode

    private val viewModel by viewModels { { RecentFilesViewModel(requireActivity().application) } }
    private lateinit var adapter: RecentFilesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        RecentFilesFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.setTitle(R.string.recent_files_title)

        overlayActionMode = OverlayToolbarActionMode(binding.overlayToolbar)
        binding.appBarLayout.syncBackgroundColorTo(binding.overlayToolbar)

        binding.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        adapter = RecentFilesAdapter(this)
        binding.recyclerView.adapter = adapter

        viewModel.files.observe(viewLifecycleOwner) { stateful ->
            binding.progress.isVisible = stateful is me.zhanghai.android.files.util.Loading
            binding.emptyView.isVisible = stateful is me.zhanghai.android.files.util.Success && stateful.value.isEmpty()
            binding.recyclerView.isVisible = stateful is me.zhanghai.android.files.util.Success && stateful.value.isNotEmpty()

            if (stateful is me.zhanghai.android.files.util.Success) {
                adapter.submitList(stateful.value)
            } else if (stateful is me.zhanghai.android.files.util.Failure) {
                stateful.throwable.printStackTrace()
                me.zhanghai.android.files.util.showToast(stateful.throwable.toString())
            }
        }
    }

    override fun openFile(file: FileItem) {
        if (!file.mimeType.isApk) {
            val intent = file.path.createViewIntent(file.mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .apply { putArgs(file) }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Logic for APKs (omitted for brevity, or can copy from FileListFragment)
             val intent = file.path.createInstallPackageIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
             try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
