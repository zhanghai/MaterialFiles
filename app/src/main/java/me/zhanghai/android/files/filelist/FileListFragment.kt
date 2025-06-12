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
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
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
import androidx.core.view.isVisible
import androidx.core.view.updatePaddingRelative
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.leinardi.android.speeddial.SpeedDialView
import java8.nio.file.Path
import java8.nio.file.Paths
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.app.clipboardManager
import me.zhanghai.android.files.compat.checkSelfPermissionCompat
import me.zhanghai.android.files.compat.setGroupDividerEnabledCompat
import me.zhanghai.android.files.databinding.FileListFragmentAppBarIncludeBinding
import me.zhanghai.android.files.databinding.FileListFragmentBinding
import me.zhanghai.android.files.databinding.FileListFragmentBottomBarIncludeBinding
import me.zhanghai.android.files.databinding.FileListFragmentContentIncludeBinding
import me.zhanghai.android.files.databinding.FileListFragmentIncludeBinding
import me.zhanghai.android.files.databinding.FileListFragmentSpeedDialIncludeBinding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.asMimeTypeOrNull
import me.zhanghai.android.files.file.extension
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
import me.zhanghai.android.files.ui.DrawerLayoutOnBackPressedCallback
import me.zhanghai.android.files.ui.FixQueryChangeSearchView
import me.zhanghai.android.files.ui.OverlayToolbarActionMode
import me.zhanghai.android.files.ui.PersistentBarLayout
import me.zhanghai.android.files.ui.PersistentBarLayoutToolbarActionMode
import me.zhanghai.android.files.ui.PersistentDrawerLayout
import me.zhanghai.android.files.ui.ScrollingViewOnApplyWindowInsetsListener
import me.zhanghai.android.files.ui.SpeedDialViewOnBackPressedCallback
import me.zhanghai.android.files.ui.ThemedFastScroller
import me.zhanghai.android.files.ui.ToolbarActionMode
import me.zhanghai.android.files.util.DebouncedRunnable
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.addOnBackPressedCallback
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.asFileName
import me.zhanghai.android.files.util.asFileNameOrNull
import me.zhanghai.android.files.util.checkSelfPermission
import me.zhanghai.android.files.util.copyText
import me.zhanghai.android.files.util.create
import me.zhanghai.android.files.util.createInstallPackageIntent
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.createManageAppAllFilesAccessPermissionIntent
import me.zhanghai.android.files.util.createSendStreamIntent
import me.zhanghai.android.files.util.createViewIntent
import me.zhanghai.android.files.util.extraPath
import me.zhanghai.android.files.util.extraPathList
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.getDimensionDp
import me.zhanghai.android.files.util.getQuantityString
import me.zhanghai.android.files.util.hasSw600Dp
import me.zhanghai.android.files.util.isOrientationLandscape
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.setOnEditorConfirmActionListener
import me.zhanghai.android.files.util.showToast
import me.zhanghai.android.files.util.startActivitySafe
import me.zhanghai.android.files.util.supportsExternalStorageManager
import me.zhanghai.android.files.util.takeIfNotEmpty
import me.zhanghai.android.files.util.valueCompat
import me.zhanghai.android.files.util.viewModels
import me.zhanghai.android.files.util.withChooser
import me.zhanghai.android.files.viewer.image.ImageViewerActivity
import kotlin.math.roundToInt

class FileListFragment : Fragment(), BreadcrumbLayout.Listener, FileListAdapter.Listener,
    ConfirmReplaceFileDialogFragment.Listener, OpenApkDialogFragment.Listener,
    ConfirmDeleteFilesDialogFragment.Listener, CreateArchiveDialogFragment.Listener,
    RenameFileDialogFragment.Listener, CreateFileDialogFragment.Listener,
    CreateDirectoryDialogFragment.Listener, NavigateToPathDialogFragment.Listener,
    NavigationFragment.Listener, ShowRequestAllFilesAccessRationaleDialogFragment.Listener,
    ShowRequestNotificationPermissionRationaleDialogFragment.Listener,
    ShowRequestNotificationPermissionInSettingsRationaleDialogFragment.Listener,
    ShowRequestStoragePermissionRationaleDialogFragment.Listener,
    ShowRequestStoragePermissionInSettingsRationaleDialogFragment.Listener {
    private val requestAllFilesAccessLauncher = registerForActivityResult(
        RequestAllFilesAccessContract(), this::onRequestAllFilesAccessResult
    )
    private val requestStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(), this::onRequestStoragePermissionResult
    )
    private val requestStoragePermissionInSettingsLauncher = registerForActivityResult(
        RequestPermissionInSettingsContract(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
        this::onRequestStoragePermissionInSettingsResult
    )
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(), this::onRequestNotificationPermissionResult
    )
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestNotificationPermissionInSettingsLauncher = registerForActivityResult(
        RequestPermissionInSettingsContract(android.Manifest.permission.POST_NOTIFICATIONS),
        this::onRequestNotificationPermissionInSettingsResult
    )

    private val args by args<Args>()
    private val argsPath by lazy { args.intent.extraPath }

    private val viewModel by viewModels { { FileListViewModel() } }

    private lateinit var binding: Binding

    private lateinit var navigationFragment: NavigationFragment

    private lateinit var menuBinding: MenuBinding

    private lateinit var overlayActionMode: ToolbarActionMode

    private lateinit var bottomActionMode: ToolbarActionMode

    private lateinit var layoutManager: GridLayoutManager

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
        binding.appBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            binding.contentLayout.updatePaddingRelative(
                bottom = contentLayoutInitialPaddingBottom +
                    binding.appBarLayout.totalScrollRange + verticalOffset
            )
        }
        binding.appBarLayout.syncBackgroundColorTo(binding.overlayToolbar)
        binding.breadcrumbLayout.setListener(this)
        if (!(activity.hasSw600Dp && activity.isOrientationLandscape)) {
            binding.swipeRefreshLayout.setProgressViewEndTarget(
                true, binding.swipeRefreshLayout.progressViewEndOffset
            )
        }
        binding.swipeRefreshLayout.setOnRefreshListener { this.refresh() }
        layoutManager = GridLayoutManager(activity, 1)
        binding.recyclerView.layoutManager = layoutManager
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
            //return false
            binding.speedDialView.close()
            true
        }

        val viewLifecycleOwner = viewLifecycleOwner
        addOnBackPressedCallback(
            object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    viewModel.navigateUp()
                }
            }
                .also { callback ->
                    viewModel.breadcrumbLiveData.observe(viewLifecycleOwner) {
                        callback.isEnabled = viewModel.canNavigateUpBreadcrumb
                    }
                }
        )
        addOnBackPressedCallback(overlayActionMode.onBackPressedCallback)
        addOnBackPressedCallback(SpeedDialViewOnBackPressedCallback(binding.speedDialView))
        binding.drawerLayout?.let {
            addOnBackPressedCallback(DrawerLayoutOnBackPressedCallback(it))
        }

        if (!viewModel.hasTrail) {
            var path = argsPath
            val intent = args.intent
            var pickOptions: PickOptions? = null
            when (val action = intent.action) {
                Intent.ACTION_GET_CONTENT, Intent.ACTION_OPEN_DOCUMENT,
                Intent.ACTION_CREATE_DOCUMENT -> {
                    val mode = if (action == Intent.ACTION_CREATE_DOCUMENT) {
                        PickOptions.Mode.CREATE_FILE
                    } else {
                        PickOptions.Mode.OPEN_FILE
                    }
                    val mimeType = intent.type?.asMimeTypeOrNull() ?: MimeType.ANY
                    val fileName = if (mode == PickOptions.Mode.CREATE_FILE) {
                        intent.getStringExtra(Intent.EXTRA_TITLE)?.asFileNameOrNull()?.value
                            ?: mimeType.extension?.let { "file.$it" } ?: "file"
                    } else {
                        null
                    }
                    val readOnly = action == Intent.ACTION_GET_CONTENT
                    val extraMimeTypes = if (mode == PickOptions.Mode.OPEN_FILE) {
                        intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)
                            ?.mapNotNull { it.asMimeTypeOrNull() }?.takeIfNotEmpty()
                    } else {
                        null
                    }
                    val mimeTypes = extraMimeTypes ?: listOf(mimeType)
                    val localOnly = intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false)
                    val allowMultiple = mode != PickOptions.Mode.CREATE_FILE &&
                        intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                    pickOptions =
                        PickOptions(mode, fileName, readOnly, mimeTypes, localOnly, allowMultiple)
                }
                Intent.ACTION_OPEN_DOCUMENT_TREE -> {
                    val localOnly = intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false)
                    pickOptions = PickOptions(
                        PickOptions.Mode.OPEN_DIRECTORY, null, false, emptyList(), localOnly, false
                    )
                }
                ACTION_VIEW_DOWNLOADS ->
                    path = Paths.get(
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        ).path
                    )
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
        viewModel.currentPathLiveData.observe(viewLifecycleOwner) { onCurrentPathChanged(it) }
        viewModel.searchViewExpandedLiveData.observe(viewLifecycleOwner) {
            onSearchViewExpandedChanged(it)
        }
        viewModel.breadcrumbLiveData.observe(viewLifecycleOwner) {
            binding.breadcrumbLayout.setData(it)
        }
        viewModel.viewTypeLiveData.observe(viewLifecycleOwner) { onViewTypeChanged(it) }
        // Live data only calls observeForever() on its sources when it is active, so we have to
        // make view type live data active first (so that it can load its initial value) before we
        // register another observer that needs to get the view type.
        if (binding.persistentDrawerLayout != null) {
            Settings.FILE_LIST_PERSISTENT_DRAWER_OPEN.observe(viewLifecycleOwner) {
                onPersistentDrawerOpenChanged(it)
            }
        }
        viewModel.sortOptionsLiveData.observe(viewLifecycleOwner) { onSortOptionsChanged(it) }
        viewModel.viewSortPathSpecificLiveData.observe(viewLifecycleOwner) {
            onViewSortPathSpecificChanged(it)
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

        if (!viewModel.isNotificationPermissionRequested) {
            ensureStorageAccess()
        }
        if (!viewModel.isStorageAccessRequested) {
            ensureNotificationPermission()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menuBinding = MenuBinding.inflate(menu, inflater)
        menuBinding.viewSortItem.subMenu!!.setGroupDividerEnabledCompat(true)
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

        updateViewSortMenuItems()
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
            R.id.action_view_list -> {
                viewModel.viewType = FileViewType.LIST
                true
            }
            R.id.action_view_grid -> {
                viewModel.viewType = FileViewType.GRID
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
            R.id.action_view_sort_path_specific -> {
                viewModel.isViewSortPathSpecific = !menuBinding.viewSortPathSpecificItem.isChecked
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

    fun onKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        if (bottomActionMode.isActive) {
            val menu = bottomActionMode.menu
            menu.setQwertyMode(
                KeyCharacterMap.load(event.deviceId).keyboardType != KeyCharacterMap.NUMERIC
            )
            if (menu.performShortcut(keyCode, event, 0)) {
                return true
            }
        }
        if (overlayActionMode.isActive) {
            val menu = overlayActionMode.menu
            menu.setQwertyMode(
                KeyCharacterMap.load(event.deviceId).keyboardType != KeyCharacterMap.NUMERIC
            )
            if (menu.performShortcut(keyCode, event, 0)) {
                return true
            }
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
        updateSpanCount()
    }

    private fun onCurrentPathChanged(path: Path) {
        updateOverlayToolbar()
        updateBottomToolbar()
    }

    private fun onSearchViewExpandedChanged(expanded: Boolean) {
        updateViewSortMenuItems()
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
            viewModel.pendingState?.let { layoutManager.onRestoreInstanceState(it) }
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

    private fun onViewTypeChanged(viewType: FileViewType) {
        updateSpanCount()
        adapter.viewType = viewType
        updateViewSortMenuItems()
    }

    private fun updateSpanCount() {
        layoutManager.spanCount = when (viewModel.viewType) {
            FileViewType.LIST -> 1
            FileViewType.GRID -> {
                var widthDp = resources.configuration.screenWidthDp
                val persistentDrawerLayout = binding.persistentDrawerLayout
                if (persistentDrawerLayout != null &&
                    persistentDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    widthDp -= getDimensionDp(R.dimen.navigation_max_width).roundToInt()
                }
                (widthDp / 180).coerceAtLeast(2)
            }
        }
    }

    private fun onSortOptionsChanged(sortOptions: FileSortOptions) {
        adapter.sortOptions = sortOptions
        updateViewSortMenuItems()
    }

    private fun onViewSortPathSpecificChanged(pathSpecific: Boolean) {
        updateViewSortMenuItems()
    }

    private fun updateViewSortMenuItems() {
        if (!this::menuBinding.isInitialized) {
            return
        }
        val searchViewExpanded = viewModel.isSearchViewExpanded
        menuBinding.viewSortItem.isVisible = !searchViewExpanded
        if (searchViewExpanded) {
            return
        }
        val viewType = viewModel.viewType
        val checkedViewTypeItem = when (viewType) {
            FileViewType.LIST -> menuBinding.viewListItem
            FileViewType.GRID -> menuBinding.viewGridItem
        }
        checkedViewTypeItem.isChecked = true
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
        menuBinding.viewSortPathSpecificItem.isChecked = viewModel.isViewSortPathSpecific
    }

    private fun navigateUp() {
        collapseSearchView()
        viewModel.navigateUp()
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
        val state = layoutManager.onSaveInstanceState()
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
            val count = if (pickOptions.allowMultiple) Int.MAX_VALUE else 1
            when (pickOptions.mode) {
                PickOptions.Mode.OPEN_FILE ->
                    getQuantityString(R.plurals.file_list_title_open_file, count)
                PickOptions.Mode.CREATE_FILE -> getString(R.string.file_list_title_create_file)
                PickOptions.Mode.OPEN_DIRECTORY ->
                    getQuantityString(R.plurals.file_list_title_open_directory, count)
            }
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
            if (pickOptions.mode == PickOptions.Mode.OPEN_DIRECTORY) {
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
            val isOpen = when (pickOptions.mode) {
                PickOptions.Mode.OPEN_FILE, PickOptions.Mode.OPEN_DIRECTORY -> true
                PickOptions.Mode.CREATE_FILE -> false
            }
            menu.findItem(R.id.action_open).isVisible = isOpen
            menu.findItem(R.id.action_create).isVisible = !isOpen
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
                    if (areAllFilesArchivePaths) {
                        R.string.file_list_select_action_extract
                    } else {
                        R.string.copy
                    }
                )
            menu.findItem(R.id.action_delete).isVisible = !isAnyFileReadOnly
            val areAllFilesArchiveFiles = files.all { it.isArchiveFile }
            menu.findItem(R.id.action_extract).isVisible = areAllFilesArchiveFiles
            val isCurrentPathReadOnly = viewModel.currentPath.fileSystem.isReadOnly
            menu.findItem(R.id.action_archive).isVisible = !isCurrentPathReadOnly
        }
        if (!overlayActionMode.isActive) {
            binding.appBarLayout.setExpanded(true)
            binding.appBarLayout.addOnOffsetChangedListener(
                AppBarLayoutExpandHackListener(binding.recyclerView)
            )
            overlayActionMode.start(object : ToolbarActionMode.Callback {
                override fun onToolbarActionModeMenuItemClicked(
                    toolbarActionMode: ToolbarActionMode,
                    item: MenuItem
                ): Boolean = onOverlayActionModeMenuItemClicked(item)

                override fun onToolbarActionModeFinished(toolbarActionMode: ToolbarActionMode) {
                    onOverlayActionModeFinished()
                }
            })
        }
    }

    private fun onOverlayActionModeMenuItemClicked(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_open -> {
                pickFiles(viewModel.selectedFiles)
                true
            }
            R.id.action_create -> {
                confirmReplaceFile(viewModel.selectedFiles.single())
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
            R.id.action_extract -> {
                extractFiles(viewModel.selectedFiles)
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
            R.id.action_select_range -> {
                rangeSelectFiles()
                true
            }
            else -> false
        }

    private fun onOverlayActionModeFinished() {
        viewModel.clearSelectedFiles()
    }

    private fun confirmReplaceFile(file: FileItem, setFileName: Boolean = true) {
        if (setFileName) {
            val fileName = file.name
            binding.bottomCreateFileNameEdit.setText(fileName)
            binding.bottomCreateFileNameEdit.setSelection(
                0, fileName.asFileName().baseName.length
            )
        }
        ConfirmReplaceFileDialogFragment.show(file, this)
    }

    override fun replaceFile(file: FileItem) {
        pickFiles(fileItemSetOf(file))
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

    private fun extractFiles(files: FileItemSet) {
        copyFiles(files.mapTo(fileItemSetOf()) { it.createDummyArchiveRoot() })
        viewModel.selectFiles(files, false)
    }

    private fun showCreateArchiveDialog(files: FileItemSet) {
        CreateArchiveDialogFragment.show(files, this)
    }

    override fun archive(
        files: FileItemSet,
        name: String,
        format: Int,
        filter: Int,
        password: String?
    ) {
        val archiveFile = viewModel.currentPath.resolve(name)
        FileJobService.archive(
            makePathListForJob(files), archiveFile, format, filter, password, requireContext()
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

    private fun rangeSelectFiles() {
        adapter.rangeSelectFiles()
    }

    private fun onPasteStateChanged(pasteState: PasteState) {
        updateBottomToolbar()
    }

    private fun updateBottomToolbar() {
        val pickOptions = viewModel.pickOptions
        if (pickOptions != null) {
            bottomActionMode.setMenuResource(R.menu.file_list_pick_bottom)
            val menu = bottomActionMode.menu
            when (pickOptions.mode) {
                PickOptions.Mode.CREATE_FILE -> {
                    bottomActionMode.title = null
                    binding.bottomCreateFileNameEdit.isVisible = true
                    val createMenuItem = menu.findItem(R.id.action_create)
                    binding.bottomCreateFileNameEdit.setOnEditorConfirmActionListener {
                        onBottomActionModeMenuItemClicked(createMenuItem)
                    }
                    if (!viewModel.isCreateFileNameEditInitialized) {
                        val fileName = pickOptions.fileName!!
                        binding.bottomCreateFileNameEdit.setText(fileName)
                        binding.bottomCreateFileNameEdit.setSelection(
                            0, fileName.asFileName().baseName.length
                        )
                        binding.bottomCreateFileNameEdit.requestFocus()
                        viewModel.isCreateFileNameEditInitialized = true
                    }
                    menu.findItem(R.id.action_open).isVisible = false
                    createMenuItem.isVisible = true
                }
                PickOptions.Mode.OPEN_DIRECTORY -> {
                    val path = viewModel.currentPath
                    val navigationRoot = NavigationRootMapLiveData.valueCompat[path]
                    val name = navigationRoot?.getName(requireContext()) ?: path.name
                    bottomActionMode.title =
                        getString(R.string.file_list_open_current_directory_format, name)
                    binding.bottomCreateFileNameEdit.isVisible = false
                    menu.findItem(R.id.action_open).isVisible = true
                    menu.findItem(R.id.action_create).isVisible = false
                }
                else -> {
                    if (bottomActionMode.isActive) {
                        bottomActionMode.finish()
                    }
                    return
                }
            }
        } else {
            val pasteState = viewModel.pasteState
            val files = pasteState.files
            if (files.isEmpty()) {
                if (bottomActionMode.isActive) {
                    bottomActionMode.finish()
                }
                return
            }
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
            binding.bottomCreateFileNameEdit.isVisible = false
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
                override fun onToolbarNavigationIconClicked(toolbarActionMode: ToolbarActionMode) {
                    onBottomToolbarNavigationIconClicked()
                }

                override fun onToolbarActionModeMenuItemClicked(
                    toolbarActionMode: ToolbarActionMode,
                    item: MenuItem
                ): Boolean = onBottomActionModeMenuItemClicked(item)

                override fun onToolbarActionModeFinished(toolbarActionMode: ToolbarActionMode) {
                    onBottomActionModeFinished()
                }
            })
        }
    }

    private fun onBottomToolbarNavigationIconClicked() {
        val pickOptions = viewModel.pickOptions
        if (pickOptions != null) {
            requireActivity().finish()
        } else {
            bottomActionMode.finish()
        }
    }

    private fun onBottomActionModeMenuItemClicked(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_open -> {
                pickPaths(linkedSetOf(viewModel.currentPath))
                true
            }
            R.id.action_create -> {
                val fileName = binding.bottomCreateFileNameEdit.text.toString()
                if (fileName.isEmpty()) {
                    showToast(R.string.file_list_create_file_name_error_empty)
                } else if (fileName.asFileNameOrNull() == null) {
                    showToast(R.string.file_list_create_file_name_error_invalid)
                } else {
                    val file = getFileWithName(fileName)
                    if (file != null) {
                        confirmReplaceFile(file, false)
                    } else {
                        val path = viewModel.currentPath.resolve(fileName)
                        pickPaths(linkedSetOf(path))
                    }
                }
                true
            }
            R.id.action_paste -> {
                pasteFiles(currentPath)
                true
            }
            else -> false
        }

    private fun onBottomActionModeFinished() {
        val pickOptions = viewModel.pickOptions
        if (pickOptions == null) {
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
        files.map { it.path }.sortedBy { it.toUri() }

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
            } else {
                when (pickOptions.mode) {
                    PickOptions.Mode.OPEN_FILE -> pickFiles(fileItemSetOf(file))
                    PickOptions.Mode.CREATE_FILE -> confirmReplaceFile(file)
                    PickOptions.Mode.OPEN_DIRECTORY -> {}
                }
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
        for (index in 0..<adapter.itemCount) {
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

    override fun hasFileWithName(name: String): Boolean = getFileWithName(name) != null

    private fun getFileWithName(name: String): FileItem? {
        val fileListData = viewModel.fileListStateful
        if (fileListData !is Success) {
            return null
        }
        return fileListData.value.find { it.name == name }
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
        if (Environment::class.supportsExternalStorageManager()) {
            if (!Environment.isExternalStorageManager()) {
                ShowRequestAllFilesAccessRationaleDialogFragment.show(this)
                viewModel.isStorageAccessRequested = true
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
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

    override fun onShowRequestAllFilesAccessRationaleResult(shouldRequest: Boolean) {
        if (shouldRequest) {
            requestAllFilesAccess()
        } else {
            viewModel.isStorageAccessRequested = false
            // This isn't an onActivityResult() callback so it's not delivered before calling
            // onResume(), and we need to do this manually.
            ensureNotificationPermission()
        }
    }

    private fun requestAllFilesAccess() {
        requestAllFilesAccessLauncher.launch(Unit)
    }

    private fun onRequestAllFilesAccessResult(isGranted: Boolean) {
        viewModel.isStorageAccessRequested = false
        if (isGranted) {
            refresh()
        }
    }

    override fun onShowRequestStoragePermissionRationaleResult(shouldRequest: Boolean) {
        if (shouldRequest) {
            requestStoragePermission()
        } else {
            viewModel.isStorageAccessRequested = false
        }
    }

    private fun requestStoragePermission() {
        requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun onRequestStoragePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            viewModel.isStorageAccessRequested = false
            refresh()
        } else if (shouldShowRequestPermissionRationale(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )) {
            ShowRequestStoragePermissionRationaleDialogFragment.show(this)
        } else {
            ShowRequestStoragePermissionInSettingsRationaleDialogFragment.show(this)
        }
    }

    override fun onShowRequestStoragePermissionInSettingsRationaleResult(shouldRequest: Boolean) {
        if (shouldRequest) {
            requestStoragePermissionInSettings()
        } else {
            viewModel.isStorageAccessRequested = false
        }
    }

    private fun requestStoragePermissionInSettings() {
        requestStoragePermissionInSettingsLauncher.launch(Unit)
    }

    private fun onRequestStoragePermissionInSettingsResult(isGranted: Boolean) {
        viewModel.isStorageAccessRequested = false
        if (isGranted) {
            refresh()
        }
    }

    private fun ensureNotificationPermission() {
        if (viewModel.isNotificationPermissionRequested) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )) {
                    ShowRequestNotificationPermissionRationaleDialogFragment.show(this)
                } else {
                    requestNotificationPermission()
                }
                viewModel.isNotificationPermissionRequested = true
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onShowRequestNotificationPermissionRationaleResult(shouldRequest: Boolean) {
        if (shouldRequest) {
            requestNotificationPermission()
        } else {
            viewModel.isNotificationPermissionRequested = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun onRequestNotificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            viewModel.isNotificationPermissionRequested = false
        } else if (shouldShowRequestPermissionRationale(
            android.Manifest.permission.POST_NOTIFICATIONS
        )) {
            ShowRequestNotificationPermissionRationaleDialogFragment.show(this)
        } else {
            ShowRequestNotificationPermissionInSettingsRationaleDialogFragment.show(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onShowRequestNotificationPermissionInSettingsRationaleResult(
        shouldRequest: Boolean
    ) {
        if (shouldRequest) {
            requestNotificationPermissionInSettings()
        } else {
            viewModel.isNotificationPermissionRequested = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermissionInSettings() {
        requestNotificationPermissionInSettingsLauncher.launch(Unit)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun onRequestNotificationPermissionInSettingsResult(isGranted: Boolean) {
        if (isGranted) {
            viewModel.isNotificationPermissionRequested = false
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
            Environment::class.createManageAppAllFilesAccessPermissionIntent(context.packageName)

        @RequiresApi(Build.VERSION_CODES.R)
        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            Environment.isExternalStorageManager()
    }

    private class RequestPermissionInSettingsContract(private val permissionName: String)
        : ActivityResultContract<Unit, Boolean>() {
        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            application.checkSelfPermissionCompat(permissionName) ==
                PackageManager.PERMISSION_GRANTED
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
        val bottomCreateFileNameEdit: EditText,
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
                    bottomBarBinding.bottomCreateFileNameEdit, speedDialBinding.speedDialView
                )
            }
        }
    }

    private class MenuBinding private constructor(
        val menu: Menu,
        val searchItem: MenuItem,
        val viewSortItem: MenuItem,
        val viewListItem: MenuItem,
        val viewGridItem: MenuItem,
        val sortByNameItem: MenuItem,
        val sortByTypeItem: MenuItem,
        val sortBySizeItem: MenuItem,
        val sortByLastModifiedItem: MenuItem,
        val sortOrderAscendingItem: MenuItem,
        val sortDirectoriesFirstItem: MenuItem,
        val viewSortPathSpecificItem: MenuItem,
        val selectAllItem: MenuItem,
        val showHiddenFilesItem: MenuItem
    ) {
        companion object {
            fun inflate(menu: Menu, inflater: MenuInflater): MenuBinding {
                inflater.inflate(R.menu.file_list, menu)
                return MenuBinding(
                    menu, menu.findItem(R.id.action_search), menu.findItem(R.id.action_view_sort),
                    menu.findItem(R.id.action_view_list), menu.findItem(R.id.action_view_grid),
                    menu.findItem(R.id.action_sort_by_name),
                    menu.findItem(R.id.action_sort_by_type),
                    menu.findItem(R.id.action_sort_by_size),
                    menu.findItem(R.id.action_sort_by_last_modified),
                    menu.findItem(R.id.action_sort_order_ascending),
                    menu.findItem(R.id.action_sort_directories_first),
                    menu.findItem(R.id.action_view_sort_path_specific),
                    menu.findItem(R.id.action_select_all),
                    menu.findItem(R.id.action_show_hidden_files)
                )
            }
        }
    }
}
