/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.GravityCompat
import androidx.core.view.updatePaddingRelative
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.leinardi.android.speeddial.SpeedDialView
import java8.nio.file.Path
import java8.nio.file.Paths
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.app.clipboardManager
import me.zhanghai.android.files.compat.checkSelfPermissionCompat
import me.zhanghai.android.files.databinding.FileListFragmentAppBarIncludeBinding
import me.zhanghai.android.files.databinding.FileListFragmentBinding
import me.zhanghai.android.files.databinding.FileListFragmentBottomBarIncludeBinding
import me.zhanghai.android.files.databinding.FileListFragmentContentIncludeBinding
import me.zhanghai.android.files.databinding.FileListFragmentIncludeBinding
import me.zhanghai.android.files.databinding.FileListFragmentSpeedDialIncludeBinding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.asMimeTypeOrNull
import me.zhanghai.android.files.file.fileProviderUri
import me.zhanghai.android.files.file.isApk
import me.zhanghai.android.files.file.isImage
import me.zhanghai.android.files.filejob.FileJobService
import me.zhanghai.android.files.filelist.FileSortOptions.By
import me.zhanghai.android.files.filelist.FileSortOptions.Order
import me.zhanghai.android.files.fileproperties.FilePropertiesDialogFragment
import me.zhanghai.android.files.navigation.BookmarkDirectories
import me.zhanghai.android.files.navigation.BookmarkDirectory
import me.zhanghai.android.files.navigation.NavigationFragment
import me.zhanghai.android.files.navigation.NavigationRootMapLiveData
import me.zhanghai.android.files.provider.archive.createArchiveRootPath
import me.zhanghai.android.files.provider.archive.isArchivePath
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.terminal.Terminal
import me.zhanghai.android.files.ui.AppBarLayoutExpandHackListener
import me.zhanghai.android.files.ui.CoordinatorAppBarLayout
import me.zhanghai.android.files.ui.FixQueryChangeSearchView
import me.zhanghai.android.files.ui.OverlayToolbarActionMode
import me.zhanghai.android.files.ui.PersistentBarLayout
import me.zhanghai.android.files.ui.PersistentBarLayoutToolbarActionMode
import me.zhanghai.android.files.ui.PersistentDrawerLayout
import me.zhanghai.android.files.ui.ScrollingViewOnApplyWindowInsetsListener
import me.zhanghai.android.files.ui.ThemedFastScroller
import me.zhanghai.android.files.ui.ToolbarActionMode
import me.zhanghai.android.files.util.DebouncedRunnable
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.checkSelfPermission
import me.zhanghai.android.files.util.copyText
import me.zhanghai.android.files.util.create
import me.zhanghai.android.files.util.createInstallPackageIntent
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.createSendStreamIntent
import me.zhanghai.android.files.util.createViewIntent
import me.zhanghai.android.files.util.extraPath
import me.zhanghai.android.files.util.extraPathList
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.getQuantityString
import me.zhanghai.android.files.util.hasSw600Dp
import me.zhanghai.android.files.util.isOrientationLandscape
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.showToast
import me.zhanghai.android.files.util.startActivitySafe
import me.zhanghai.android.files.util.takeIfNotEmpty
import me.zhanghai.android.files.util.valueCompat
import me.zhanghai.android.files.util.viewModels
import me.zhanghai.android.files.util.withChooser
import me.zhanghai.android.files.viewer.image.ImageViewerActivity

class FileListFragment : Fragment(), BreadcrumbLayout.Listener, FileListAdapter.Listener,
    OpenApkDialogFragment.Listener, ConfirmDeleteFilesDialogFragment.Listener,
    CreateArchiveDialogFragment.Listener, RenameFileDialogFragment.Listener,
    CreateFileDialogFragment.Listener, CreateDirectoryDialogFragment.Listener,
    NavigateToPathDialogFragment.Listener, NavigationFragment.Listener,
    ShowRequestAllFilesAccessRationaleDialogFragment.Listener,
    ShowRequestStoragePermissionRationaleDialogFragment.Listener,
    ShowRequestStoragePermissionInSettingsRationaleDialogFragment.Listener {
    private val requestAllFilesAccessLauncher = registerForActivityResult(
        RequestAllFilesAccessContract(), this::onRequestAllFilesAccessResult
    )
    private val requestStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(), this::onRequestStoragePermissionResult
    )
    private val requestStoragePermissionInSettingsLauncher = registerForActivityResult(
        RequestStoragePermissionInSettingsContract(),
        this::onRequestStoragePermissionInSettingsResult
    )

    private val args by args<Args>()
    private val argsPath by lazy { args.intent.extraPath }

    private val viewModel by viewModels { { FileListViewModel() } }

    private lateinit var binding: Binding

    private lateinit var navigationFragment: NavigationFragment

    private lateinit var menuBinding: MenuBinding

    private lateinit var overlayActionMode: ToolbarActionMode

    private lateinit var bottomActionMode: ToolbarActionMode

    private lateinit var adapter: FileListAdapter

    private val debouncedSearchRunnable = DebouncedRunnable(Handler(Looper.getMainLooper()), 1000) {
        if (!isResumed || !viewModel.isSearchViewExpanded) {
            return@DebouncedRunnable
        }
        val query = viewModel.searchViewQuery
        if (query.isEmpty()) {
            return@DebouncedRunnable
        }
        viewModel.search(query)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        Binding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState == null) {
            navigationFragment = NavigationFragment()
            childFragmentManager.commit { add(R.id.navigationFragment, navigationFragment) }
        } else {
            navigationFragment = childFragmentManager.findFragmentById(R.id.navigationFragment)
                as NavigationFragment
        }
        navigationFragment.listener = this
        val activity = requireActivity() as AppCompatActivity
        activity.setTitle(R.string.file_list_title)
        activity.setSupportActionBar(binding.toolbar)
        overlayActionMode = OverlayToolbarActionMode(binding.overlayToolbar)
        bottomActionMode = PersistentBarLayoutToolbarActionMode(
            binding.persistentBarLayout, binding.bottomBarLayout, binding.bottomToolbar
        )
        val contentLayoutInitialPaddingBottom = binding.contentLayout.paddingBottom
        binding.appBarLayout.addOnOffsetChangedListener(
            OnOffsetChangedListener { _, verticalOffset ->
                binding.contentLayout.updatePaddingRelative(
                    bottom = contentLayoutInitialPaddingBottom +
                        binding.appBarLayout.totalScrollRange + verticalOffset
                )
            }
        )
        binding.appBarLayout.syncBackgroundElevationTo(binding.overlayToolbar)
        binding.breadcrumbLayout.setListener(this)
        if (!(activity.hasSw600Dp && activity.isOrientationLandscape)) {
            binding.swipeRefreshLayout.setProgressViewEndTarget(
                true, binding.swipeRefreshLayout.progressViewEndOffset
            )
        }
        binding.swipeRefreshLayout.setOnRefreshListener { this.refresh() }
        binding.recyclerView.layoutManager = GridLayoutManager(activity, /* TODO */ 1)
        adapter = FileListAdapter(this)
        binding.recyclerView.adapter = adapter
        val fastScroller = ThemedFastScroller.create(binding.recyclerView)
        binding.recyclerView.setOnApplyWindowInsetsListener(
            ScrollingViewOnApplyWindowInsetsListener(binding.recyclerView, fastScroller)
        )
        binding.speedDialView.inflate(R.menu.file_list_speed_dial)
        binding.speedDialView.setOnActionSelectedListener {
            when (it.id) {
                R.id.action_create_file -> showCreateFileDialog()
                R.id.action_create_directory -> showCreateDirectoryDialog()
            }
            // Returning false causes the speed dial to close without animation.
            //return false;
            binding.speedDialView.close()
            true
        }

        if (!viewModel.hasTrail) {
            var path = argsPath
            val intent = args.intent
            var pickOptions: PickOptions? = null
            when (val action = intent.action ?: Intent.ACTION_VIEW) {
                Intent.ACTION_GET_CONTENT, Intent.ACTION_OPEN_DOCUMENT,
                Intent.ACTION_CREATE_DOCUMENT -> {
                    val readOnly = action == Intent.ACTION_GET_CONTENT
                    val mimeType = intent.type?.asMimeTypeOrNull() ?: MimeType.ANY
                    val extraMimeTypes = intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)
                        ?.mapNotNull { it.asMimeTypeOrNull() }?.takeIfNotEmpty()
                    val mimeTypes = extraMimeTypes ?: listOf(mimeType)
                    val localOnly = intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false)
                    val allowMultiple = intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                    // TODO: Actually support ACTION_CREATE_DOCUMENT.
                    pickOptions = PickOptions(readOnly, false, mimeTypes, localOnly, allowMultiple)
                }
                Intent.ACTION_OPEN_DOCUMENT_TREE -> {
                    val localOnly = intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false)
                    pickOptions = PickOptions(false, true, emptyList(), localOnly, false)
                }
                ACTION_VIEW_DOWNLOADS ->
                    path = Paths.get(
                        @Suppress("DEPRECATION")
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        ).path
                    )
                Intent.ACTION_VIEW ->
                    if (path != null) {
                        val mimeType = intent.type?.asMimeTypeOrNull()
                        if (mimeType != null && path.isArchiveFile(mimeType)) {
                            path = path.createArchiveRootPath()
                        }
                    }
                else ->
                    if (path != null) {
                        val mimeType = intent.type?.asMimeTypeOrNull()
                        if (mimeType != null && path.isArchiveFile(mimeType)) {
                            path = path.createArchiveRootPath()
                        }
                    }
            }
            if (path == null) {
                path = Settings.FILE_LIST_DEFAULT_DIRECTORY.valueCompat
            }
            viewModel.resetTo(path)
            if (pickOptions != null) {
                viewModel.pickOptions = pickOptions
            }
        }
        val viewLifecycleOwner = viewLifecycleOwner
        if (binding.persistentDrawerLayout != null) {
            Settings.FILE_LIST_PERSISTENT_DRAWER_OPEN.observe(viewLifecycleOwner) {
                onPersistentDrawerOpenChanged(it)
            }
        }
        viewModel.currentPathLiveData.observe(viewLifecycleOwner) { onCurrentPathChanged(it) }
        viewModel.searchViewExpandedLiveData.observe(viewLifecycleOwner) {
            onSearchViewExpandedChanged(it)
        }
        viewModel.breadcrumbLiveData.observe(viewLifecycleOwner) {
            binding.breadcrumbLayout.setData(it)
        }
        viewModel.sortOptionsLiveData.observe(viewLifecycleOwner) { onSortOptionsChanged(it) }
        viewModel.sortPathSpecificLiveData.observe(viewLifecycleOwner) {
            onSortPathSpecificChanged(it)
        }
        viewModel.pickOptionsLiveData.observe(viewLifecycleOwner) { onPickOptionsChanged(it) }
        viewModel.selectedFilesLiveData.observe(viewLifecycleOwner) { onSelectedFilesChanged(it) }
        viewModel.pasteStateLiveData.observe(viewLifecycleOwner) { onPasteStateChanged(it) }
        Settings.FILE_NAME_ELLIPSIZE.observe(viewLifecycleOwner) { onFileNameEllipsizeChanged(it) }
        viewModel.fileListLiveData.observe(viewLifecycleOwner) { onFileListChanged(it) }
        Settings.FILE_LIST_SHOW_HIDDEN_FILES.observe(viewLifecycleOwner) {
            onShowHiddenFilesChanged(it)
        }
    }

    override fun onResume() {
        super.onResume()

        ensureStorageAccess()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menuBinding = MenuBinding.inflate(menu, inflater)
        setUpSearchView()
    }

    private fun setUpSearchView() {
        val searchView = menuBinding.searchItem.actionView as FixQueryChangeSearchView
        // MenuItem.OnActionExpandListener.onMenuItemActionExpand() is called before SearchView
        // resets the query.
        searchView.setOnSearchClickListener {
            viewModel.isSearchViewExpanded = true
            searchView.setQuery(viewModel.searchViewQuery, false)
            debouncedSearchRunnable()
        }
        // SearchView.OnCloseListener.onClose() is not always called.
        menuBinding.searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                viewModel.isSearchViewExpanded = false
                viewModel.stopSearching()
                return true
            }
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                debouncedSearchRunnable.cancel()
                viewModel.search(query)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (searchView.shouldIgnoreQueryChange) {
                    return false
                }
                viewModel.searchViewQuery = query
                debouncedSearchRunnable()
                return false
            }
        })
        if (viewModel.isSearchViewExpanded) {
            menuBinding.searchItem.expandActionView()
        }
    }

    private fun collapseSearchView() {
        if (this::menuBinding.isInitialized && menuBinding.searchItem.isActionViewExpanded) {
            menuBinding.searchItem.collapseActionView()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        updateSortMenuItems()
        updateSelectAllMenuItem()
        updateShowHiddenFilesMenuItem()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout?.openDrawer(GravityCompat.START)
                if (binding.persistentDrawerLayout != null) {
                    Settings.FILE_LIST_PERSISTENT_DRAWER_OPEN.putValue(
                        !Settings.FILE_LIST_PERSISTENT_DRAWER_OPEN.valueCompat
                    )
                }
                true
            }
            R.id.action_sort_by_name -> {
                viewModel.setSortBy(By.NAME)
                true
            }
            R.id.action_sort_by_type -> {
                viewModel.setSortBy(By.TYPE)
                true
            }
            R.id.action_sort_by_size -> {
                viewModel.setSortBy(By.SIZE)
                true
            }
            R.id.action_sort_by_last_modified -> {
                viewModel.setSortBy(By.LAST_MODIFIED)
                true
            }
            R.id.action_sort_order_ascending -> {
                viewModel.setSortOrder(
                    if (!menuBinding.sortOrderAscendingItem.isChecked) {
                        Order.ASCENDING
                    } else {
                        Order.DESCENDING
                    }
                )
                true
            }
            R.id.action_sort_directories_first -> {
                viewModel.setSortDirectoriesFirst(!menuBinding.sortDirectoriesFirstItem.isChecked)
                true
            }
            R.id.action_sort_path_specific -> {
                viewModel.isSortPathSpecific = !menuBinding.sortPathSpecificItem.isChecked
                true
            }
            R.id.action_new_task -> {
                newTask()
                true
            }
            R.id.action_navigate_up -> {
                navigateUp()
                true
            }
            R.id.action_navigate_to -> {
                showNavigateToPathDialog()
                true
            }
            R.id.action_refresh -> {
                refresh()
                true
            }
            R.id.action_select_all -> {
                selectAllFiles()
                true
            }
            R.id.action_show_hidden_files -> {
                setShowHiddenFiles(!menuBinding.showHiddenFilesItem.isChecked)
                true
            }
            R.id.action_share -> {
                share()
                true
            }
            R.id.action_copy_path -> {
                copyPath()
                true
            }
            R.id.action_open_in_terminal -> {
                openInTerminal()
                true
            }
            R.id.action_add_bookmark -> {
                addBookmark()
                true
            }
            R.id.action_create_shortcut -> {
                createShortcut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed(): Boolean {
        val drawerLayout = binding.drawerLayout
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }
        if (binding.speedDialView.isOpen) {
            binding.speedDialView.close()
            return true
        }
        if (overlayActionMode.isActive) {
            overlayActionMode.finish()
            return true
        }
        if (viewModel.navigateUp(false)) {
            return true
        }
        // See also https://developer.android.com/about/versions/12/behavior-changes-all#back-press
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && requireActivity().isTaskRoot) {
            viewModel.isStorageAccessRequested = false
        }
        return false
    }

    private fun onPersistentDrawerOpenChanged(open: Boolean) {
        binding.persistentDrawerLayout?.let {
            if (open) {
                it.openDrawer(GravityCompat.START)
            } else {
                it.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun onCurrentPathChanged(path: Path) {
        updateOverlayToolbar()
        updateBottomToolbar()
    }

    private fun onSearchViewExpandedChanged(expanded: Boolean) {
        updateSortMenuItems()
    }

    private fun onFileListChanged(stateful: Stateful<List<FileItem>>) {
        val files = stateful.value
        val isSearching = viewModel.searchState.isSearching
        when {
            stateful is Failure -> binding.toolbar.setSubtitle(R.string.error)
            stateful is Loading && !isSearching -> binding.toolbar.setSubtitle(R.string.loading)
            else -> binding.toolbar.subtitle = getSubtitle(files!!)
        }
        val hasFiles = !files.isNullOrEmpty()
        binding.swipeRefreshLayout.isRefreshing = stateful is Loading && (hasFiles || isSearching)
        binding.progress.fadeToVisibilityUnsafe(stateful is Loading && !(hasFiles || isSearching))
        binding.errorText.fadeToVisibilityUnsafe(stateful is Failure && !hasFiles)
        val throwable = (stateful as? Failure)?.throwable
        if (throwable != null) {
            throwable.printStackTrace()
            val error = throwable.toString()
            if (hasFiles) {
                showToast(error)
            } else {
                binding.errorText.text = error
            }
        }
        binding.emptyView.fadeToVisibilityUnsafe(stateful is Success && !hasFiles)
        if (files != null) {
            updateAdapterFileList()
        } else {
            // This resets animation as well.
            adapter.clear()
        }
        if (stateful is Success) {
            viewModel.pendingState
                ?.let { binding.recyclerView.layoutManager!!.onRestoreInstanceState(it) }
        }
    }

    private fun getSubtitle(files: List<FileItem>): String {
        val directoryCount = files.count { it.attributes.isDirectory }
        val fileCount = files.size - directoryCount
        val directoryCountText = if (directoryCount > 0) {
            getQuantityString(
                R.plurals.file_list_subtitle_directory_count_format, directoryCount, directoryCount
            )
        } else {
            null
        }
        val fileCountText = if (fileCount > 0) {
            getQuantityString(
                R.plurals.file_list_subtitle_file_count_format, fileCount, fileCount
            )
        } else {
            null
        }
        return when {
            !directoryCountText.isNullOrEmpty() && !fileCountText.isNullOrEmpty() ->
                (directoryCountText + getString(R.string.file_list_subtitle_separator)
                    + fileCountText)
            !directoryCountText.isNullOrEmpty() -> directoryCountText
            !fileCountText.isNullOrEmpty() -> fileCountText
            else -> getString(R.string.empty)
        }
    }

    private fun onSortOptionsChanged(sortOptions: FileSortOptions) {
        adapter.comparator = sortOptions.createComparator()
        updateSortMenuItems()
    }

    private fun onSortPathSpecificChanged(pathSpecific: Boolean) {
        updateSortMenuItems()
    }

    private fun updateSortMenuItems() {
        if (!this::menuBinding.isInitialized) {
            return
        }
        val searchViewExpanded = viewModel.isSearchViewExpanded
        menuBinding.sortItem.isVisible = !searchViewExpanded
        if (searchViewExpanded) {
            return
        }
        val sortOptions = viewModel.sortOptions
        val checkedSortByItem = when (sortOptions.by) {
            By.NAME -> menuBinding.sortByNameItem
            By.TYPE -> menuBinding.sortByTypeItem
            By.SIZE -> menuBinding.sortBySizeItem
            By.LAST_MODIFIED -> menuBinding.sortByLastModifiedItem
        }
        checkedSortByItem.isChecked = true
        menuBinding.sortOrderAscendingItem.isChecked = sortOptions.order == Order.ASCENDING
        menuBinding.sortDirectoriesFirstItem.isChecked = sortOptions.isDirectoriesFirst
        menuBinding.sortPathSpecificItem.isChecked = viewModel.isSortPathSpecific
    }

    private fun navigateUp() {
        collapseSearchView()
        viewModel.navigateUp(true)
    }

    private fun showNavigateToPathDialog() {
        NavigateToPathDialogFragment.show(currentPath, this)
    }

    private fun newTask() {
        openInNewTask(currentPath)
    }

    private fun refresh() {
        viewModel.reload()
    }

    private fun setShowHiddenFiles(showHiddenFiles: Boolean) {
        Settings.FILE_LIST_SHOW_HIDDEN_FILES.putValue(showHiddenFiles)
    }

    private fun onShowHiddenFilesChanged(showHiddenFiles: Boolean) {
        updateAdapterFileList()
        updateShowHiddenFilesMenuItem()
    }

    private fun updateAdapterFileList() {
        var files = viewModel.fileListStateful.value ?: return
        if (!Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat) {
            files = files.filterNot { it.isHidden }
        }
        adapter.replaceListAndIsSearching(files, viewModel.searchState.isSearching)
    }

    private fun updateShowHiddenFilesMenuItem() {
        if (!this::menuBinding.isInitialized) {
            return
        }
        val showHiddenFiles = Settings.FILE_LIST_SHOW_HIDDEN_FILES.valueCompat
        menuBinding.showHiddenFilesItem.isChecked = showHiddenFiles
    }

    private fun share() {
        shareFile(currentPath, MimeType.DIRECTORY)
    }

    private fun copyPath() {
        copyPath(currentPath)
    }

    private fun openInTerminal() {
        val path = currentPath
        if (path.isLinuxPath) {
            Terminal.open(path.toFile().path, requireContext())
        } else {
            // TODO
        }
    }

    override fun navigateTo(path: Path) {
        collapseSearchView()
        val state = binding.recyclerView.layoutManager!!.onSaveInstanceState()
        viewModel.navigateTo(state!!, path)
    }

    override fun copyPath(path: Path) {
        clipboardManager.copyText(path.toUserFriendlyString(), requireContext())
    }

    override fun openInNewTask(path: Path) {
        val intent = FileListActivity.createViewIntent(path)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivitySafe(intent)
    }

    private fun onPickOptionsChanged(pickOptions: PickOptions?) {
        val title = if (pickOptions == null) {
            getString(R.string.file_list_title)
        } else {
            val titleRes = if (pickOptions.pickDirectory) {
                R.plurals.file_list_title_pick_directory
            } else {
                R.plurals.file_list_title_pick_file
            }
            val count = if (pickOptions.allowMultiple) Int.MAX_VALUE else 1
            getQuantityString(titleRes, count)
        }
        requireActivity().title = title
        updateSelectAllMenuItem()
        updateOverlayToolbar()
        updateBottomToolbar()
        adapter.pickOptions = pickOptions
    }

    private fun updateSelectAllMenuItem() {
        if (!this::menuBinding.isInitialized) {
            return
        }
        val pickOptions = viewModel.pickOptions
        menuBinding.selectAllItem.isVisible = pickOptions == null || pickOptions.allowMultiple
    }

    private fun pickFiles(files: FileItemSet) {
        pickPaths(files.mapTo(linkedSetOf()) { it.path })
    }

    private fun pickPaths(paths: LinkedHashSet<Path>) {
        val intent = Intent().apply {
            val pickOptions = viewModel.pickOptions!!
            if (paths.size == 1) {
                val path = paths.single()
                data = path.fileProviderUri
                extraPath = path
            } else {
                val mimeTypes = pickOptions.mimeTypes.map { it.value }
                val items = paths.map { ClipData.Item(it.fileProviderUri) }
                clipData = ClipData::class.create(null, mimeTypes, items)
                extraPathList = paths.toList()
            }
            var flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            if (!pickOptions.readOnly) {
                flags = flags or (Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            if (pickOptions.pickDirectory) {
                flags = flags or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            }
            addFlags(flags)
        }
        requireActivity().run {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun onSelectedFilesChanged(files: FileItemSet) {
        updateOverlayToolbar()
        adapter.replaceSelectedFiles(files)
    }

    private fun updateOverlayToolbar() {
        val files = viewModel.selectedFiles
        if (files.isEmpty()) {
            if (overlayActionMode.isActive) {
                overlayActionMode.finish()
            }
            return
        }
        val pickOptions = viewModel.pickOptions
        if (pickOptions != null) {
            overlayActionMode.title = getString(R.string.file_list_select_title_format, files.size)
            overlayActionMode.setMenuResource(R.menu.file_list_pick)
            val menu = overlayActionMode.menu
            menu.findItem(R.id.action_select_all).isVisible = pickOptions.allowMultiple
        } else {
            overlayActionMode.title = getString(R.string.file_list_select_title_format, files.size)
            overlayActionMode.setMenuResource(R.menu.file_list_select)
            val menu = overlayActionMode.menu
            val isAnyFileReadOnly = files.any { it.path.fileSystem.isReadOnly }
            menu.findItem(R.id.action_cut).isVisible = !isAnyFileReadOnly
            val areAllFilesArchivePaths = files.all { it.path.isArchivePath }
            menu.findItem(R.id.action_copy)
                .setIcon(
                    if (areAllFilesArchivePaths) {
                        R.drawable.extract_icon_control_normal_24dp
                    } else {
                        R.drawable.copy_icon_control_normal_24dp
                    }
                )
                .setTitle(
                    if (areAllFilesArchivePaths) R.string.file_list_select_action_extract else R.string.copy
                )
            menu.findItem(R.id.action_delete).isVisible = !isAnyFileReadOnly
            val isCurrentPathReadOnly = viewModel.currentPath.fileSystem.isReadOnly
            menu.findItem(R.id.action_archive).isVisible = !isCurrentPathReadOnly
        }
        if (!overlayActionMode.isActive) {
            binding.appBarLayout.setExpanded(true)
            binding.appBarLayout.addOnOffsetChangedListener(
                AppBarLayoutExpandHackListener(binding.recyclerView)
            )
            overlayActionMode.start(object : ToolbarActionMode.Callback {
                override fun onToolbarActionModeStarted(toolbarActionMode: ToolbarActionMode) {}

                override fun onToolbarActionModeItemClicked(
                    toolbarActionMode: ToolbarActionMode,
                    item: MenuItem
                ): Boolean = onOverlayActionModeItemClicked(toolbarActionMode, item)

                override fun onToolbarActionModeFinished(toolbarActionMode: ToolbarActionMode) {
                    onOverlayActionModeFinished(toolbarActionMode)
                }
            })
        }
    }

    private fun onOverlayActionModeItemClicked(
        toolbarActionMode: ToolbarActionMode,
        item: MenuItem
    ): Boolean =
        when (item.itemId) {
            R.id.action_pick -> {
                pickFiles(viewModel.selectedFiles)
                true
            }
            R.id.action_cut -> {
                cutFiles(viewModel.selectedFiles)
                true
            }
            R.id.action_copy -> {
                copyFiles(viewModel.selectedFiles)
                true
            }
            R.id.action_delete -> {
                confirmDeleteFiles(viewModel.selectedFiles)
                true
            }
            R.id.action_archive -> {
                showCreateArchiveDialog(viewModel.selectedFiles)
                true
            }
            R.id.action_share -> {
                shareFiles(viewModel.selectedFiles)
                true
            }
            R.id.action_select_all -> {
                selectAllFiles()
                true
            }
            else -> false
        }

    private fun onOverlayActionModeFinished(toolbarActionMode: ToolbarActionMode) {
        viewModel.clearSelectedFiles()
    }

    private fun cutFiles(files: FileItemSet) {
        viewModel.addToPasteState(false, files)
        viewModel.selectFiles(files, false)
    }

    private fun copyFiles(files: FileItemSet) {
        viewModel.addToPasteState(true, files)
        viewModel.selectFiles(files, false)
    }

    private fun confirmDeleteFiles(files: FileItemSet) {
        ConfirmDeleteFilesDialogFragment.show(files, this)
    }

    override fun deleteFiles(files: FileItemSet) {
        FileJobService.delete(makePathListForJob(files), requireContext())
        viewModel.selectFiles(files, false)
    }

    private fun showCreateArchiveDialog(files: FileItemSet) {
        CreateArchiveDialogFragment.show(files, this)
    }

    override fun archive(
        files: FileItemSet,
        name: String,
        archiveType: String,
        compressorType: String?
    ) {
        val archiveFile = viewModel.currentPath.resolve(name)
        FileJobService.archive(
            makePathListForJob(files), archiveFile, archiveType, compressorType, requireContext()
        )
        viewModel.selectFiles(files, false)
    }

    private fun shareFiles(files: FileItemSet) {
        shareFiles(files.map { it.path }, files.map { it.mimeType })
        viewModel.selectFiles(files, false)
    }

    private fun selectAllFiles() {
        adapter.selectAllFiles()
    }

    private fun onPasteStateChanged(pasteState: PasteState) {
        updateBottomToolbar()
    }

    private fun updateBottomToolbar() {
        val pickOptions = viewModel.pickOptions
        if (pickOptions != null) {
            if (!pickOptions.pickDirectory) {
                if (bottomActionMode.isActive) {
                    bottomActionMode.finish()
                }
                return
            }
            bottomActionMode.setNavigationIcon(R.drawable.check_icon_control_normal_24dp)
            val path = viewModel.currentPath
            val navigationRoot = NavigationRootMapLiveData.valueCompat[path]
            val name = navigationRoot?.getName(requireContext()) ?: path.name
            bottomActionMode.title =
                getString(R.string.file_list_select_current_directory_format, name)
        } else {
            val pasteState = viewModel.pasteState
            val files = pasteState.files
            if (files.isEmpty()) {
                if (bottomActionMode.isActive) {
                    bottomActionMode.finish()
                }
                return
            }
            bottomActionMode.setNavigationIcon(R.drawable.close_icon_control_normal_24dp)
            val areAllFilesArchivePaths = files.all { it.path.isArchivePath }
            bottomActionMode.title = getString(
                if (pasteState.copy) {
                    if (areAllFilesArchivePaths) {
                        R.string.file_list_paste_extract_title_format
                    } else {
                        R.string.file_list_paste_copy_title_format
                    }
                } else {
                    R.string.file_list_paste_move_title_format
                }, files.size
            )
            bottomActionMode.setMenuResource(R.menu.file_list_paste)
            val isCurrentPathReadOnly = viewModel.currentPath.fileSystem.isReadOnly
            bottomActionMode.menu.findItem(R.id.action_paste)
                .setTitle(
                    if (areAllFilesArchivePaths) R.string.file_list_paste_action_extract_here else R.string.paste
                )
                .isEnabled = !isCurrentPathReadOnly
        }
        if (!bottomActionMode.isActive) {
            bottomActionMode.start(object : ToolbarActionMode.Callback {
                override fun onToolbarActionModeStarted(toolbarActionMode: ToolbarActionMode) {}

                override fun onToolbarActionModeItemClicked(
                    toolbarActionMode: ToolbarActionMode,
                    item: MenuItem
                ): Boolean = onBottomActionModeItemClicked(toolbarActionMode, item)

                override fun onToolbarActionModeFinished(toolbarActionMode: ToolbarActionMode) {
                    onBottomActionModeFinished(toolbarActionMode)
                }
            })
        }
    }

    private fun onBottomActionModeItemClicked(
        toolbarActionMode: ToolbarActionMode,
        item: MenuItem
    ): Boolean =
        when (item.itemId) {
            R.id.action_paste -> {
                pasteFiles(currentPath)
                true
            }
            else -> false
        }

    private fun onBottomActionModeFinished(toolbarActionMode: ToolbarActionMode) {
        val pickOptions = viewModel.pickOptions
        if (pickOptions != null) {
            if (pickOptions.pickDirectory) {
                pickPaths(linkedSetOf(viewModel.currentPath))
            }
        } else {
            viewModel.clearPasteState()
        }
    }

    private fun pasteFiles(targetDirectory: Path) {
        val pasteState = viewModel.pasteState
        if (viewModel.pasteState.copy) {
            FileJobService.copy(
                makePathListForJob(pasteState.files), targetDirectory, requireContext()
            )
        } else {
            FileJobService.move(
                makePathListForJob(pasteState.files), targetDirectory, requireContext()
            )
        }
        viewModel.clearPasteState()
    }

    private fun makePathListForJob(files: FileItemSet): List<Path> =
        files.map { it.path }.sorted()

    private fun onFileNameEllipsizeChanged(fileNameEllipsize: TextUtils.TruncateAt) {
        adapter.nameEllipsize = fileNameEllipsize
    }

    override fun clearSelectedFiles() {
        viewModel.clearSelectedFiles()
    }

    override fun selectFile(file: FileItem, selected: Boolean) {
        viewModel.selectFile(file, selected)
    }

    override fun selectFiles(files: FileItemSet, selected: Boolean) {
        viewModel.selectFiles(files, selected)
    }

    override fun openFile(file: FileItem) {
        val pickOptions = viewModel.pickOptions
        if (pickOptions != null) {
            if (file.attributes.isDirectory) {
                navigateTo(file.path)
            } else if (!pickOptions.pickDirectory) {
                pickFiles(fileItemSetOf(file))
            }
            return
        }
        if (file.mimeType.isApk) {
            openApk(file)
            return
        }
        if (file.isListable) {
            navigateTo(file.listablePath)
            return
        }
        openFileWithIntent(file, false)
    }

    private fun openApk(file: FileItem) {
        if (!file.isListable) {
            installApk(file)
            return
        }
        when (Settings.OPEN_APK_DEFAULT_ACTION.valueCompat) {
            OpenApkDefaultAction.INSTALL -> installApk(file)
            OpenApkDefaultAction.VIEW -> viewApk(file)
            OpenApkDefaultAction.ASK -> OpenApkDialogFragment.show(file, this)
        }
    }

    override fun installApk(file: FileItem) {
        val path = file.path
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!path.isArchivePath) path.fileProviderUri else null
        } else {
            // PackageInstaller only supports file URI before N.
            if (path.isLinuxPath) Uri.fromFile(path.toFile()) else null
        }
        if (uri != null) {
            startActivitySafe(uri.createInstallPackageIntent())
        } else {
            FileJobService.installApk(path, requireContext())
        }
    }

    override fun viewApk(file: FileItem) {
        navigateTo(file.listablePath)
    }

    override fun openFileWith(file: FileItem) {
        openFileWithIntent(file, true)
    }

    private fun openFileWithIntent(file: FileItem, withChooser: Boolean) {
        val path = file.path
        val mimeType = file.mimeType
        if (path.isArchivePath) {
            FileJobService.open(path, mimeType, withChooser, requireContext())
        } else {
            val intent = path.fileProviderUri.createViewIntent(mimeType)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .apply {
                    extraPath = path
                    maybeAddImageViewerActivityExtras(this, path, mimeType)
                }
                .let {
                    if (withChooser) {
                        it.withChooser(
                            EditFileActivity::class.createIntent()
                                .putArgs(EditFileActivity.Args(path, mimeType)),
                            OpenFileAsDialogActivity::class.createIntent()
                                .putArgs(OpenFileAsDialogFragment.Args(path))
                        )
                    } else {
                        it
                    }
                }
            startActivitySafe(intent)
        }
    }

    private fun maybeAddImageViewerActivityExtras(intent: Intent, path: Path, mimeType: MimeType) {
        if (!mimeType.isImage) {
            return
        }
        var paths = mutableListOf<Path>()
        // We need the ordered list from our adapter instead of the list from FileListLiveData.
        for (index in 0 until adapter.itemCount) {
            val file = adapter.getItem(index)
            val filePath = file.path
            if (file.mimeType.isImage || filePath == path) {
                paths.add(filePath)
            }
        }
        var position = paths.indexOf(path)
        if (position == -1) {
            return
        }
        // HACK: Don't send too many paths to avoid TransactionTooLargeException.
        if (paths.size > IMAGE_VIEWER_ACTIVITY_PATH_LIST_SIZE_MAX) {
            val start = (position - IMAGE_VIEWER_ACTIVITY_PATH_LIST_SIZE_MAX / 2)
                .coerceIn(0, paths.size - IMAGE_VIEWER_ACTIVITY_PATH_LIST_SIZE_MAX)
            paths = paths.subList(start, start + IMAGE_VIEWER_ACTIVITY_PATH_LIST_SIZE_MAX)
            position -= start
        }
        ImageViewerActivity.putExtras(intent, paths, position)
    }

    override fun cutFile(file: FileItem) {
        cutFiles(fileItemSetOf(file))
    }

    override fun copyFile(file: FileItem) {
        copyFiles(fileItemSetOf(file))
    }

    override fun confirmDeleteFile(file: FileItem) {
        confirmDeleteFiles(fileItemSetOf(file))
    }

    override fun showRenameFileDialog(file: FileItem) {
        RenameFileDialogFragment.show(file, this)
    }

    override fun hasFileWithName(name: String): Boolean {
        val fileListData = viewModel.fileListStateful
        return fileListData is Success && fileListData.value.any { it.name == name }
    }

    override fun renameFile(file: FileItem, newName: String) {
        FileJobService.rename(file.path, newName, requireContext())
        viewModel.selectFile(file, false)
    }

    override fun extractFile(file: FileItem) {
        copyFile(file.createDummyArchiveRoot())
    }

    override fun showCreateArchiveDialog(file: FileItem) {
        showCreateArchiveDialog(fileItemSetOf(file))
    }

    override fun shareFile(file: FileItem) {
        shareFile(file.path, file.mimeType)
    }

    private fun shareFile(path: Path, mimeType: MimeType) {
        shareFiles(listOf(path), listOf(mimeType))
    }

    private fun shareFiles(paths: List<Path>, mimeTypes: List<MimeType>) {
        val uris = paths.map { it.fileProviderUri }
        val intent = uris.createSendStreamIntent(mimeTypes)
            .withChooser()
        startActivitySafe(intent)
    }

    override fun copyPath(file: FileItem) {
        copyPath(file.path)
    }

    override fun addBookmark(file: FileItem) {
        addBookmark(file.path)
    }

    private fun addBookmark() {
        addBookmark(currentPath)
    }

    private fun addBookmark(path: Path) {
        BookmarkDirectories.add(BookmarkDirectory(null, path))
        showToast(R.string.file_add_bookmark_success)
    }

    override fun createShortcut(file: FileItem) {
        createShortcut(file.path, file.mimeType)
    }

    private fun createShortcut() {
        createShortcut(currentPath, MimeType.DIRECTORY)
    }

    private fun createShortcut(path: Path, mimeType: MimeType) {
        val context = requireContext()
        val isDirectory = mimeType == MimeType.DIRECTORY
        val shortcutInfo = ShortcutInfoCompat.Builder(context, path.toString())
            .setShortLabel(path.name)
            .setIntent(
                if (isDirectory) {
                    FileListActivity.createViewIntent(path)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                } else {
                    OpenFileActivity.createIntent(path, mimeType)
                }
            )
            .setIcon(
                IconCompat.createWithResource(
                    context, if (isDirectory) {
                        R.mipmap.directory_shortcut_icon
                    } else {
                        R.mipmap.file_shortcut_icon
                    }
                )
            )
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            showToast(R.string.shortcut_created)
        }
    }

    override fun showPropertiesDialog(file: FileItem) {
        FilePropertiesDialogFragment.show(file, this)
    }

    private fun showCreateFileDialog() {
        CreateFileDialogFragment.show(this)
    }

    override fun createFile(name: String) {
        val path = currentPath.resolve(name)
        FileJobService.create(path, false, requireContext())
    }

    private fun showCreateDirectoryDialog() {
        CreateDirectoryDialogFragment.show(this)
    }

    override fun createDirectory(name: String) {
        val path = currentPath.resolve(name)
        FileJobService.create(path, true, requireContext())
    }

    override val currentPath: Path
        get() = viewModel.currentPath

    override fun navigateToRoot(path: Path) {
        collapseSearchView()
        viewModel.resetTo(path)
    }

    override fun navigateToDefaultRoot() {
        navigateToRoot(Settings.FILE_LIST_DEFAULT_DIRECTORY.valueCompat)
    }

    override fun observeCurrentPath(owner: LifecycleOwner, observer: (Path) -> Unit) {
        viewModel.currentPathLiveData.observe(owner, observer)
    }

    override fun closeNavigationDrawer() {
        binding.drawerLayout?.closeDrawer(GravityCompat.START)
    }

    private fun ensureStorageAccess() {
        if (viewModel.isStorageAccessRequested) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                ShowRequestAllFilesAccessRationaleDialogFragment.show(this)
                viewModel.isStorageAccessRequested = true
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )) {
                    ShowRequestStoragePermissionRationaleDialogFragment.show(this)
                } else {
                    requestStoragePermission()
                }
                viewModel.isStorageAccessRequested = true
            }
        }
    }

    override fun requestAllFilesAccess() {
        requestAllFilesAccessLauncher.launch(Unit)
    }

    private fun onRequestAllFilesAccessResult(isGranted: Boolean) {
        if (isGranted) {
            viewModel.isStorageAccessRequested = false
            refresh()
        }
    }

    override fun requestStoragePermission() {
        requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun onRequestStoragePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            viewModel.isStorageAccessRequested = false
            refresh()
        } else if (!shouldShowRequestPermissionRationale(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
            ShowRequestStoragePermissionInSettingsRationaleDialogFragment.show(this)
        }
    }

    override fun requestStoragePermissionInSettings() {
        requestStoragePermissionInSettingsLauncher.launch(Unit)
    }

    private fun onRequestStoragePermissionInSettingsResult(isGranted: Boolean) {
        if (isGranted) {
            viewModel.isStorageAccessRequested = false
            refresh()
        }
    }

    companion object {
        private const val ACTION_VIEW_DOWNLOADS =
            "me.zhanghai.android.files.intent.action.VIEW_DOWNLOADS"

        private const val IMAGE_VIEWER_ACTIVITY_PATH_LIST_SIZE_MAX = 1000
    }

    private class RequestAllFilesAccessContract : ActivityResultContract<Unit, Boolean>() {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(
                android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.fromParts("package", context.packageName, null)
            )

        @RequiresApi(Build.VERSION_CODES.R)
        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            Environment.isExternalStorageManager()
    }

    private class RequestStoragePermissionInSettingsContract
        : ActivityResultContract<Unit, Boolean>() {
        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            application.checkSelfPermissionCompat(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
    }

    @Parcelize
    class Args(val intent: Intent) : ParcelableArgs

    private class Binding private constructor(
        val root: View,
        val drawerLayout: DrawerLayout? = null,
        val persistentDrawerLayout: PersistentDrawerLayout? = null,
        val persistentBarLayout: PersistentBarLayout,
        val appBarLayout: CoordinatorAppBarLayout,
        val toolbar: Toolbar,
        val overlayToolbar: Toolbar,
        val breadcrumbLayout: BreadcrumbLayout,
        val contentLayout: ViewGroup,
        val progress: ProgressBar,
        val errorText: TextView,
        val emptyView: View,
        val swipeRefreshLayout: SwipeRefreshLayout,
        val recyclerView: RecyclerView,
        val bottomBarLayout: ViewGroup,
        val bottomToolbar: Toolbar,
        val speedDialView: SpeedDialView
    ) {
        companion object {
            fun inflate(
                inflater: LayoutInflater,
                root: ViewGroup?,
                attachToRoot: Boolean
            ): Binding {
                val binding = FileListFragmentBinding.inflate(inflater, root, attachToRoot)
                val bindingRoot = binding.root
                val includeBinding = FileListFragmentIncludeBinding.bind(bindingRoot)
                val appBarBinding = FileListFragmentAppBarIncludeBinding.bind(bindingRoot)
                val contentBinding = FileListFragmentContentIncludeBinding.bind(bindingRoot)
                val bottomBarBinding = FileListFragmentBottomBarIncludeBinding.bind(bindingRoot)
                val speedDialBinding = FileListFragmentSpeedDialIncludeBinding.bind(bindingRoot)
                return Binding(
                    bindingRoot, includeBinding.drawerLayout, includeBinding.persistentDrawerLayout,
                    includeBinding.persistentBarLayout, appBarBinding.appBarLayout,
                    appBarBinding.toolbar, appBarBinding.overlayToolbar,
                    appBarBinding.breadcrumbLayout, contentBinding.contentLayout,
                    contentBinding.progress, contentBinding.errorText, contentBinding.emptyView,
                    contentBinding.swipeRefreshLayout, contentBinding.recyclerView,
                    bottomBarBinding.bottomBarLayout, bottomBarBinding.bottomToolbar,
                    speedDialBinding.speedDialView
                )
            }
        }
    }

    private class MenuBinding private constructor(
        val menu: Menu,
        val searchItem: MenuItem,
        val sortItem: MenuItem,
        val sortByNameItem: MenuItem,
        val sortByTypeItem: MenuItem,
        val sortBySizeItem: MenuItem,
        val sortByLastModifiedItem: MenuItem,
        val sortOrderAscendingItem: MenuItem,
        val sortDirectoriesFirstItem: MenuItem,
        val sortPathSpecificItem: MenuItem,
        val selectAllItem: MenuItem,
        val showHiddenFilesItem: MenuItem
    ) {
        companion object {
            fun inflate(menu: Menu, inflater: MenuInflater): MenuBinding {
                inflater.inflate(R.menu.file_list, menu)
                return MenuBinding(
                    menu, menu.findItem(R.id.action_search), menu.findItem(R.id.action_sort),
                    menu.findItem(R.id.action_sort_by_name),
                    menu.findItem(R.id.action_sort_by_type),
                    menu.findItem(R.id.action_sort_by_size),
                    menu.findItem(R.id.action_sort_by_last_modified),
                    menu.findItem(R.id.action_sort_order_ascending),
                    menu.findItem(R.id.action_sort_directories_first),
                    menu.findItem(R.id.action_sort_path_specific),
                    menu.findItem(R.id.action_select_all),
                    menu.findItem(R.id.action_show_hidden_files)
                )
            }
        }
    }
}
